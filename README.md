# Smart Tourism Management System (STMS)

**Course:** Database Management Systems (DBMS) Laboratory  
**Assignment:** Assignment 9 — Three-Tier Desktop Database Application  
**Student Name:** Sayali Gayke  
**UCE NO:** UCE2026005  
**Batch:** A4  

---

## 1. Project Overview

The **Smart Tourism Management System (STMS)** is an enterprise-grade desktop database application built using **Java Swing, JDBC, and MySQL**. It strictly implements a **Three-Tier Architecture**:
1. **Presentation Layer (Java Swing):** Modern graphical user interfaces tailored separately for Tourists and Administrators.
2. **Service / Business Layer:** Role-Based Access Control (RBAC), data validation, date sequencing, dynamic 3NF pricing, and transaction coordination.
3. **Data Access Layer (DAO / JDBC):** Parameterized SQL queries using PreparedStatements, database connection management, and transactional integrity.

The system connects to the pre-existing normalized database `smart_tourism_db` (from Assignments 6 & 7) without altering existing tables or destroying data. Security is enhanced via a dedicated, non-destructive `USER_CREDENTIAL` table utilizing industry-standard **BCrypt password hashing**.

---

## 2. Technology Stack

- **Language:** Java 17+ (tested on JDK 17, JDK 21, and JDK 26)
- **GUI Framework:** Java Swing (`java.desktop`)
- **Database Engine:** MySQL Server 8.0+
- **Database Name:** `smart_tourism_db`
- **Security / Hashing:** BCrypt password hashing (`lib/jbcrypt-0.4.jar` & `PasswordUtil`)
- **Connectivity:** JDBC with MySQL Connector/J 8.3.0 (`lib/mysql-connector-j-8.3.0.jar`)
- **IDE:** Eclipse IDE for Java Developers
- **Build System:** Standard Eclipse Java Project + `pom.xml` for Maven builds

---

## 3. Role-Based Access Control (RBAC) Specification

Every operation is strictly validated in the **Service Layer** using `SessionManager` and cannot be bypassed via GUI manipulation.

### 3.1. Tourist Role Capabilities & Restrictions

**Permitted Actions:**
1. **Browse Destinations:** View all destinations; search and filter by name or location.
2. **View Attractions:** View attractions by destination, including entry fees, closing times, operating hours, and descriptions.
3. **Explore Tour Packages:** Browse packages with duration, per-person cost, and included destinations.
4. **Self-Registration:** Register as a tourist with name, email, phone, address, and password.
5. **Book Tour Packages:** Book packages for self with dynamic 3NF cost calculation (`cost × people`). Identity is bound directly to the active session.
6. **My Bookings:** View only their own bookings and calculated amounts.
7. **Cancel Bookings:** Cancel their own eligible bookings (status updated to 'Cancelled').
8. **My Payments:** View only payment status and refund information for their own bookings.
9. **Manage Profile:** Update personal contact details (Name, Phone, Street, City, State, Country).
10. **Change Password:** Update account password (requires verifying current password).
11. **Logout:** Invalidate active session.

**Strictly Prohibited for Tourists (Service Layer Enforced):**
- Cannot add, edit, or delete destinations (throws `ValidationException: Access Denied`).
- Cannot add, edit, or delete attractions or attraction types.
- Cannot add, edit, or delete tour packages or destination mappings.
- Cannot view or cancel other tourists' bookings.
- Cannot view other tourists' payments or update payment status.
- Cannot access administrative dashboards or user management.

### 3.2. Administrator Role Capabilities

**Permitted Actions:**
1. **Manage Destinations:** Full CRUD with foreign-key dependency checks.
2. **Manage Attractions & Types:** Full CRUD on composite key `(destination_id, attraction_id)`.
3. **Manage Tour Packages:** Full CRUD with positive duration and non-negative cost checks.
4. **Package Destinations:** Associate destinations with packages via `DESTINATION_PACKAGE`.
5. **Manage Bookings:** View all tourist bookings, filter records, update booking status.
6. **Manage Payments & Refunds:** Review all payments, update payment statuses (`Completed`, `Failed`, `Refunded`), and record refund details.
7. **View Users & Tourists:** View registered tourists and administrative accounts.
8. **Dashboard Statistics:** View system KPIs and revenue totals.
9. **Change Password & Logout.**

---

## 4. Setup and Installation

