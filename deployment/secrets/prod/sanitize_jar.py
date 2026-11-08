#!/usr/bin/env python3
"""Create a prod-only artifact; preserve classes/libraries and remove secret resources."""
import argparse
import json
from pathlib import Path
import re
import zipfile
from runtime import contract


def sanitize(manifest, source, destination):
    assert manifest['environment']=='prod' and manifest['mode']=='managed'
    assert Path(source).resolve()!=Path(destination).resolve()
    keys={e['key'] for e in manifest['entries']}
    removed=0
    with zipfile.ZipFile(source) as src, zipfile.ZipFile(destination,'w') as dst:
        assert 'org/springframework/boot/loader/PropertiesLauncher.class' in src.namelist()
        assert not any(re.match(r'META-INF/.*\.(SF|RSA|DSA)$', n) for n in src.namelist()), 'signed JAR requires a separate build'
        for info in src.infolist():
            if info.filename=='BOOT-INF/classes/koin-firebase-adminsdk.json':
                removed+=1;continue
            b=src.read(info.filename)
            if re.fullmatch(r'BOOT-INF/classes/application(?:-[\w-]+)?\.ya?ml',info.filename):
                lines=b.decode().splitlines(keepends=True)
                kept=[]
                for line in lines:
                    variables=set(re.findall(r'\$\{([A-Z][A-Z0-9_]*)',line))
                    if variables & keys:
                        # Only known simple leaf placeholders may be removed; arbitrary YAML is rejected.
                        assert re.fullmatch(r'\s*[\w.-]+:\s*[\"\']?\$\{[A-Z][A-Z0-9_]*(?::[^\n]*)?\}[\"\']?\s*',line)
                        removed+=1
                    else:kept.append(line)
                b=''.join(kept).encode()
            dst.writestr(info,b)
    with zipfile.ZipFile(source) as src, zipfile.ZipFile(destination) as dst:
        assert 'BOOT-INF/classes/koin-firebase-adminsdk.json' not in dst.namelist()
        for n in src.namelist():
            if n.endswith(('.class','.jar')): assert src.read(n)==dst.read(n), 'business code changed'
    return removed

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('manifest');p.add_argument('source');p.add_argument('destination');a=p.parse_args()
    try:print(json.dumps({'removedResourcesOrLines':sanitize(contract(a.manifest),a.source,a.destination),'businessCodeUnchanged':True}))
    except Exception:
        Path(a.destination).unlink(missing_ok=True)
        raise SystemExit('Artifact validation failed; no candidate retained.')
