# StudentBridge

StudentBridge is a university capstone web platform that helps international students in South Korea find student-friendly part-time jobs and helps local employers reach student workers.

The current implementation uses a static HTML/CSS/JavaScript frontend, Java Servlets running on Apache Tomcat, and a MySQL database accessed through JDBC.

## Current Status

| Area | Status | Notes |
|---|---|---|
| Homepage | Working | `index.html` presents the product, navigation, and calls to action. |
| Job search UI | Working | `frontend/jobsearch.html` loads jobs from the backend and stays usable with an empty state if the database is unavailable. |
| Registration | Working | `RegisterServlet` inserts student and employer accounts into MySQL when Tomcat/Railway database settings are configured. |
| Login | Working | `LoginServlet` checks MySQL credentials, creates a servlet session, and redirects users to the correct dashboard/profile flow. |
| Job application | Working | Students can apply once per job through `ApplyServlet`; duplicate applications are blocked. |
| Employer workflow | Working | Employers can post jobs, view applicants for their own jobs, and send messages to student applicants. |
| Kakao Map address search | Working with graceful fallback | Users search by address; coordinates are hidden and saved when Kakao geocoding succeeds. |
| Live deployment | Available | Render hosts the current demo build at the live site link below. |

## Demo Links

| Demo type | Link | Status |
|---|---|---|
| Local Tomcat demo | <http://localhost:8080/> | Use for final local verification after running the Render/Tomcat-style build. |
| Static GitHub Pages demo | <https://capstonedesign-spring2026-ulsancollege.github.io/StudentBridge/> | Optional static reference; backend features require Tomcat/Render. |
| Render live site | <https://studentbridge-6jn2.onrender.com> | Live StudentBridge deployment. |
| Final presentation deck | [Download PPTX](docs/presentations/StudentBridge_Premium_Final_Deck_v5.pptx) | Capstone final presentation deck. |
| Demo video | `[TODO: add demo video link]` | Required backup evidence. |
| Screenshots | `[TODO: add screenshot folder or issue links]` | Required for sprint packets and brochure QR support. |

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML, CSS, JavaScript |
| Backend | Java Servlets with Jakarta Servlet API |
| Server | Apache Tomcat 10.1.x |
| Database | MySQL with JDBC |
| Driver/dependencies | `server/mysql-connector-j-9.3.0.jar`, `server/servlet-api.jar` |

## Core Demo Flow

1. Open the StudentBridge homepage.
2. Register or log in as an employer.
3. Post a student-friendly job with a real address and Kakao map lookup.
4. Log out and log in as a student.
5. Search for the job and apply with one click.
6. Log back in as the employer, open applicants, and send a message.
7. Log in as the student and confirm the message is visible.

## Documentation Index

| Document | Purpose |
|---|---|
| [Project Overview](docs/PROJECT_OVERVIEW.md) | Problem, users, solution, MVP scope, and evidence placeholders. |
| [Architecture](docs/ARCHITECTURE.md) | Frontend/backend/database structure and key risks. |
| [Setup Guide](docs/SETUP_GUIDE.md) | Java Servlet, Tomcat, and MySQL setup steps. |
| [User Stories](docs/USER_STORIES.md) | MVP stories, acceptance criteria, and status. |
| [Sprint 2](docs/SPRINT_2.md) | Midterm sprint goal, evidence needs, and blockers. |
| [Testing Notes](docs/TESTING_NOTES.md) | Manual test plan and current test status. |
| [Demo Plan](docs/DEMO_PLAN.md) | Live demo script, backup plan, and QR support. |
| [Midterm Pitch Notes](docs/MIDTERM_PITCH_NOTES.md) | Investor-style pitch outline and ask. |
| [Roadmap](docs/ROADMAP.md) | Path from current MVP to final presentation. |
| [Team Roles](docs/TEAM_ROLES.md) | Team ownership, weekly role rotation, and responsibilities. |
| [AI Code Ownership Audit](docs/AI_CODE_OWNERSHIP_AUDIT.md) | AI usage, code ownership, risks, and stabilization goals. |
| [Design Package](docs/DESIGN_PACKAGE.md) | Paper.design UI plan, Canva visual package, and brochure content. |
| [Docs Index](docs/README.md) | Full documentation map for the repository. |

## Known Issues

- Passwords are stored as plain text for the capstone demo; hashing is required before real users.
- Admin moderation, employer verification, and file storage hardening are future production work.
- Some documentation evidence links still need final screenshots, demo video, and professor-submission URLs.
- Kakao Map requires the JavaScript key and allowed domains to be configured in Kakao Developers; the app still saves address text if the map cannot load.

## Quick Start

Use the full [Setup Guide](docs/SETUP_GUIDE.md). The short version is:

```bash
./deploy.sh
```

Then start Tomcat and open:

```text
http://localhost:8080/StudentBridge/
```

## Evidence Policy

This project follows the class rule: if it is not linked, it did not happen. Add real links for GitHub issues, pull requests, board screenshots, demo videos, screenshots, and sprint packets before using the documentation for grading.
