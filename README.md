# IAPEX - API REST (Spring Boot)

API principal para la gestión, autenticación y seguridad de pacientes extraviados en instituciones de salud. Forma parte del ecosistema IAPEX, integrando microservicios y aplicaciones especializadas.

<p align="center">
	<img src="https://img.shields.io/badge/Java-17-blue?logo=openjdk" alt="Java version">
	<img src="https://img.shields.io/badge/Spring%20Boot-3.1-6DB33F?logo=springboot" alt="Spring Boot version">
	<img src="https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql" alt="PostgreSQL">
	<img src="https://img.shields.io/badge/MongoDB-7.0-47A248?logo=mongodb" alt="MongoDB">
</p>

---

## 🏥 ¿Qué es IAPEX?

**IAPEX** (_Inteligencia Artificial para la Localización de Pacientes Extraviados en Instituciones de Salud_) es un sistema robusto y validado para la identificación y gestión de pacientes no localizados en instituciones de salud.

### Componentes del ecosistema

- **App móvil**: búsqueda rápida desde dispositivos móviles
- **App web**: gestión institucional y para personal de salud
- **API REST (Spring Boot)**: backend principal, autenticación y seguridad
- **API de búsqueda (FastAPI)**: motor de IA y búsqueda híbrida

Esta API REST gestiona usuarios, pacientes, autenticación, roles, notificaciones y operaciones institucionales, integrándose con el motor de IA y otros servicios.

---

## ✨ Funcionalidades principales

- 🔒 **Autenticación y autorización** (JWT, roles, usuarios)
- 🏥 **Gestión de pacientes e instituciones**
- 📧 **Notificaciones por correo electrónico**
- 📦 **Carga y gestión de archivos**
- 📊 **Integración con bases de datos relacionales y NoSQL**
- 📝 **Documentación interactiva** (Swagger UI)

---

## 🛠 Tecnologías

- **Java 17+**
- **Spring Boot 3.1+**
- **PostgreSQL** (datos principales)
- **MongoDB** (caché y encodings)
- **Spring Security**
- **Swagger/OpenAPI**
- **Maven**

---

## ⚡ Instalación rápida

### Prerrequisitos

- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 14+**
- **MongoDB 6.0+**

### Pasos

1. **Clonar repositorio**

```bash
git clone https://github.com/aescobar80/API-REST-IAPEX.git
cd API-REST-IAPEX
```

2. **Configurar variables de entorno**

Copia el archivo de ejemplo:

```bash
cp .env.example .env
```

Edita `.env` con tus credenciales y rutas.

3. **Compilar y ejecutar**

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`

---

## 🔑 Variables de entorno

Las variables sensibles y de configuración se gestionan en el archivo `.env`. Consulta `.env.example` para ver los valores requeridos:

```env
SPRING_APPLICATION_NAME=IAPEX-API-REST
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/iapex
SPRING_DATASOURCE_PASSWORD=your_db_password
...otros valores...
```

---

## 🏗 Ecosistema IAPEX: arquitectura de microservicios

IAPEX está compuesto por múltiples repositorios especializados que conforman una arquitectura moderna y escalable:

| Repositorio                                                            | Propósito                 | Estado       |
| ---------------------------------------------------------------------- | ------------------------- | ------------ |
| **[IAPEX-MOBILE-APP](https://github.com/aescobar80/IAPEX-MOBILE-APP)** | App móvil para búsqueda   | ✅ Operativo |
| **[IAPEX_APP-WEB](https://github.com/aescobar80/IAPEX_APP-WEB)**       | App web institucional     | ✅ Operativo |
| **[API-REST-IAPEX](https://github.com/aescobar80/API-REST-IAPEX)**     | API principal y seguridad | ✅ Operativo |
| **[API-SEARCH-IAPEX](https://github.com/misraelDev/API-SEARCH-IAPEX)** | Motor de IA y búsqueda    | ✅ Operativo |

---

## 🤝 Colaboración interna

Seguimos convenciones específicas para mantener consistencia - consulta [CONTRIBUTING.MD](CONTRIBUTING.MD).

## 🤝 Reconocimientos

Este proyecto fue desarrollado por el equipo de autores:

- Florentino Altamirano Misrael
- Ortiz Pérez Alejandro
- Serrano Puertos Jorge Christian

Con la asesoría y guía conceptual de:

- Escobar García Arturo

Y con el apoyo académico de la

- Universidad Tecnológica del Centro de Veracruz
