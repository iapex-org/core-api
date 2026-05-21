# IAPEX — Core REST API

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/MongoDB-7.0-47A248?logo=mongodb&logoColor=white" alt="MongoDB">
  <img src="https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black" alt="Swagger">
</p>

<p align="center">
  <em>Central REST API for patient management, authentication, and institutional operations in the IAPEX ecosystem.</em>
</p>

<p align="center">
  <a href="https://github.com/iapex-org/core-api">Repository</a>
  ·
  <a href="https://github.com/iapex-org/core-api/issues">Report Bug</a>
  ·
  <a href="https://virtual.cuautitlan.unam.mx/intar/wp-content/uploads/sites/19/2025/12/166-A-Hybrid-Artificial-Intelligent-System-for-Missing-JORGE-CHRISTIAN-SERRANO-PUERTOS.pdf">Research Paper</a>
</p>

<p align="center">
  <a href="README.md">🇬🇧 English</a> · <a href="README.es.md">🇪🇸 Español</a>
</p>

---

## About IAPEX

**IAPEX** (Hybrid AI for Missing Patient Identification) is a validated system that helps healthcare institutions identify and manage unidentified or missing patients through a fusion of facial recognition and textual analysis.

This repository contains the **Core REST API** — the central backend service managing:

- User authentication and authorization (JWT + RBAC)
- Patient and institution CRUD operations
- Contact request processing
- Email notifications
- File and media management
- Integration with the AI search engine

### Ecosystem

| Component | Repository | Stack |
|-----------|-----------|-------|
| **Core API** (this) | [iapex-org/core-api](https://github.com/iapex-org/core-api) | Spring Boot 3, PostgreSQL, MongoDB |
| **Web Portal** | [iapex-org/web-app](https://github.com/iapex-org/web-app) | Angular 19, Bootstrap, TypeScript |
| **Mobile App** | [iapex-org/mobile-app](https://github.com/iapex-org/mobile-app) | React 18, Ionic 8, Capacitor |

## Features

- **JWT Authentication** — Secure token-based auth with refresh tokens
- **Role-Based Access Control** — Granular permissions for different user roles
- **Patient Management** — Full CRUD for patient records and morphological data
- **Institution Management** — Healthcare institution profiles and memberships
- **Contact Requests** — Process family inquiries about potential matches
- **Email Notifications** — SMTP-based institutional notifications
- **File Management** — Secure media upload and storage
- **Swagger Documentation** — Interactive API docs at `/swagger-ui.html`
- **Caching** — Performance optimization with Caffeine cache
- **PostgreSQL + MongoDB** — Relational data with NoSQL for encodings/cache

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- MongoDB 6.0+

### Setup

```bash
git clone https://github.com/iapex-org/core-api.git
cd core-api
```

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Configure your environment variables:

```env
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/iapex
SPRING_DATASOURCE_PASSWORD=your_db_password
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_email_password
```

Build and run:

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

API documentation: `http://localhost:8080/swagger-ui.html`

## Architecture

```
┌─────────────────────────────────────────────────┐
│                  Core API                        │
│               Spring Boot 3.3                     │
│                                                   │
│  ┌──────────┐ ┌──────────┐ ┌────────────────┐   │
│  │  Auth    │ │ Patient  │ │  Institution    │   │
│  │  Module  │ │  Module  │ │  Module         │   │
│  └────┬─────┘ └────┬─────┘ └───────┬────────┘   │
│       │            │               │             │
│  ┌────▼────────────▼───────────────▼────────┐    │
│  │           Service Layer                  │    │
│  └────┬────────────┬───────────────┬────────┘    │
│       │            │               │             │
│  ┌────▼────┐ ┌─────▼──────┐ ┌─────▼────────┐    │
│  │Postgres │ │  MongoDB   │ │  Mail Server │    │
│  └─────────┘ └────────────┘ └──────────────┘    │
└─────────────────────────────────────────────────┘
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | User authentication |
| POST | `/api/v1/auth/register` | User registration |
| GET | `/api/v1/patients` | List patients |
| POST | `/api/v1/patients` | Create patient |
| GET | `/api/v1/institutions` | List institutions |
| POST | `/api/v1/contact-requests` | Submit contact request |
| GET | `/api/v1/notifications` | List notifications |

Full API documentation available via Swagger UI.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for our branch naming, commit conventions, and PR workflow.

## License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file for details.

## Acknowledgments

**Authors:**
- Serrano Puertos Jorge Christian — christian.serrano.puertos@gmail.com
- Florentino Altamirano Misrael — misraelaltamirano@gmail.com
- Ortiz Pérez Alejandro — alex03ortizperez@gmail.com

**Collaborators:**
- Chávez Moreno Jose Eduardo
- Fernández López Kevin Noé

**Advisor:**
- Escobar García Arturo

**Stakeholders:**
- Guarneros Nolasco Luis Rolando
- Cruz Ramos Nancy Aracely

**Academic Support:**
- Universidad Tecnológica del Centro de Veracruz