# Staging external secrets

This adapter is scoped to develop deployment only. Production workflow is unchanged.
The stage artifact omits Firebase JSON and secret environment-placeholder leaves. The stage
host starts Spring Boot PropertiesLauncher with an external, read-only classpath resource
and configtree produced by its service-specific OpenBao Agent. No OpenBao token enters CI
or the application. The removed Firebase build step must not be reintroduced.

Tools are a vendored snapshot of BCSDLab/bcsdpdevops host/secrets, 2026-11-08. Keep the
manifest and tooling in sync through a reviewed PR. Runtime tooling, access controls,
startup gate, receipt verification and rollback belong to the DevOps repository.

Do not merge this workflow change before the staging host deployment wrapper is installed.
A sanitized JAR cannot use the old `.env.dev` / `java -jar` startup path. The production
workflow and main branch keep their current behavior until separately authorized.