### 4.1. Prerequisites
- Java JDK 17 or later installed and configured on your system PATH.
- MySQL Server 8.0+ running on `localhost:3306` with existing `smart_tourism_db`.
- Eclipse IDE for Java Developers.

### 4.2. Database Configuration
1. Open [`db.properties`](file:///c:/Users/Sauu/eclipse-workspace/SmartTourismManagement/db.properties) in the project root:
   ```properties
   db.url=jdbc:mysql://localhost:3306/smart_tourism_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=root
   ```
2. Run the safe, non-destructive migration script to create `user_credential`:
   ```bash
   mysql -u root -p smart_tourism_db < schema_migration_credential.sql
   ```
   *(Or run it inside MySQL Workbench / MySQL CLI).*

### 4.3. Default Login Credentials

| Role | Name | Email | User ID | Password | Access Level |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Administrator** | Priya Patil | `priya@example.com` | `9002` | `admin123` | Full Management & Statistics |
| **Administrator** | Neha Rao | `neha@example.com` | `9012` | `admin123` | Full Management & Statistics |
| **Tourist** | Aarav Sharma | `aarav@example.com` | `9001` | `tourist123` | Browsing, Booking, Profile |
| **Tourist** | Rahul Verma | `rahul@example.com` | `9003` | `tourist123` | Browsing, Booking, Profile |

*Note: New tourists can also self-register at any time by clicking **"Register as Tourist"** on the login screen.*

---

## 5. How to Run the Application

### Option 1: In Eclipse IDE
1. Open Eclipse and choose your workspace (`c:\Users\Sauu\eclipse-workspace`).
2. If not already imported, select **File $\rightarrow$ Open Projects from File System...** $\rightarrow$ select `SmartTourismManagement`.
3. Press **F5** to refresh the project.
4. Right-click [`Main.java`](file:///c:/Users/Sauu/eclipse-workspace/SmartTourismManagement/src/smarttourism/Main.java) $\rightarrow$ **Run As $\rightarrow$ Java Application**.

### Option 2: Using the Batch / PowerShell Scripts
- In Command Prompt:
  ```cmd
  cd c:\Users\Sauu\eclipse-workspace\SmartTourismManagement
  run.bat
  ```
- In PowerShell:
  ```powershell
  cd c:\Users\Sauu\eclipse-workspace\SmartTourismManagement
  .\run.ps1
  ```

### Option 3: Terminal Single-File Execution
```powershell
cd c:\Users\Sauu\eclipse-workspace\SmartTourismManagement\src\smarttourism
java Main.java
```

---

## 6. Automated Testing Suite

To run the complete automated test suite verifying all 16 RBAC and business logic scenarios against your local database:

```powershell
cd c:\Users\Sauu\eclipse-workspace\SmartTourismManagement
java -cp "bin;lib\mysql-connector-j-8.3.0.jar;lib\jbcrypt-0.4.jar" smarttourism.test.SystemIntegrationTest
```

**Expected Result:**
```text
==================================================================
  Smart Tourism Management System — RBAC & Integration Test Suite
==================================================================
  [PASS] 1. Database Connection to MySQL smart_tourism_db
  [PASS] 2. Administrator Login with valid BCrypt credentials
  [PASS] 3. Invalid password rejected cleanly
  [PASS] 3b. Account without credentials receives clear administrator contact message
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
  Test Summary: 17 / 17 Tests Passed Successfully!
==================================================================
```

---

## 7. Capturing Screenshots

To automatically generate all lab report screenshots:

```powershell
cd c:\Users\Sauu\eclipse-workspace\SmartTourismManagement
java -cp "bin;lib\mysql-connector-j-8.3.0.jar;lib\jbcrypt-0.4.jar" smarttourism.test.ScreenshotCaptureUtil
```

The screenshots are saved in [`screenshots/`](file:///c:/Users/Sauu/eclipse-workspace/SmartTourismManagement/screenshots/):
1. `1_login_screen.png`: Login screen with email, password, and registration controls.
2. `2_admin_dashboard.png`: Administrator Dashboard with live KPIs and navigation.
3. `3_admin_destination_management.png`: Destination Management with Add/Edit/Delete buttons.
4. `4_admin_payment_management.png`: Payment & Refund Management.
5. `5_tourist_dashboard.png`: Tourist Portal with Browse, Book, My Bookings, My Payments, and My Profile tabs.
