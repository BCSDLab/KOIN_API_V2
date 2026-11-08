#!/usr/bin/env python3
"""Offline check for the non-deployable prod proposal; never access secrets."""
import sys
import tempfile
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from runtime import contract, generate

root = Path(__file__).resolve().parent
manifest = contract(root / 'manifest.json')
assert manifest['environment'] == 'prod' and manifest['mode'] == 'managed'
expected = '# Candidate only; not installed. Exact KV data paths, no list/write/admin access.\n'
for source in manifest['sources'].values():
    expected += 'path "' + source['path'].replace('kv/', 'kv/data/', 1) + '" {\n  capabilities = ["read"]\n}\n'
assert (root / 'read-policy.hcl').read_text() == expected
print('PASS: prod managed contract and exact read policy; no external calls')
