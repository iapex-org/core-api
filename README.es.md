# IAPEX — API REST Principal

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/MongoDB-7.0-47A248?logo=mongodb&logoColor=white" alt="MongoDB">
  <img src="https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black" alt="Swagger">
</p>

<p align="center">
  <em>API REST central para gestión de pacientes, autenticación y operaciones institucionales en el ecosistema IAPEX.</em>
</p>

<p align="center">
  <a href="https://github.com/iapex-org/core-api">Repositorio</a>
  ·
  <a href="https://github.com/iapex-org/core-api/issues">Reportar Bug</a>
  ·
  <a href="https://virtual.cuautitlan.unam.mx/intar/wp-content/uploads/sites/19/2025/12/166-A-Hybrid-Artificial-Intelligent-System-for-Missing-JORGE-CHRISTIAN-SERRANO-PUERTOS.pdf">Artículo de Investigación</a>
</p>

<p align="center">
  <a href="README.md">🇬🇧 English</a> · <a href="README.es.md">🇪🇸 Español</a>
</p>

---

## Acerca de IAPEX

**IAPEX** (Inteligencia Artificial para la Localización de Pacientes Extraviados en Instituciones de Salud) es un sistema validado que ayuda a instituciones de salud a identificar y gestionar pacientes no localizados mediante una fusión de reconocimiento facial y análisis textual.

Este repositorio contiene la **API REST Principal** — el servicio backend central que gestiona:

- Autenticación y autorización de usuarios (JWT + RBAC)
- Operaciones CRUD de pacientes e instituciones
- Procesamiento de solicitudes de contacto
- Notificaciones por correo electrónico
- Gestión de archivos multimedia
- Integración con el motor de búsqueda de IA

### Ecosistema

| Componente | Repositorio | Stack |
|-----------|-----------|-------|
| **Core API** (este) | [iapex-org/core-api](https://github.com/iapex-org/core-api) | Spring Boot 3, PostgreSQL, MongoDB |
| **Portal Web** | [iapex-org/web-app](https://github.com/iapex-org/web-app) | Angular 19, Bootstrap, TypeScript |
| **App Móvil** | [iapex-org/mobile-app](https://github.com/iapex-org/mobile-app) | React 18, Ionic 8, Capacitor |

## Funcionalidades

- **Autenticación JWT** — Auth segura basada en tokens con refresh tokens
- **Control de Acceso RBAC** — Permisos granulares para diferentes roles
- **Gestión de Pacientes** — CRUD completo para registros de pacientes y datos morfológicos
- **Gestión de Instituciones** — Perfiles de instituciones de salud y membresías
- **Solicitudes de Contacto** — Procesar consultas familiares sobre coincidencias potenciales
- **Notificaciones Email** — Notificaciones institucionales vía SMTP
- **Gestión de Archivos** — Carga y almacenamiento seguro de multimedia
- **Documentación Swagger** — Docs interactivos en `/swagger-ui.html`
- **Caché** — Optimización de rendimiento con Caffeine cache
- **PostgreSQL + MongoDB** — Datos relacionales con NoSQL para encodings/caché

## Inicio rápido

### Prerrequisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- MongoDB 6.0+

### Instalación

```bash
git clone https://github.com/iapex-org/core-api.git
cd core-api
```

Crea un archivo `.env` desde el ejemplo:

```bash
cp .env.example .env
```

Configura tus variables de entorno:

```env
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/iapex
SPRING_DATASOURCE_PASSWORD=your_db_password
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_email_password
```

Compila y ejecuta:

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`

Documentación de la API: `http://localhost:8080/swagger-ui.html`

## Arquitectura

```
┌─────────────────────────────────────────────────┐
│                  Core API                        │
│               Spring Boot 3.3                     │
│                                                   │
│  ┌──────────┐ ┌──────────┐ ┌────────────────┐   │
│  │  Auth    │ │ Patient  │ │  Institution    │   │
│  │  Module  │ │  Module  │ │  Module         │   │
│  └────┬─────┘ └────┬─────┘ └───────┬────────┘    │
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

## Contribuciones

Por favor lee [CONTRIBUTING.md](CONTRIBUTING.md) para nuestras convenciones de ramas, commits y flujo de PRs.

## Licencia

Este proyecto está licenciado bajo GNU General Public License v3.0 — consulta el archivo [LICENSE](LICENSE) para más detalles.

## Reconocimientos

**Autores:**
- Serrano Puertos Jorge Christian — christian.serrano.puertos@gmail.com
- Florentino Altamirano Misrael — misraelaltamirano@gmail.com
- Ortiz Pérez Alejandro — alex03ortizperez@gmail.com

**Colaboradores:**
- Chávez Moreno Jose Eduardo
- Fernández López Kevin Noé

**Asesor:**
- Escobar García Arturo

**Interesados (Stakeholders):**
- Guarneros Nolasco Luis Rolando
- Cruz Ramos Nancy Aracely

**Apoyo Académico:**
- Universidad Tecnológica del Centro de Veracruz
