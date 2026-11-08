#!/usr/bin/env python3
"""Value-free service contract tooling; OpenBao owns auth, leases and encryption."""
import argparse
import hashlib
import json
import os
import re
import shutil
import stat
import sys
import tempfile
from datetime import datetime, timezone
from pathlib import Path


def now():
    return datetime.now(timezone.utc).isoformat().replace('+00:00', 'Z')


def contract(path):
    m = json.loads(Path(path).read_text())
    assert set(m) == {'schemaVersion','service','environment','mode','owner','path','entries'}
    assert m['schemaVersion'] == 1
    assert re.fullmatch(r'[a-z][a-z0-9-]{1,63}', m['service'])
    assert m['environment'] in ('stage', 'prod')
    assert m['mode'] in ('managed', 'observe')
    assert re.fullmatch(r'kv/(stage|prod)/[a-z0-9-]+/runtime', m['path'])
    assert m['path'].split('/')[1:3] == [m['environment'], m['service']]
    assert m['entries'] and len(m['entries']) <= 200
    names, targets = set(), set()
    for e in m['entries']:
        assert set(e) == {'key','target','format'}
        assert re.fullmatch(r'[A-Z][A-Z0-9_]{0,100}', e['key'])
        assert e['key'] not in names
        names.add(e['key'])
        assert e['format'] in ('text', 'json')
        assert re.fullmatch(r'(configtree|files)/[A-Za-z0-9_.-]+', e['target'])
        assert e['target'] not in targets
        targets.add(e['target'])
    return m


def revision(m):
    return hashlib.sha256(json.dumps(m, sort_keys=True).encode()).hexdigest()[:16]


def atomic(path, data, mode=0o600):
    path = Path(path)
    fd, tmp = tempfile.mkstemp(dir=path.parent)
    try:
        with os.fdopen(fd, 'w') as f:
            json.dump(data, f, ensure_ascii=False)
            f.flush()
            os.fsync(f.fileno())
        os.chmod(tmp, mode)
        os.replace(tmp, path)
    finally:
        if os.path.exists(tmp): os.unlink(tmp)


def generate(m, directory, address):
    assert m['environment'] == 'stage' and m['mode'] == 'managed', 'generation enabled for stage only'
    assert re.fullmatch(r'https://[A-Za-z0-9.:-]+', address)
    out = Path(directory)
    out.mkdir(parents=True, exist_ok=True)
    base = '/etc/bcsd-secrets/' + m['environment'] + '-' + m['service']
    run = '/run/bcsd-secrets/' + m['environment'] + '-' + m['service']
    path = m['path'].replace('kv/', 'kv/data/', 1)
    template = '{{ with secret "' + path + '" }}{{ .Data | toJSON }}{{ end }}'
    hcl = '''vault {
 address = ADDRESS
 ca_cert = BASE_CA
 tls_server_name = "localhost"
}
auto_auth {
 method "cert" {
  mount_path = "auth/cert"
  config = {
   name = ROLE
   client_cert = CERT
   client_key = KEY
  }
 }
}
template_config {
 exit_on_retry_failure = true
 static_secret_render_interval = "1m"
}
template {
 contents = TEMPLATE
 destination = DEST
 perms = "0600"
 error_on_missing_key = true
}
'''
    for k, v in {'ADDRESS': address, 'BASE_CA':base+'/ca.crt','ROLE':'stage-'+m['service'], 'CERT':base+'/client.crt','KEY':base+'/client.key','TEMPLATE':template,'DEST':run+'/candidate.json'}.items():
        hcl = hcl.replace(k, json.dumps(v))
    (out/'agent.hcl').write_text(hcl)
    (out/'agent-once.hcl').write_text('exit_after_auth = true\n' + hcl)
    (out/'policy.hcl').write_text('path '+json.dumps(path)+' { capabilities = ["read"] }\n')
    (out/'agent.service').write_text("""[Unit]
Description=BCSD stage service secret agent
After=network-online.target
Wants=network-online.target
[Service]
Type=simple
UMask=0077
ExecStartPre=/usr/bin/install -d -m 0711 /run/bcsd-secrets
ExecStartPre=/usr/bin/install -d -m 0711 RUN
ExecStart=/opt/openbao/bin/bao agent -config=BASE/agent.hcl
Restart=on-failure
RestartSec=10
NoNewPrivileges=true
ProtectSystem=strict
ProtectHome=true
PrivateTmp=true
ReadWritePaths=/run/bcsd-secrets
[Install]
WantedBy=multi-user.target
""".replace('RUN',run).replace('BASE',base))
    atomic(out/'manifest.json', m)
    print(json.dumps({'service':m['service'],'manifestRevision':revision(m),'generated':True}))


