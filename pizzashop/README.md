# PizzaShop Web Application

A Spring Boot (3.5.5) based web application for an online pizza ordering platform. Current focus: authentication, password reset, role management, and foundational catalog entities (foods, categories, sizes, toppings) with an extensible architecture for future ordering, cart, and payment features.

---
## 1. Key Features (Current & In-Progress)
- User Registration (baseline scaffolding) & Email-based Login
- Secure Authentication (Spring Security, BCrypt password hashing)
- Role-based Authorization (`ROLE_USER`, `ROLE_ADMIN`, etc.)
- Password Reset Flow (token issuance, email delivery, validation, reset)
- Account State Enforcement (inactive / soft-deleted user blocked at login)
- Basic Catalog Domain: Food, Food Category, Sizes, Toppings, Ingredients (schema ready)
- Internationalizable Message Catalog (messages.properties)

Planned (Roadmap): Cart & Checkout, Orders, Payments Integration, OAuth2 Social Login, Catalog Service Layer expansion, API (JSON) endpoints, Observability (metrics, tracing), Multi-language UI.

---
## 2. Technology Stack
| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.5 |
| Web MVC & Views | Spring MVC + Thymeleaf |
| Security | Spring Security (form login, remember-me, future OAuth2) |
| Persistence | Spring Data JPA (Hibernate) |
| Database | MySQL / MariaDB compatible (DDL provided) |
| Build | Maven Wrapper (`mvnw.cmd`, `mvnw`) |
| Validation | Jakarta Bean Validation (hibernate-validator) |
| Email | Spring Boot Starter Mail |
| Logging | SLF4J + (default Logback) |
| Testing | JUnit 5, Spring Boot Test, Selenium (UI smoke) |
| Utility | Lombok |
| Diagrams | PlantUML |

---
## 3. Project Structure (Simplified)
```
src/main/java/com/swp/pizzashop
 ├─ controller/          # MVC Controllers (Authentication, Password Reset)
 ├─ config/              # SecurityConfig & future configs
 ├─ service/             # Service interfaces & implementations (co-located)
 ├─ repository/          # Spring Data repositories
 ├─ model/               # JPA entities (User, Role, Food, etc.)
 ├─ form/                # Form backing / validation DTOs (LoginForm, ResetPasswordForm)
 ├─ messages/            # MessageService & SystemMessageCode
 └─ ...
src/main/resources
 ├─ templates/           # Thymeleaf templates (login, profile, reset-password, etc.)
 ├─ static/              # CSS, JS, images, webfonts
 │   └─ db/              # SQL scripts (schema & seed)
 ├─ uml/                 # PlantUML diagrams (class, sequence, ER, packages, use cases)
 └─ application.properties
```

---
## 4. Getting Started
### 4.1 Prerequisites
- JDK 17 installed (`java -version` should report 17.x)
- MySQL (create database `mydb`)
- (Optional) PlantUML + Java for rendering diagrams

---
## 5. Contribution Guidelines
1. Create feature branch from `develop`. Name branches descriptively (e.g., `features/login`).
2. Follow package layering (controller -> service -> repository -> model).
3. Add or update tests for new public behaviors.
4. Keep diagrams updated when changing architecture (PlantUML sources under `static/uml`).
5. Run `mvnw.cmd clean verify` before PR.
6. No secrets committed (use environment variables / config server).

Git Commit Message Convention (suggested):
```
feat(auth): add locked account handling
fix(reset): correct token expiry validation
refactor(user): extract password policy checker
chore(deps): bump spring-boot to 3.5.x
```

---
## 6. Git Basics (Quick Reference)
All commands assume Windows Command Prompt (no Bash-specific syntax). Replace placeholders (UPSTREAM, BRANCH_NAME, etc.).

### 6.1 Initial Setup
```cmd
git config --global user.name "Your Name"
git config --global user.email you@example.com
git config --global core.autocrlf true       :: On Windows to normalize line endings
git clone <repo-url>
cd pizzashop
git remote -v
```
Add an upstream (if this is a fork):
```cmd
git remote add upstream <original-repo-url>
```

### 6.2 Getting Latest Changes
```cmd
git fetch origin                       :: Fetch new commits & branches
git pull origin develop                :: Update local develop
```
Update all remotes (origin + upstream, if any):
```cmd
git fetch --all --prune
```
Reset local develop to remote (CAUTION: discards local commits not pushed):
```cmd
git checkout develop
git fetch origin
git reset --hard origin/develop
```

