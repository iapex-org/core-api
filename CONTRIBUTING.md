# Contributing to IAPEX Core API

## Our Workflow (GitHub Flow)

Simple and direct:

1. Create your **branch** from `main`.
2. Make **commits** in English using conventional commits.
3. Open a **pull request (PR)**.
4. Get **approval**.
5. **Merge** and done.

---

## Naming: Branches and Commits

### Branches

| Prefix     | Usage                | Examples                   |
| :---------- | :----------------- | :------------------------- |
| `feat/`     | New features       | `feat/facial-recognition`  |
| `fix/`      | Bug fixes          | `fix/search-validation`    |
| `refactor/` | Code improvements  | `refactor/patient-service` |
| `docs/`     | Documentation      | `docs/api-endpoints`       |
| `test/`     | Tests              | `test/search-algorithm`    |

### Commits (type: subject)

| Type       | Description                 | Example                               |
| :---------- | :-------------------------- | :------------------------------------ |
| `feat:`     | New features               | `feat: implement hybrid search`       |
| `fix:`      | Bug fixes                  | `fix: correct similarity calculation` |
| `refactor:` | Code restructuring          | `refactor: modularize search logic`   |
| `docs:`     | Documentation changes       | `docs: update installation guide`     |
| `test:`     | Add or modify tests        | `test: add patient search tests`      |

---

## How to Contribute (Step by Step)

1. **Update `main`:**

    ```bash
    git switch main
    git pull origin main
    ```

    _Don't use `--rebase` unless you know what you're doing._

2. **Create your branch:**

    ```bash
    git branch feat/new-feature
    git switch feat/new-feature
    ```

    _E.g., `git branch feat/patient-search` and `git switch feat/patient-search`._

3. **Work and commit:**

    - Develop your code
    - Make **small, frequent** commits
    - Use conventional commits

    ```bash
    git add .
    git commit -m "feat: add patient similarity filter"
    ```

4. **Push your branch to GitHub:**

    ```bash
    git push -u origin feat/new-feature
    ```

    _First time only. After that, `git push`._

5. **Open a Pull Request (PR):**
    - Go to GitHub.
    - **Use the template:** When creating the PR, fill out the template that appears automatically (located in `.github/PULL_REQUEST_TEMPLATE.md`).
    - **PR Title:** Clear, follows the main commit convention (e.g., `feat: add hybrid search mode`).
    - **Description:**
        - **What:** Summary of changes.
        - **Why:** Justification.
        - **How to test:** Steps for the reviewer.
        - **Notes:** Any extras (e.g., "breaking changes", impact on other services).

---

## Merge Requirements

- :white_check_mark: **Approval** from a team member
- :white_check_mark: **No conflicts** with `main`
- :white_check_mark: **Correct naming** (branches and commits)
- :white_check_mark: **Updated documentation** (if applicable)

---

## Review Process

- **Update:** If changes are requested, respond and update your PR.

---

## Key Rules

### Before Merging

- **No direct push to `main` (always via PR)!**
- **No merging code that doesn't compile!**
- **No merging if it breaks existing functionality!**
- **Mandatory:** Use branch and commit naming conventions.

### After Merging

- **Delete your branch** on GitHub.
- Notify the team about important changes.

---

## Questions?

- Open an **Issue**.
- Contact any team member.
