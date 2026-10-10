# Assignment 9 Submission Report Guide

**Title:** Smart Tourism Management System (STMS) — Three-Tier Database Application with RBAC  
**Student Name:** Sayali Gayke  
**UCE NO:** UCE2026005  
**Batch:** A4  
**Subject:** Database Management Systems (DBMS) Laboratory  

---

## 1. Title, Aim, and Problem Statement

### Title
Design and Implementation of a Three-Tier Desktop Database Application for the Smart Tourism Management System with Role-Based Access Control (RBAC) using Java Swing, JDBC, and MySQL.

### Aim
To design, implement, and test a complete, robust, college-level desktop database application that interacts with a normalized relational database (`smart_tourism_db`) using Java Swing for the Presentation Layer, Java Service classes for the Business Logic Layer, and JDBC DAO classes for the Data Access Layer, adhering strictly to Three-Tier Architecture, Role-Based Access Control (RBAC), and secure BCrypt password hashing.

### Problem Statement
In Assignment 6 and Assignment 7, a normalized relational database `smart_tourism_db` was designed up to Third Normal Form (3NF) and populated with relational tables (`USER`, `TOURIST`, `ADMINISTRATOR`, `DESTINATION`, `ATTRACTION_TYPE`, `ATTRACTION`, `TOUR_PACKAGE`, `DESTINATION_PACKAGE`, `BOOKING`, `PAYMENT`), integrity constraints, triggers, and stored procedures.

Assignment 9 requires developing a complete desktop application with:
1. **Three-Tier Architecture:** Complete separation between Presentation (Swing), Business/Application Logic (Service), and Persistence (DAO).
2. **Role-Based Access Control (RBAC):** Distinct roles for **Tourists** and **Administrators** strictly enforced in the Service Layer (not merely hidden in the GUI).
   - Tourists can browse destinations, view attractions, browse tour packages, self-register, book packages, view own bookings/payments, and cancel own eligible bookings.
   - Tourists cannot add, edit, or delete destinations, attractions, packages, or manage payments.
   - Administrators manage destinations, attractions, packages, package-destination mappings, bookings, and payment status/refunds.
3. **Secure Authentication:** Implementation of a separate `USER_CREDENTIAL` table referencing `USER(user_id)` with BCrypt password hashing without altering existing 3NF normalized tables.
4. **Normalized Schema Adherence:** Dynamic calculation of booking totals ($cost \times people$), composite primary keys on `ATTRACTION(destination_id, attraction_id)` and `DESTINATION_PACKAGE`, and adherence to database triggers.

---

## 2. Three-Tier Architecture & Security Flow

```text
       +-------------------------------------------------------------------+
       |                    TIER 1: PRESENTATION LAYER                     |
       |                (Package: smarttourism.presentation)               |
       |                                                                   |
       |  • LoginForm.java           • RegisterDialog.java                 |
       |  • TouristDashboard.java    • Dashboard.java (Admin Dashboard)    |
       |  • DestinationForm.java     • AttractionForm.java                 |
       |  • PackageForm.java         • BookingForm.java                    |
       |  • PaymentForm.java         • UserForm.java                       |
       +----------------------------------+--------------------------------+
                                          │
                         UI Actions & DTOs│ Service Results
                                          ▼
       +-------------------------------------------------------------------+
       |                 TIER 2: BUSINESS / SERVICE LAYER                  |
       |                   (Package: smarttourism.service)                 |
       |                                                                   |
       |  • SessionManager.java      (Centralized Session & RBAC Guards)   |
       |  • AuthService.java         (BCrypt Auth, Register, Change Pass) |
       |  • DestinationService.java  (Admin CRUD, Public Read)             |
       |  • AttractionService.java   (Admin CRUD, Public Read)             |
       |  • PackageService.java      (Admin CRUD, Public Read)             |
       |  • BookingService.java      (Tourist Self-Book, Dynamic Cost)     |
       |  • PaymentService.java      (Admin Status/Refund, Tourist Scoped) |
       |  • UserService.java         (Admin Mgmt, Tourist Profile Update)  |
       |  • PasswordUtil.java        (BCrypt Hashing & Verification)       |
       +----------------------------------+--------------------------------+
                                          │
                     Parameterized Queries│ ResultSets & Entities
                                          ▼
       +-------------------------------------------------------------------+
       |                  TIER 3: DATA ACCESS LAYER (DAO)                  |
       |                     (Package: smarttourism.dao)                   |
       |                                                                   |
       |  • DBConnection.java        (JDBC Connection Pool & Diagnostics)  |
       |  • UserDAO.java             (ACID Multi-Table Register & Auth)    |
       |  • DestinationDAO.java      • AttractionDAO.java                  |
       |  • PackageDAO.java          • DestinationPackageDAO.java          |
       |  • BookingDAO.java          (Calculates p.cost * b.people)        |
       |  • PaymentDAO.java          (Scoped Tourist Queries & Functions)  |
       |  • DashboardDAO.java        (KPI Aggregations)                    |
       +----------------------------------+--------------------------------+
                                          │
                                      JDBC│ TCP/IP Port 3306
                                          ▼
       +-------------------------------------------------------------------+
       |                       MYSQL DATABASE ENGINE                       |
       |                     Database: smart_tourism_db                    |
       |                                                                   |
       |  Tables: USER, TOURIST, ADMINISTRATOR, USER_CREDENTIAL,           |
       |          DESTINATION, ATTRACTION_TYPE, ATTRACTION,                |
       |          TOUR_PACKAGE, DESTINATION_PACKAGE, BOOKING, PAYMENT      |
       +-------------------------------------------------------------------+
```

