# ads-dental-surgery

Final project for the ADS dental surgery domain.

## Overview
Build a Smart Appointment and Billing API for the ADS dental surgery domain using Spring Boot, JWT auth, DTO-based contracts, tests, Docker, Kubernetes, MySQL, and EKS deployment.

## Problem Statement
Dental clinics often run separate, manual processes for appointment scheduling and billing. This causes missed appointments, longer patient wait times, billing disputes, and poor visibility for office staff.

The clinic needs one secure API-driven system that can manage appointments, manage billing, enforce business rules between scheduling and billing, and support reliable deployment in containerized and cloud environments.

## Feature Scope

### MVP Features
1. Authentication and authorization
- JWT login endpoint.
- Roles: ADMIN, DENTIST, RECEPTIONIST.
- Protected endpoints by role.

2. Core APIs
- Patients: create, read, update.
- Dentists: create, read, update.
- Treatments: create, read, update, list.
- Appointments: create, reschedule, cancel, list.
- Bills: generate from completed appointment, get status, mark paid.

3. Business rules
- Prevent overlapping appointments per dentist.
- Status flow: SCHEDULED -> COMPLETED or CANCELLED.
- Allow bill generation only for COMPLETED appointments.
- Enforce one bill per appointment.

4. Quality
- DTO-only controller boundaries.
- Unified API error model.
- Unit tests for service rules.
- Integration tests for auth + appointment + billing flow.

### Standout Feature
Smart waitlist promotion suggestions:
- Triggered after cancellation.
- Ranked by treatment fit, time-window fit, and priority.
- Includes explanation fields in response for transparent decisions.

### Non-Goals
- Full email/SMS notification platform.
- Real-time frontend app.
- Insurance adjudication complexity.
- Multi-clinic tenancy.

## Requirements / Use Cases
The solution supports the main presentation and grading requirements:
- Login and JWT-based authentication.
- Role-based access for ADMIN, DENTIST, and RECEPTIONIST.
- Patient, dentist, treatment, appointment, and billing workflows.
- Appointment rescheduling, cancellation, completion, and bill generation rules.
- Smart waitlist promotion after cancellations.

## Diagrams

### Domain Model
This class diagram shows the core entities and their relationships across patients, dentists, treatments, appointments, bills, and waitlist entries.

![Domain model class diagram](images/domain-model-class-diagram.png)

### High-Level Architecture
The first diagram shows the logical application layers and service flow.

![Logical architecture diagram](images/high-level-architecture-logical.png)

The second diagram shows how the application is deployed in EKS with the MySQL database and image registry.

![Deployment architecture diagram](images/high-level-architecture-deployment-eks.png)

### Database ER Diagram
This ER diagram shows the persisted entities and the relationships used by the MySQL schema.

![Database ER diagram](images/database-erd-mysql.png)

### Use-Case Diagram
This use-case diagram maps the project scope to the main actors and their actions.

![Use-case diagram](images/use-case-diagram.png)

## Dockerization

Build the application image:

```bash
docker build -t ads-dental-surgery:latest .
```

Run the app and MySQL together with Docker Compose:

```bash
docker compose up --build
```

The compose setup exposes the API on `http://localhost:8080` and uses the MySQL service defined in `docker-compose.yml`. The application reads its runtime settings from environment variables, including `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT`, and `JWT_SECRET`.

### Image Publishing Convention

For branch-to-branch delivery (especially before Kubernetes work), publish images from the feature branch with both stable and immutable tags:

```bash
BRANCH_TAG=feature-dockerization
SHA_TAG=sha-$(git rev-parse --short HEAD)

docker build -t ads-dental-surgery:${BRANCH_TAG} .
docker tag ads-dental-surgery:${BRANCH_TAG} kshitijgrg/ads-dental-surgery:${BRANCH_TAG}
docker tag ads-dental-surgery:${BRANCH_TAG} kshitijgrg/ads-dental-surgery:${SHA_TAG}
docker tag ads-dental-surgery:${BRANCH_TAG} kshitijgrg/ads-dental-surgery:latest

docker push kshitijgrg/ads-dental-surgery:${BRANCH_TAG}
docker push kshitijgrg/ads-dental-surgery:${SHA_TAG}
docker push kshitijgrg/ads-dental-surgery:latest
```

Use `kshitijgrg/ads-dental-surgery:sha-<commit>` in Kubernetes manifests for reproducible deployments.

## Kubernetes Manifests

This repository uses a base plus overlays structure:
- `k8s/base`: common resources for namespace, config, storage, MySQL, and API.
- `k8s/overlays/dev`: development-ready overlay with LoadBalancer service and image tag pinning.
- `k8s/overlays/eks`: EKS-oriented overlay with ALB ingress.

Apply development overlay:

```bash
kubectl apply -k k8s/overlays/dev
```

Apply EKS overlay:

```bash
kubectl apply -k k8s/overlays/eks
```

Check rollout status:

```bash
kubectl -n ads-dental-surgery rollout status deployment/ads-mysql
kubectl -n ads-dental-surgery rollout status deployment/ads-api
```

Before applying to a real cluster, update overlay secrets with secure values:
- `k8s/overlays/dev/secrets.yaml`
- `k8s/overlays/eks/secrets.yaml`

For the full AWS deployment flow, see [docs/11-eks-deployment.md](docs/11-eks-deployment.md).

## Tech Stack
- Java 21
- Spring Boot 3.3.x
- Spring Security and JWT
- MySQL 8.4
- Docker
- Kubernetes
- AWS EKS