def private_file(path):
    s = Path(path).lstat()
    assert stat.S_ISREG(s.st_mode) and not s.st_mode & 0o077, 'input must be private regular file'


def materialize(m, candidate, root, uid=None, gid=None):
    assert m['mode'] == 'managed' and m['environment'] == 'stage'
    private_file(candidate)
    assert Path(candidate).stat().st_size < 1024 * 1024
    body = json.loads(Path(candidate).read_text())
    version = body['metadata']['version']
    assert type(version) is int and version > 0
    assert not body['metadata'].get('destroyed') and not body['metadata'].get('deletion_time')
    data = body['data']
    # One KV object is a coherent release. No partial per-file activation.
    assert set(data) == {e['key'] for e in m['entries']}, 'key set mismatch'
    rendered = {}
    for e in m['entries']:
        value = data[e['key']]
        assert isinstance(value, str) and value and '\x00' not in value, 'invalid required value'
        if e['format'] == 'json':
            obj = json.loads(value)
            assert isinstance(obj, dict)
            if e['key'] == 'FCM_ADMIN_SDK_JSON':
                assert all(isinstance(obj.get(k),str) and obj[k] for k in ('private_key','client_email','project_id'))
        rendered[e['target']] = value
    root = Path(root)
    assert root.is_dir() and not root.is_symlink()
    assert not root.stat().st_mode & 0o022
    tmp = Path(tempfile.mkdtemp(prefix='release-', dir=root))
    try:
        for target, value in rendered.items():
            p = tmp/target
            p.parent.mkdir(mode=0o700, exist_ok=True)
            p.write_text(value)
            p.chmod(0o400)
        receipt = {'service':m['service'],'environment':m['environment'],'manifestRevision':revision(m),'renderedVersion':version,'renderedAt':now(),'keyCount':len(rendered)}
        atomic(tmp/'receipt.json', receipt, 0o400)
        if uid is not None:
            for p in [*tmp.rglob('*'),tmp]: os.chown(p, uid, gid if gid is not None else uid)
        # Return immutable directory; caller starts app with this exact path, never a moving symlink.
        print(json.dumps({'release':str(tmp),**receipt}))
        return tmp
    except BaseException:
        shutil.rmtree(tmp)
        raise


def main():
    p=argparse.ArgumentParser()
    p.add_argument('action', choices=['check','generate','materialize'])
    p.add_argument('manifest')
    p.add_argument('--output')
    p.add_argument('--address')
    p.add_argument('--candidate')
    p.add_argument('--uid', type=int)
    p.add_argument('--gid', type=int)
    a=p.parse_args()
    try:
        m=contract(a.manifest)
        if a.action=='generate': generate(m,a.output,a.address)
        elif a.action=='materialize': materialize(m,a.candidate,a.output,a.uid,a.gid)
        else: print(json.dumps({'valid':True,'service':m['service'],'manifestRevision':revision(m)}))
    except Exception:
        # Never include parser exception messages: they can contain secret input.
        print('Service secret validation failed; no release activated.',file=sys.stderr)
        sys.exit(1)

if __name__=='__main__': main()
