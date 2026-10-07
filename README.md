# Shehia Management System API

A backend API for managing community and administrative workflows in a Shehia management system. This project was developed collaboratively by a small cross-functional team:

- Backend Developer: API development, business logic, security, database integration
- Analyst: requirements, workflows, business rules, reporting logic
- Designer: user-facing templates, assets, and visual consistency

The application is built with Java and Spring Boot, and it supports resident and staff management, issue reporting, announcements, letter generation, authentication, and dashboard analytics.

## Overview

This system helps manage essential operational activities such as:

- User authentication and authorization
- Resident registration and profile management
- Staff account and role management
- Issue reporting and status tracking
- Announcement publishing and retrieval
- Letter generation and PDF export
- Dashboard statistics and activity overview

## Tech Stack

- Java 21
- Spring Boot 3.3.3
- Spring Web
- Spring Data JPA
- Spring Security
- MySQL Database
- JWT Authentication
- Thymeleaf
- OpenHTMLToPDF
- Maven

## Project Structure

```text
backend_api_final/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/shehia_management/api/
│   │   │   ├── announcement/
│   │   │   ├── auth/
│   │   │   ├── dashboard/
│   │   │   ├── identity/
│   │   │   ├── issue/
│   │   │   ├── letter/
│   │   │   ├── resident/
│   │   │   ├── shared/
│   │   │   ├── staff/
│   │   │   └── ApiApplication.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   ├── static/
│   │   │   └── templates/
│   │   │       └── letters/
│   └── test/
├── uploads/
│   └── issue-photos/
├── .gitignore
├── .gitattributes
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src backend.zip
└── README.md
```

## Main Modules

### 1. Authentication and Security
The application provides secure login, token-based authorization, and protected endpoints using Spring Security and JWT.

### 2. Resident Management
Handles resident information, profile records, public statistics, and resident-related access flows.

### 3. Staff Management
Includes staff registration, assignment, account handling, zone-based assignment, and staff administration features.

### 4. Issue Reporting
Supports issue tracking, category and priority assignment, and file-based issue uploads such as photos.

### 5. Letters and PDF Generation
Generates official community letters such as residence, conduct, event, permit, guarantor, and bank-related letters using HTML templates and PDF export.

### 6. Announcements
Provides announcement creation and retrieval for admin/public access, including status and type handling.

### 7. Dashboard
Contains summary metrics and activity insights for administrative monitoring.

## Prerequisites

Before running this project, make sure you have the following installed:

- Java 21 or later
- Maven
- MySQL database server
- Git

## Configuration

The application configuration is stored in:

```properties
src/main/resources/application.properties
```

Update the database connection values according to your local environment:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shehia_db
spring.datasource.username=root
spring.datasource.password=your_password
```

Make sure the database exists before starting the application.

## Run the Project

### Using Maven Wrapper

For Linux/macOS:

```bash
./mvnw spring-boot:run
```

For Windows:

```bash
mvnw.cmd spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

## Default Setup Notes

- The project uses `spring.jpa.hibernate.ddl-auto=update`
- File upload size is configured for up to 10MB
- JWT settings are configured in the application properties file

## Collaboration Notes

This project reflects collaborative work between three key roles:

- Backend Developer: implemented the API architecture, database models, controllers, and logic
- Analyst: defined business requirements and validated workflows
- Designer: prepared templates, assets, and visual structure for a more polished user experience

This team-based structure allows the system to combine technical functionality with business alignment and user-focused design.

## Future Improvements

Possible future enhancements include:

- API documentation with Swagger/OpenAPI
- Notification system for issue status updates
- Reporting exports in Excel/PDF
- Better validation for all modules
- UI integration with frontend client
- Automated testing coverage for critical services

## Contact

For questions or collaboration requests, please contact the project maintainer or repository owner.

---

This README was written to explain the project clearly for GitHub and team collaboration purposes.
