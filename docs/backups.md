# Backups

This is a transient API with no database, no data passing through the API is persisted.

Only the following is persisted:

- Mutual TLS certificate authority - S3 (versioning enabled)
- API container Images - ECR (versioning enabled)
