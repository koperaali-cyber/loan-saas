# LoanSaaS — Multi-Tenant Loan Management Platform

A complete, production-style **SaaS loan management system** for Tanzanian microfinance lenders, built with **Java Spring Boot + Thymeleaf + Spring Security + JPA + PostgreSQL**. One platform owner (Super Admin) approves and manages many independent lenders; each lender manages their own isolated customers, loans and payments.

Amounts are in **TZS**. Phone-based login. Fully server-rendered (no SPA).

---

## Prerequisites

- **JDK 21**
- **PostgreSQL** installed and running (with pgAdmin, or psql)
- **IntelliJ IDEA** (bundled Maven is used — no separate Maven install needed)

---

## Step 1 — Create the database

Open **pgAdmin** (or psql) and create an empty database:

```sql
CREATE DATABASE loansaas;
```

That's the only manual DB step. Hibernate creates all tables automatically on first run.

---

## Step 2 — Set your PostgreSQL password

Open `src/main/resources/application.properties` and make sure the username/password match your PostgreSQL install:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/loansaas
spring.datasource.username=postgres
spring.datasource.password=postgres
```

The defaults assume user `postgres` with password `postgres`. Change the password to whatever you set when installing PostgreSQL.

---

## Step 3 — Run

1. In IntelliJ: **File → Open** → select the unzipped `loan-saas` folder. Maven downloads dependencies automatically.
2. Open `src/main/java/com/loansaas/LoanSaasApplication.java`.
3. Click the green ▶ **Run** button.
4. Wait for the console line: `Tomcat started on port 8080`.

---

## Step 4 — Open in your browser

Go to **http://localhost:8080**

On first run the database is auto-seeded with demo lenders, customers, loans and payments (including one overdue and one fully-paid loan) so every screen has realistic data. Seeding is skipped on later runs because the data persists in PostgreSQL.

---

## Demo accounts

| Role              | Phone            | Password    |
|-------------------|------------------|-------------|
| Super Admin       | `+255700000000`  | `admin123`  |
| Lender (approved) | `+255712345678`  | `lender123` |
| Lender (approved) | `+255754112233`  | `lender123` |
| Lender (pending)  | `+255786998877`  | `lender123` |

> The **pending** lender cannot log in until the Super Admin approves it — this demonstrates the approval workflow. Log in as the Super Admin, open **Pending Approvals**, and approve it.

---

## What you can do

**As Super Admin**
- See platform-wide stats (lenders, customers, loans, money issued/collected).
- Approve, reject (with reason), suspend, re-activate, reset password for, or delete any lender.
- Browse all customers, loans, payments and the activity log across every tenant.

**As a Lender**
- Register customers (name, phone, alt phone, NIDA, photo) — NIDA & phone are unique platform-wide.
- Issue loans with automatic interest & total-repayment calculation and auto due-date.
- Record repayments — balance and loan status (ACTIVE / PARTIALLY_PAID / PAID / OVERDUE / DEFAULTED) update automatically.
- View a dashboard with a loan-status chart, search/sort customers and loans, see automatic risk scoring (LOW / MEDIUM / HIGH).
- Export the loan book to **PDF** and **Excel**.
- Change password and view profile.

Each lender's data is strictly isolated — no lender can see another lender's records.

---

## Resetting the database

Because PostgreSQL now persists data across restarts, seeding runs only once. To start completely fresh, either:

- Drop and re-create the database (`DROP DATABASE loansaas; CREATE DATABASE loansaas;`), **or**
- Temporarily set `spring.jpa.hibernate.ddl-auto=create-drop` in `application.properties`, run once, then set it back to `update`.

---

## Tech stack

- **Java 21**, **Spring Boot 3.3.x**
- Spring MVC, Spring Security (BCrypt, form login by phone), Spring Data JPA / Hibernate
- Thymeleaf server-side templates + custom CSS design system (no Bootstrap, no CDN)
- **PostgreSQL**
- OpenPDF + Apache POI for PDF / Excel export
- Lombok

---

## Project structure

```
loan-saas/
├── pom.xml
└── src/main/
    ├── java/com/loansaas/
    │   ├── LoanSaasApplication.java
    │   ├── config/        (security, data seeder, web/uploads config)
    │   ├── controller/    (public, admin, lender, customer, loan, payment, report)
    │   ├── dto/
    │   ├── entity/        (User, Lender, Customer, Loan, Payment, Activity + enums)
    │   ├── repository/
    │   ├── security/      (custom UserDetails, login success routing)
    │   ├── service/       (business logic + export service)
    │   └── util/          (LoanCalculator, FileStorageService)
    └── resources/
        ├── application.properties
        ├── static/        (css, js)
        └── templates/     (Thymeleaf: admin/, lender/, auth/, fragments/)
```

Uploaded customer/lender photos are stored in an `uploads/` folder created at the project root on first upload, and served at `/uploads/**`.

---

## Notes

- Interest is a simple flat percentage of the principal: `total = amount + (amount × rate%)`.
- The whole UI is responsive with a collapsible sidebar on mobile.
