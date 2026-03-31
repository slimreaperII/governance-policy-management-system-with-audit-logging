# Governance Policy Management System with Audit Logging

**Overview**

This project is a microservices-based system designed to manage policies and records governance actions for audit purposes.

It consists of two independent services:

* **Governance Service**→ Handles policy creation and lifecycle management
* **Audit Service** → Listens to events and stores them for auditability

Communication between services uses event-driven architecture using Kafka, ensuring loose coupling and scalability.

**Built With**
* Spring Boot – Backend framework  
* PostgreSQL – Data persistence  
* Apache Kafka – Event messaging  
* Docker – Containerized infrastructure  

### Features
*   Create and manage governance policies
*   Submit policies for approval
*   Approve or reject policies
*   Publish governance events when policy actions occur
*   Persist policies and audit records in the database

**Workflow**

  ```text
  Client Request
       ↓ 
  Governance Service (REST API)
       ↓
  Save Policy in PostgreSQL
       ↓
  Publish Event to Kafka
       ↓
  Audit Service consumes event
       ↓
  Store audit log in database
  ```

## Prerequisites & Dependencies

To set up and run this project, you will need the following installed on your system:

*   Docker & Docker compose
*   Java (JDK 25/26)
*   maven 

## Installation & Setup Instructions

+ run docker conatiner
  
  ```bash
   cd governance-policy-management-system-with-audit-logging
   docker compose up -d
  ```
+ run backend services
   
  ```bash 
   cd governance-service
   ./mvnw.cmd spring-boot:run
  ```
  ```bash
   cd audit-service
   ./mvnw.cmd spring-boot:run
  ```


## API Endpoints

* Create Policy
  
    `POST /api/policies`

* Get all policies
  
    `GET /api/policies`

* Get policy by id

    `GET /api/policies/{id}`
  
* Submit policy for approval
  
    `POST /api/policies/{id}/submit`
  
* Approve policy

    `POST /api/policies/{id}/approve`

* Reject policy

    `POST /api/policies/{id}/reject`


## Note

- PostgreSQL and Kafka are fully containerized using Docker. No local installation is required
- Databases for both servies are created automatically upon running docker compose file
