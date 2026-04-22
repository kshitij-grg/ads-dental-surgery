# ADS Dental Surgery - Smart Appointment & Billing API

## 🏥 Overview
A professional, secure, and containerized API-driven system designed to modernize dental clinic operations. This project integrates appointment scheduling, billing workflows, and smart waitlist management into a unified enterprise solution.

### **Problem Statement**
Modern dental clinics often struggle with fragmented manual processes, leading to scheduling conflicts, billing disputes, and inefficient patient waitlist management. This API provides a secure, reliable, and scalable solution to bridge these gaps.

---

## 🛠 Tech Stack
*   **Backend**: Java 21, Spring Boot 3.4.x
*   **Security**: Spring Security + JWT (JSON Web Tokens)
*   **Database**: MySQL 8.4
*   **DevOps & Infrastructure**: Docker, Kubernetes (Minikube/EKS), Kustomize
*   **CI/CD**: GitHub Actions
*   **Documentation**: Springdoc OpenAPI (Swagger UI)

---

## ✨ Features

### **MVP Core**
*   **Role-Based Security**: Secure endpoints for `ADMIN`, `DENTIST`, and `RECEPTIONIST`.
*   **Comprehensive APIs**: Full CRUD for Patients, Dentists, Treatments, and Appointments.
*   **Automated Billing**: Generates invoices only from `COMPLETED` appointments, enforcing strict business rules.
*   **Conflict Prevention**: Business logic to prevent overlapping appointments for dentists.

### **🚀 Standout Feature: Smart Waitlist Promotion**
The system includes a ranking service that suggests the best patient to promote from the waitlist when an appointment is cancelled. Suggestions are ranked based on:
*   Treatment compatibility
*   Time-window fit
*   Patient priority score

---

## 🏗 Architecture & Design
The project follows a clean, layered architecture (Controller -> Service -> Repository) with DTO-only boundaries to ensure data integrity and security.

### **Architecture & Design Artifacts**

#### **1. Domain Model Class Diagram**
*Visual representation of core entities (Patient, Dentist, Appointment, Bill) and their relationships.*
![Domain Model](images/domain-model-class-diagram.png)

#### **2. High-Level Logical Architecture**
*Shows the clean, layered separation of concerns (Controller -> Service -> Repository) and the security filter chain.*
![Logical Architecture](images/high-level-architecture-logical.png)

#### **3. Database ER Diagram**
*The MySQL schema design ensuring data normalization and referential integrity.*
![Database ERD](images/database-erd-mysql.png)

#### **4. Use-Case Diagram**
*Mapping system functionality to the ADMIN, DENTIST, and RECEPTIONIST roles.*

![Use-Case Diagram](images/use-case-diagram.png)

---

## 🤖 CI/CD Automation
This project features a fully automated delivery pipeline using **GitHub Actions**:
*   **Continuous Integration**: Automated Maven builds and test execution on every pull request.
*   **Continuous Deployment**: 
    *   Automatic Docker image building and versioning.
    *   Automatic push to Docker Hub (`kshitijgrg/ads-dental-surgery`).
    *   Versioning via `:latest` and `:sha-<commit-hash>` tags.

---

## 🚀 Quick Start (Local Demo)

### **Prerequisites**
*   Docker Desktop
*   Minikube & kubectl

### **One-Command Deployment**
To deploy the entire stack (Database + API) to your local Minikube cluster:

1.  **Start Minikube**:
    ```bash
    minikube start
    ```
2.  **Deploy Manifests**:
    ```bash
    kubectl apply -k k8s/overlays/dev
    ```
3.  **Open the Service**:
    ```bash
    minikube service ads-api -n ads-dental-surgery
    ```

*Note: For the best experience, visit `{URL}/swagger-ui/index.html` once the service opens to interact with the API.*