---

## 3. Database Schema Migration (`USER_CREDENTIAL`)

To provide enterprise authentication without breaking 3NF normalization or existing foreign keys:

```sql
USE smart_tourism_db;

CREATE TABLE IF NOT EXISTS user_credential (
    user_id INT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_credential_user
        FOREIGN KEY (user_id) REFERENCES user(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

---

## 4. Default Demonstration Credentials

| Role | Name | Email | User ID | Password | Access Level |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Administrator** | Priya Patil | `priya@example.com` | `9002` | `admin123` | Full Administrative Management |
| **Administrator** | Neha Rao | `neha@example.com` | `9012` | `admin123` | Full Administrative Management |
| **Tourist** | Aarav Sharma | `aarav@example.com` | `9001` | `tourist123` | Browse, Book, My Bookings, Profile |
| **Tourist** | Rahul Verma | `rahul@example.com` | `9003` | `tourist123` | Browse, Book, My Bookings, Profile |

---

## 5. Automated Verification Results (16 / 16 Tests Passed)

```text
==================================================================
  Smart Tourism Management System — RBAC & Integration Test Suite
==================================================================
  [PASS] 1. Database Connection to MySQL smart_tourism_db
  [PASS] 2. Administrator Login with valid BCrypt credentials
  [PASS] 3. Invalid password rejected cleanly
  [PASS] 4. Tourist Login with valid BCrypt credentials
  [PASS] 5. Tourist Self-Registration creates USER, TOURIST, & USER_CREDENTIAL
  [PASS] 6. Duplicate email registration rejected
  [PASS] 7. Tourist can browse destinations and packages
  [PASS] 8. RBAC Guard: Tourist denied from creating destination
  [PASS] 9. RBAC Guard: Tourist denied from creating tour package
  [PASS] 10. Tourist can create booking for themselves with dynamic 3NF cost
  [PASS] 11. Ownership Scoping: Tourist cannot view another tourist's booking
  [PASS] 12. Tourist cannot update payment status
  [PASS] 13. Tourist can cancel their own eligible booking
  [PASS] 14. Password change requires current password and works with BCrypt
  [PASS] 15. Administrator can view all bookings and statistics
  [PASS] 16. Logout clears session and blocks protected operations
==================================================================
  Test Summary: 16 / 16 Tests Passed Successfully!
==================================================================
```

---

## 6. Screenshots Captured

The demonstration screenshots are available in [`screenshots/`](file:///c:/Users/Sauu/eclipse-workspace/SmartTourismManagement/screenshots/):
1. `1_login_screen.png`: Login screen with email, password, show/hide password, and registration controls.
2. `2_admin_dashboard.png`: Administrator Dashboard with KPIs and management modules.
3. `3_admin_destination_management.png`: Destination Management with full CRUD capabilities.
4. `4_admin_payment_management.png`: Payment & Refund Management form.
5. `5_tourist_dashboard.png`: Tourist Dashboard with destinations, tour packages, booking form, dynamic pricing, and booking history.
