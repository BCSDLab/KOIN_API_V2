# Candidate only; not installed. Exact KV data paths, no list/write/admin access.
path "kv/data/prod/koin-api/database" {
  capabilities = ["read"]
}
path "kv/data/prod/koin-api/auth" {
  capabilities = ["read"]
}
path "kv/data/prod/koin-api/firebase" {
  capabilities = ["read"]
}
path "kv/data/prod/koin-api/payment" {
  capabilities = ["read"]
}
path "kv/data/prod/koin-api/integrations" {
  capabilities = ["read"]
}
