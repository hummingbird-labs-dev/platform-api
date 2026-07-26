# Platform API

A versioned control-plane API for the Hummingbird Labs platform, providing operational visibility into **images**, **workloads**, **edge ingress**, and **system health**.

```
Platform API → Image Registry, Kubernetes, Edge Server, Observability Stack
                            ↓
                    Single Source of Truth
                            ↓
        hummingbirdctl CLI, Automation, GitOps Reconciliation
```

## Overview

The Platform API serves as the central control plane for the Hummingbird Labs lab platform. It exposes resource discovery, operational status, and platform health through a RESTful API organized by responsibility, not technology.

### Core Capabilities

- **Images**: Browse container images and metadata from `registry.lan.hummingbirdlabs.dev`
- **Workloads**: Discover running applications, services, and cluster infrastructure
- **Edge**: Monitor internet-facing ingress, reverse proxy routes, and TLS status
- **Health**: Check platform readiness and component status

### Guiding Principles

- **Responsibility-based organization**: API sections organized by operational responsibility (Images, Workloads, Edge, Health), not by technology choices
- **Single source of truth**: Authoritative view of what's deployed and its operational state
- **Discoverability**: Enables CLI tools to query and understand platform resources
- **Extensibility**: Foundation for future policy enforcement and approved platform changes
- **Boring technology**: Standard REST conventions, proven patterns, maintainable code

## Architecture

### API Structure

The API uses URL versioning and responsibility-based organization:

```
/api/v1/
├── /images              - Container artifacts
├── /workloads           - Application orchestration
├── /edge                - Internet-facing ingress & reverse proxy
└── /health              - Platform operational state
```

### Resource Categories

#### Images (Container Artifact Management)
Sourced from `registry.lan.hummingbirdlabs.dev` (Docker Registry V2 API)

- **List repositories** with available tags
- **Query image metadata** (digest, size, architecture, creation time)
- **Browse artifacts** available for deployment

**Example**: What version of `platform-api` is available? What images are in the registry?

#### Workloads (Application Orchestration & Runtime)
Sourced from Kubernetes API

- **Deployments**: Running applications and their replica status
- **Pods**: Individual container instances, phase, resource usage
- **Services**: Network exposure and endpoints
- **Nodes**: Cluster infrastructure and capacity
- **Namespaces**: Logical resource grouping
- **Storage**: PersistentVolumeClaims and their status

**Example**: What applications are deployed? How many replicas? What resources are they using?

#### Edge (Internet-Facing Ingress & Reverse Proxy)
Sourced from Edge server (Caddy) configuration and health checks

- **Server status**: Operational state and uptime
- **Routes/Destinations**: Available hostnames and their upstreams
- **Health**: Per-route destination health status
- **TLS**: Certificate status and auto-renewal
- **Metrics**: Request rates and latencies (future)

**Example**: What hosts are exposed via the edge? Which destinations are healthy?

#### Health (Platform Operational State)
Sourced from component health checks and integration probes

- **Platform readiness**: Overall status and operational state
- **Component health**: Individual system component status
- **Probes**: Readiness and liveness checks
- **Integration status**: Connectivity to external systems

**Example**: Is the platform ready? Is the registry accessible? Are all edge routes healthy?

### Request/Response Format

All responses follow Kubernetes API conventions:

```json
{
  "apiVersion": "v1",
  "kind": "DeploymentList",
  "metadata": {
    "resourceVersion": "12345",
    "timestamp": "2026-07-26T14:38:47Z"
  },
  "items": [...]
}
```

Error responses:

```json
{
  "apiVersion": "v1",
  "kind": "Status",
  "status": "Failure",
  "message": "Deployment not found",
  "reason": "NotFound",
  "code": 404
}
```

## API Reference

### Images API

#### List all image repositories
```bash
curl http://localhost:8080/api/v1/images/repositories?limit=50
```

#### Get repository details (with tags)
```bash
curl http://localhost:8080/api/v1/images/repositories/platform-api
```

#### Get image metadata
```bash
curl http://localhost:8080/api/v1/images/repositories/platform-api/v0.1.0
```

### Workloads API

#### List all deployments
```bash
curl http://localhost:8080/api/v1/workloads/deployments?namespace=platform
```

#### List pods in a namespace
```bash
curl http://localhost:8080/api/v1/workloads/namespaces/platform/pods
```

#### List all nodes in the cluster
```bash
curl http://localhost:8080/api/v1/workloads/nodes
```

#### Filter by labels
```bash
curl "http://localhost:8080/api/v1/workloads/pods?labels=app=platform-api"
```

### Edge API

#### Get edge server status
```bash
curl http://localhost:8080/api/v1/edge/status
```

#### List all routes
```bash
curl http://localhost:8080/api/v1/edge/routes
```

#### Get route details
```bash
curl http://localhost:8080/api/v1/edge/routes/registry.lan.hummingbirdlabs.dev
```

### Health API

#### Get overall platform health
```bash
curl http://localhost:8080/api/v1/health
```

#### Get component health
```bash
curl http://localhost:8080/api/v1/health/components
```

#### Check specific component
```bash
curl http://localhost:8080/api/v1/health/workloads
```

## Query Parameters

All list endpoints support:

- **`limit`** (default: 50, max: 500) - Pagination limit
- **`offset`** (default: 0) - Pagination offset
- **`labels`** - Kubernetes label selector (e.g., `app=platform-api,environment=platform`)
- **`namespace`** - Filter by Kubernetes namespace
- **`fields`** - Field projection (e.g., `name,status.phase`)
- **`status`** - Filter by health status (e.g., `healthy`, `unhealthy`)

### Examples

```bash
# Get first 20 pods in the platform namespace
curl "http://localhost:8080/api/v1/workloads/pods?namespace=platform&limit=20&offset=0"

# Get deployments with a specific label
curl "http://localhost:8080/api/v1/workloads/deployments?labels=app=platform-api"

# Get only name and status fields
curl "http://localhost:8080/api/v1/workloads/deployments?fields=name,status.phase"

# Find unhealthy routes
curl "http://localhost:8080/api/v1/edge/routes?status=unhealthy"
```

## Integration with hummingbirdctl

The Platform API is the foundation for the `hummingbirdctl` CLI. The CLI queries this API to provide:

| Command | Purpose | API Endpoint |
|---------|---------|---|
| `hummingbirdctl list` | See all resources | `/api/v1/*` |
| `hummingbirdctl images` | Browse container images | `/api/v1/images/*` |
| `hummingbirdctl apps` | Show running applications | `/api/v1/workloads/deployments` |
| `hummingbirdctl nodes` | List cluster nodes | `/api/v1/workloads/nodes` |
| `hummingbirdctl routes` | Show available routes | `/api/v1/edge/routes` |
| `hummingbirdctl edge status` | Check edge ingress | `/api/v1/edge/status` |
| `hummingbirdctl health` | Platform health check | `/api/v1/health` |
| `hummingbirdctl diagnose` | Comprehensive diagnosis | `/api/v1/health/*` |

## Local Development

### Prerequisites

- Java 21
- Docker (for building container)
- Access to:
  - Kubernetes cluster (for workload API)
  - Container registry at `registry.lan.hummingbirdlabs.dev` (for images API)
  - Edge server via admin API (for edge API)

### Start the API

```bash
./gradlew bootRun
```

The API listens on port 8080:

```bash
# Test the API
curl http://localhost:8080/api/v1/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

### OpenAPI/Swagger Documentation

Access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

Or fetch the OpenAPI spec:
```bash
curl http://localhost:8080/v3/api-docs
```

### Run Tests

```bash
./gradlew check
```

Tests include:
- Unit tests for data models and controllers
- Integration tests with mock clients
- API contract tests

### Build Docker Image

```bash
docker build --tag platform-api:local .
docker run --rm --read-only --tmpfs /tmp -p 8080:8080 platform-api:local
```

## Deployment

### Image Publishing

GitHub Actions tests every pull request. Pushes to `main` publish a commit-SHA-tagged image to:
```
ghcr.io/hummingbird-labs-dev/platform-api
```

Version tags (e.g., `v0.1.0`) publish immutable versioned images.

### GitOps Promotion

To deploy a new version:

1. Tag and push a release:
   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   ```

2. Update the `platform` repository:
   - Edit `platform/deployments/platform-api/kustomization.yaml`
   - Change image tag to `v0.2.0`
   - Add the workload to `platform/deployments/kustomization.yaml`
   - Submit PR for review

3. Flux reconciles the change to the private Kubernetes cluster

### Configuration

Environment variables (set in Kubernetes deployment):

- `KUBERNETES_SERVICE_HOST` - Kubernetes API host
- `KUBERNETES_SERVICE_PORT` - Kubernetes API port
- `REGISTRY_URL` - Container registry URL (default: `registry.lan.hummingbirdlabs.dev`)
- `EDGE_ADMIN_URL` - Edge server admin API URL
- `CACHE_TTL_SECONDS` - Cache TTL (default: 60)

## Future Enhancements

### Immediate Roadmap

- [ ] Implement client libraries for Kubernetes, registry, and edge
- [ ] Add real-time updates via Server-Sent Events (`?watch=true`)
- [ ] Implement request caching layer for performance
- [ ] Add Prometheus metrics endpoint
- [ ] Support approved platform changes (mutations)

### Long-Term Vision

- [ ] Policy enforcement for approved platform changes
- [ ] Audit trail and change history
- [ ] Advanced filtering and search capabilities
- [ ] Metrics and time-series data integration
- [ ] Multi-cluster support
- [ ] Declarative desired-state comparison

## Architecture Decision Records

See [`architecture`](https://github.com/hummingbird-labs-dev/architecture) repository for:
- Platform API design rationale
- Integration with other platform components
- Data flow diagrams
- Operational model

## Contributing

See `.github/` for contribution guidelines.

When modifying the API:
1. Update endpoint specs in design documentation
2. Update data models and controllers
3. Add tests for new endpoints
4. Update README with new capabilities
5. Ensure backwards compatibility

## Support & Troubleshooting

### "Connection refused" to Kubernetes

Verify Kubernetes API connectivity:
```bash
curl http://localhost:8080/api/v1/workloads/nodes
```

### "Registry unreachable"

Check registry connectivity:
```bash
curl http://localhost:8080/api/v1/health/images
```

### Performance issues

Check cache status and consider:
- Increasing cache TTL
- Implementing result pagination
- Using field projection to reduce response size

## License

[License information from parent org](https://github.com/hummingbird-labs-dev)

---

**Built at home. Operated with care. Always evolving.**

For more on the Hummingbird Labs platform, visit [hummingbirdlabs.dev](https://hummingbirdlabs.dev)
