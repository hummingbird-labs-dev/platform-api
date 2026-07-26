# Platform API

A private Spring Boot API deployed by the Hummingbird Labs platform GitOps
repository.

## Local development

The project requires Java 21. Start the API with:

```sh
./gradlew bootRun
```

The API listens on port 8080:

```sh
curl http://localhost:8080/hello
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

Run the test suite with:

```sh
./gradlew check
```

Build and run the container locally:

```sh
docker build --tag platform-api:local .
docker run --rm --read-only --tmpfs /tmp -p 8080:8080 platform-api:local
```

## Image delivery and GitOps promotion

GitHub Actions tests every pull request. Pushes to `main` publish a
commit-SHA-tagged image to `ghcr.io/hummingbird-labs-dev/platform-api`; version
tags such as `v0.1.0` publish a matching immutable version tag.

After a version image exists, update
`platform/deployments/platform-api/kustomization.yaml` in the `platform`
repository to the selected tag and add the workload to
`platform/deployments/kustomization.yaml` in the same reviewed change. Flux
will then reconcile the private Kubernetes workload.