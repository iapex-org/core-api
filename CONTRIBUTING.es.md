# Guía de Contribución - IAPEX Core API

## Nuestro flujo (GitHub Flow)

Simple y directo:

1. Crea tu **rama** desde `main`.
2. Haz **commits** en inglés con conventional commits.
3. Abre un **pull request (PR)**.
4. Obtén **aprobación**.
5. **Mergea** y listo.

---

## Nomenclatura: ramas y commits

### Ramas

| Prefijo     | Uso                | Ejemplos                   |
| :---------- | :----------------- | :------------------------- |
| `feat/`     | Nuevas funciones   | `feat/facial-recognition`  |
| `fix/`      | Corrección de bugs | `fix/search-validation`    |
| `refactor/` | Mejoras de código  | `refactor/patient-service` |
| `docs/`     | Documentación      | `docs/api-endpoints`       |
| `test/`     | Pruebas            | `test/search-algorithm`    |

### Commits (tipo: asunto)

| Tipo       | Descripción                 | Ejemplo                               |
| :---------- | :-------------------------- | :------------------------------------ |
| `feat:`     | Nuevas funciones           | `feat: implement hybrid search`       |
| `fix:`      | Corrección de bugs          | `fix: correct similarity calculation` |
| `refactor:` | Reestructuración del código | `refactor: modularize search logic`   |
| `docs:`     | Cambios en la documentación | `docs: update installation guide`     |
| `test:`     | Añadir o modificar pruebas  | `test: add patient search tests`      |

---

## Cómo contribuir (paso a paso)

1. **Actualiza `main`:**

    ```bash
    git switch main
    git pull origin main
    ```

    _No uses `--rebase` a menos que sepas bien lo que haces._

2. **Crea tu rama:**

    ```bash
    git branch feat/nueva-funcionalidad
    git switch feat/nueva-funcionalidad
    ```

    _Ej: `git branch feat/patient-search` y `git switch feat/patient-search`._

3. **Trabaja y haz commits:**

    - Desarrolla tu código
    - Haz commits **pequeños y frecuentes**
    - Usa conventional commits

    ```bash
    git add .
    git commit -m "feat: add patient similarity filter"
    ```

4. **Envía tu rama a GitHub:**

    ```bash
    git push -u origin feat/nueva-funcionalidad
    ```

    _Solo la primera vez. Luego, `git push`._

5. **Abre un pull request (PR):**
    - Ve a GitHub.
    - **Usa la plantilla predeterminada:** Al crear el PR, completa la plantilla que aparece automáticamente (ubicada en `.github/PULL_REQUEST_TEMPLATE.md`).
    - **Título del PR:** Claro, sigue la convención del commit principal (ej. `feat: add hybrid search mode`).
    - **Descripción:**
        - **Qué:** Resumen de cambios.
        - **Por qué:** Justificación.
        - **Cómo probar:** Pasos para el revisor.
        - **Notas:** Cualquier extra (ej. "breaking changes", impacto en otros servicios).

---

## Requisitos para mergear

- :white_check_mark: **Aprobación** de un miembro del equipo
- :white_check_mark: **Sin conflictos** con `main`
- :white_check_mark: **Nomenclatura correcta** (ramas y commits)
- :white_check_mark: **Documentación actualizada** (si aplica)

---

## Revisión

- **Actualiza:** Si te piden cambios, responde y actualiza tu PR.

---

## Reglas clave

### Antes de mergear

- **¡No hacer push directo a `main` (siempre vía PR)!**
- **¡No mergear código que no compile!**
- **¡No mergear si rompe funcionalidad existente!**
- **Obligatorio:** Usa la nomenclatura de ramas y commits.

### Después de mergear

- **Elimina tu rama** en GitHub.
- Notifica al equipo sobre cambios importantes.

---

## ¿Dudas?

- Abre un **Issue**.
- Contacta a cualquier miembro del equipo.
