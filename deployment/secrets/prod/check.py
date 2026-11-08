#!/usr/bin/env python3
"""Offline check for the non-deployable prod proposal; never access secrets."""
import sys
import tempfile
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from runtime import contract, generate

root = Path(__file__).resolve().parent
manifest = contract(root / 'manifest.json')
assert manifest['environment'] == 'prod' and manifest['mode'] == 'observe'
expected = '# Candidate only; not installed. Exact KV data paths, no list/write/admin access.\n'
for source in manifest['sources'].values():
    expected += 'path "' + source['path'].replace('kv/', 'kv/data/', 1) + '" {\n  capabilities = ["read"]\n}\n'
assert (root / 'read-policy.hcl').read_text() == expected
with tempfile.TemporaryDirectory() as directory:
    try:
        generate(manifest, directory, 'https://invalid.example', {})
    except AssertionError:
        assert not list(Path(directory).iterdir())
    else:
        raise AssertionError('prod generation must remain disabled')
print('PASS: prod observe contract, exact read policy, generation blocked; no external calls')