### 6.3 Branch Workflow
Create a feature branch off develop:
```cmd
git checkout develop
git pull origin develop
git checkout -b feature/login-rate-limit
```
Push first time (set upstream):
```cmd
git push -u origin feature/login-rate-limit
```
Switch branches:
```cmd
git checkout develop
```
List branches (local + remote):
```cmd
git branch -a
```
Delete a merged local branch:
```cmd
git branch -d feature/old-task
```
Force delete (unmerged) if needed (be careful):
```cmd
git branch -D feature/old-task
```

### 6.4 Making & Reviewing Changes
```cmd
git status                              :: What changed
git diff                                :: See unstaged diffs
git add src\main\java\...\File.java    :: Stage a file
git add .                               :: Stage all (use carefully)
git commit -m "feat(auth): add throttle"
```
Amend last commit message (not pushed yet):
```cmd
git commit --amend -m "feat(auth): refine throttle"
```

### 6.5 Syncing Your Feature Branch
Rebase onto latest develop (preferred clean history):
```cmd
git checkout feature/login-rate-limit
git fetch origin
git rebase origin/develop
```
If conflicts arise:
1. Edit conflicting files.
2. `git add <resolved-file>` for each.
3. `git rebase --continue`.
Abort rebase if necessary:
```cmd
git rebase --abort
```
Alternatively merge (simpler, noisier history):
```cmd
git checkout feature/login-rate-limit
git merge origin/develop
```

### 6.6 Stashing (Temporary Shelving)
```cmd
git stash push -m "WIP: refactor user service"
git stash list
git stash show stash@{0}
git stash pop stash@{0}      :: Apply and drop
```

### 6.7 Inspecting History
```cmd
git log --oneline --graph --decorate --all
```
View a file’s history:
```cmd
git log --follow -- src\main\java\...\UserServiceImpl.java
```
Show a specific commit diff:
```cmd
git show <commit-hash>
```
Blame (who changed what line):
```cmd
git blame src\main\java\...\User.java
```

### 6.8 Undo / Recovery
Soft reset (keep changes staged):
```cmd
git reset --soft HEAD~1
```
Mixed reset (keep changes unstaged):
```cmd
git reset HEAD~1
```
Hard reset (discard local changes – irreversible):
```cmd
git reset --hard origin/develop
```
Revert a pushed commit (creates new inverse commit):
```cmd
git revert <commit-hash>
```

### 6.9 Tags & Releases
```cmd
git tag -a v0.1.0 -m "Initial alpha"
git push origin v0.1.0
git tag --list
```
Delete a tag (local + remote):
```cmd
git tag -d v0.1.0
git push origin :refs/tags/v0.1.0
```

### 6.10 Cleaning Up
Remove local branches already merged into develop:
```cmd
git checkout develop
git branch --merged | findstr /V "* develop" > merged.txt
for /F %b in (merged.txt) do git branch -d %b
```
(For PowerShell adapt syntax or prune manually.)

Prune deleted remote branches:
```cmd
git fetch --prune
```

### 6.11 Upstream Sync (Fork Scenario)
```cmd
git remote add upstream <original-repo-url>   :: First time only
git fetch upstream
git checkout develop
git rebase upstream/develop
git push -f origin develop                     :: Only if you own the fork branch
```

### 6.12 Common Aliases (Optional)
```cmd
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.lg "log --oneline --graph --decorate --all"
```
Use: `git lg`.

### 6.13 Typical Feature Flow (Summary)
1. Update base: `git checkout develop && git pull origin develop`
2. Create branch: `git checkout -b feature/xyz`
3. Commit changes: `git add . && git commit -m "feat(xyz): implement"`
4. Rebase before PR: `git fetch origin && git rebase origin/develop`
5. Push: `git push -u origin feature/xyz`
6. Open Pull Request
7. After merge, clean: `git checkout develop && git pull && git branch -d feature/xyz`

---
## 7. FAQ (Git / Workflow)
**Q: I committed credentials accidentally.**
- Remove file, commit, then use `git filter-repo` or `git filter-branch` (history rewrite) and rotate the secret immediately.

**Q: Rebase conflict keeps repeating.**
- You may be editing generated or formatted files; resolve, `git add`, continue. If stuck: `git rebase --abort` and try a merge instead.

**Q: My branch shows many unrelated commits.**
- You branched from an outdated base. Rebase onto latest `origin/develop`.

