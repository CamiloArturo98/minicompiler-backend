# MiniCompiler Backend

A production-ready backend API for a mini compiler and virtual machine built with Java and Spring Boot.

This project provides a complete compilation pipeline including lexical analysis, parsing, optimization, bytecode generation, and execution through a custom virtual machine. It also includes REST APIs for compilation workflows, snippets management, statistics, AI-assisted interactions, and health monitoring.

---

## Overview

MiniCompiler Backend was designed to simulate core compiler architecture concepts while following modern backend engineering practices.

The system includes:

* Lexical analyzer (Lexer)
* Parser and AST generation
* Intermediate optimization layer
* Bytecode generation
* Custom virtual machine execution
* RESTful API architecture
* Snippet persistence and compilation history
* AI-assisted compiler interaction
* Health monitoring and statistics endpoints

---

## Tech Stack

| Technology      | Purpose                      |
| --------------- | ---------------------------- |
| Java 21         | Core language                |
| Spring Boot 3   | Backend framework            |
| Spring Web      | REST APIs                    |
| Spring Data JPA | Database access              |
| PostgreSQL      | Persistent storage           |
| H2 Database     | Local runtime/testing        |
| Maven           | Dependency management        |
| Docker          | Containerization             |
| Lombok          | Boilerplate reduction        |
| Actuator        | Monitoring and health checks |

---

## Architecture

The project follows a layered and modular architecture:

```text
Controller Layer
    ↓
Service Layer
    ↓
Compiler Engine
    ├── Lexer
    ├── Parser
    ├── Optimizer
    ├── Code Generator
    └── Virtual Machine
    ↓
Persistence Layer
```

Main modules:

```text
src/main/java/com/minicompiler
├── compiler
│   ├── lexer
│   ├── parser
│   ├── ast
│   ├── optimizer
│   ├── codegen
│   └── vm
├── controller
├── service
├── domain
│   ├── entity
│   └── repository
├── dto
├── config
└── exception
```

---

## Features

### Compiler Engine

* Token generation through a custom lexer
* Syntax parsing and AST creation
* Bytecode generation using custom opcodes
* Virtual machine execution engine
* Compilation optimization support

### REST API

* Compile source code through HTTP requests
* Retrieve example programs
* Tokenize source code
* Compare optimization results
* Store compilation sessions
* Manage reusable code snippets
* Access compilation statistics

### AI Integration

* AI chat endpoint support
* Chat history persistence
* External AI provider integration through environment variables

### Infrastructure

* Docker support
* Render deployment configuration
* Health monitoring endpoints
* Global exception handling
* Configurable CORS

---

## API Endpoints

### Compiler

| Method | Endpoint                 | Description               |
| ------ | ------------------------ | ------------------------- |
| POST   | `/api/compiler/compile`  | Compile and execute code  |
| GET    | `/api/compiler/examples` | Retrieve example programs |
| GET    | `/api/compiler/health`   | Compiler health check     |

### Tokenizer

| Method | Endpoint                  | Description                      |
| ------ | ------------------------- | -------------------------------- |
| POST   | `/api/tokenizer/tokenize` | Generate tokens from source code |

### Optimizer

| Method | Endpoint                 | Description                             |
| ------ | ------------------------ | --------------------------------------- |
| POST   | `/api/optimizer/compare` | Compare optimized vs non-optimized code |

### AI

| Method | Endpoint               | Description                      |
| ------ | ---------------------- | -------------------------------- |
| POST   | `/api/ai/chat`         | AI-assisted compiler interaction |
| GET    | `/api/ai/history`      | Retrieve AI chat history         |
| DELETE | `/api/ai/history/{id}` | Delete a chat history item       |
| DELETE | `/api/ai/history`      | Clear all AI history             |

### Health

| Method | Endpoint           | Description            |
| ------ | ------------------ | ---------------------- |
| GET    | `/api/health/ping` | API availability check |

---

## Getting Started

### Prerequisites

* Java 21+
* Maven 3.9+
* PostgreSQL
* Docker (optional)

---

## Environment Variables

Create an `.env` file or configure the following variables:

```env
PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/minicompiler
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

OPENROUTER_API_KEY=your_api_key

CORS_ALLOWED_ORIGINS=http://localhost:4200
```

---

## Installation

### Clone the Repository

```bash
git clone https://github.com/your-username/minicompiler-backend.git
cd minicompiler-backend
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

The server will start on:

```text
http://localhost:8080
```

---

## Docker

### Build Docker Image

```bash
docker build -t minicompiler-backend .
```

### Run Container

```bash
docker run -p 8080:8080 minicompiler-backend
```

---

## Example Request

### Compile Code

```http
POST /api/compiler/compile
Content-Type: application/json
```

Request body:

```json
{
  "code": "x = 5 + 3"
}
```

Example response:

```json
{
  "success": true,
  "output": "8",
  "executionTime": 12
}
```

---

## Engineering Highlights

* Clean layered architecture
* Separation of concerns
* Compiler-oriented system design
* Custom virtual machine implementation
* RESTful API best practices
* Scalable service structure
* Environment-based configuration
* Production deployment readiness

---

## Deployment

The project includes:

* `Dockerfile` for containerized deployments
* `render.yaml` configuration for Render hosting
* Spring Boot Actuator monitoring

---

## Future Improvements

Potential enhancements:

* Authentication and authorization
* Real-time compilation streaming
* More advanced optimization passes
* Extended language grammar support
* WebSocket-based execution logs
* Unit and integration test expansion
* CI/CD pipeline automation

---

## License

This project is intended for educational and portfolio purposes.

---

## Author

Developed by Camilo.
