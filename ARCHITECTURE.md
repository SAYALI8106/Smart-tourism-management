# Architecture Specification: Smart Tourism Management System (STMS)

**Course:** Database Management Systems (DBMS) Laboratory — Assignment 9  
**System:** Desktop Database Application in Java Swing, JDBC, and MySQL  
**Author:** Sayali Gayke (UCE2026005, Batch A4)

---

## 1. Architectural Overview

The **Smart Tourism Management System (STMS)** is architected strictly following the classical **Three-Tier Enterprise Architecture**:

```
+-------------------------------------------------------------+
|              TIER 1: PRESENTATION LAYER                     |
|           Package: smarttourism.presentation                |
|  Swing JFrames • UITheme • Form Validation Feedback • Tables|
+-------------------------------------------------------------+
                              │
               DTO / Models   ▼   Events & Actions
+-------------------------------------------------------------+
|          TIER 2: APPLICATION / BUSINESS LOGIC LAYER         |
|              Package: smarttourism.service                  |
| ValidationException • Input Rules • Dynamic Cost Calculator |
|    Trigger Constraints • Role Enforcement • Session Auth    |
+-------------------------------------------------------------+
                              │
               Java Objects   ▼   Data Access Invocations
+-------------------------------------------------------------+
|                 TIER 3: DATA ACCESS LAYER                   |
|                Package: smarttourism.dao                    |
| PreparedStatement • Try-With-Resources • Transactions (ACID)|
|       DBConnection • SQL Mappings • Stored Functions        |
+-------------------------------------------------------------+
                              │
               TCP Port 3306  ▼   SQL / ResultSet
+-------------------------------------------------------------+
|                 DATABASE ENGINE (MySQL 8.0)                 |
|                   Database: smart_tourism_db                |
|  USER • TOURIST • ADMINISTRATOR • DESTINATION • ATTRACTION  |
|  TOUR_PACKAGE • DESTINATION_PACKAGE • BOOKING • PAYMENT     |
+-------------------------------------------------------------+
```

---

## 2. Detailed Tier Specifications

### Tier 1: Presentation Layer (`smarttourism.presentation`)
**Core Principle:** The Presentation Layer is purely responsible for visual rendering, user interaction, capturing input, and displaying tabular data. **Presentation classes never write or execute SQL queries.**

- **`LoginForm.java`:** Entry authentication window displaying live MySQL health status and quick demo user selectors.
- **`Dashboard.java`:** Central control window featuring 6 KPI statistics cards, module navigation buttons, and a live Recent Bookings table.
- **`DestinationForm.java`:** Management interface for tourist destinations with search and tabular selection.
- **`AttractionForm.java`:** Management interface for tourist attractions respecting the composite primary key `(destination_id, attraction_id)`.
- **`PackageForm.java`:** Management interface for tour packages and the `DESTINATION_PACKAGE` many-to-many relationship.
- **`BookingForm.java`:** Booking interface featuring live automatic cost calculation (`cost × people`) and date validation.
- **`PaymentForm.java`:** Payment recording interface with method selection, booking linkages, and trigger-compliant amount validation.
- **`UserForm.java`:** Profile and role manager handling common `USER` and specialized `TOURIST`/`ADMINISTRATOR` records.
- **`UITheme.java`:** Centralized design system defining typography (Segoe UI), color palette (Teals, Slates, Ambers), and modern table renderers.

### Tier 2: Application / Business Layer (`smarttourism.service`)
**Core Principle:** Encapsulates all domain business rules, cross-field validations, calculation logic, and security assertions before data reaches the database.

- **`ValidationException.java`:** Custom exception type thrown on any rule violation, providing clear user-facing messages.
- **`AuthService.java`:** Handles authentication, password hashing (SHA-256), and session user state (`getCurrentUser()`, `isAdmin()`).
- **`UserService.java`:** Validates names, email format, phone digits (7-15), and delegates transactional user-role operations.
- **`DestinationService.java`:** Enforces destination naming rules and prevents deletion of destinations referenced by attractions.
- **`AttractionService.java`:** Validates composite keys, entry fees ($\ge 0$), and time formats (`HH:mm:ss`).
- **`PackageService.java`:** Enforces positive duration ($> 0$), non-negative cost ($\ge 0$), and coordinates destination mapping.
- **`BookingService.java`:**
  - Enforces `travel_date >= booking_date` (matches database check and trigger).
  - Enforces `number_of_people > 0`.
  - Enforces status domain: `Pending`, `Confirmed`, `Cancelled`, `Completed`.
  - Calculates estimated cost dynamically: `TOUR_PACKAGE.cost * BOOKING.number_of_people`.
- **`PaymentService.java`:**
  - Enforces amount $> 0$ (matches `BeforePaymentInsert` trigger).
  - Validates payment methods (`UPI`, `Cash`, `Credit Card`, `Debit Card`, `Net Banking`).
  - Validates payment statuses (`Pending`, `Completed`, `Failed`, `Refunded`).
- **`DashboardService.java`:** Coordinates aggregation queries and recent bookings retrieval.

### Tier 3: Data Access Layer (`smarttourism.dao`)
**Core Principle:** Directly interacts with MySQL using standard JDBC. Completely isolates SQL dialect, connections, and statement execution.

- **`DBConnection.java`:**
  - Reads configuration from `db.properties`, environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`), with fallback defaults.
  - Diagnostic error handler translating SQL errors into clear diagnostic messages.
- **Safe Prepared Statements:** Every query uses `?` placeholders, completely mitigating SQL Injection.
- **Resource Management:** Every `Connection`, `PreparedStatement`, and `ResultSet` is enclosed in Java `try-with-resources` blocks to prevent resource leaks.
- **ACID Transactions:** Operations affecting multiple related tables (e.g. `USER` + `TOURIST` in `UserDAO`) execute under `setAutoCommit(false)`, `commit()`, and `rollback()` in catch blocks.
- **Stored Procedures & Functions:** Leverages MySQL stored routines from Assignment 7 (`GetCompletedPaymentAmount`, `GetTouristBookings`).

---

## 3. Database Normalization (3NF) & Schema Compliance

The application connects to `smart_tourism_db` designed in Assignments 6 and 7:

| Table Name | Primary Key | Foreign Keys | Key Constraints & Triggers |
| :--- | :--- | :--- | :--- |
| `USER` | `user_id` | — | `email` UNIQUE, `phone` UNIQUE |
| `TOURIST` | `user_id` | `user_id` → `USER.user_id` | Specialization hierarchy |
| `ADMINISTRATOR` | `user_id` | `user_id` → `USER.user_id` | Specialization hierarchy |
| `DESTINATION` | `destination_id` | — | Non-null name, location |
| `ATTRACTION_TYPE`| `type` | — | Primary key on type name |
| `ATTRACTION` | `(destination_id, attraction_id)` | `destination_id` → `DESTINATION`<br>`type` → `ATTRACTION_TYPE` | Composite PK; `entry_fee >= 0` |
| `TOUR_PACKAGE` | `package_id` | — | `duration > 0`, `cost >= 0` |
| `DESTINATION_PACKAGE`| `(destination_id, package_id)` | `destination_id` → `DESTINATION`<br>`package_id` → `TOUR_PACKAGE`| Composite PK (M:N relationship) |
| `BOOKING` | `booking_id` | `tourist_id` → `TOURIST`<br>`package_id` → `TOUR_PACKAGE` | `number_of_people > 0`<br>`travel_date >= booking_date`<br>Trigger: `BeforeBookingInsert` |
| `PAYMENT` | `payment_id` | `booking_id` → `BOOKING`<br>`admin_id` → `ADMINISTRATOR` | `amount >= 0`<br>Trigger: `BeforePaymentInsert` (`amount > 0`) |

### Strict Normalization: No Derived `total_amount` in `BOOKING`
In Assignment 7, the `BOOKING` relation was normalized into strict **Third Normal Form (3NF)** by removing the derived `total_amount` column (which introduced a transitive dependency on `TOUR_PACKAGE.cost` and `BOOKING.number_of_people`).

In accordance with this design:
- STMS **never** executes `INSERT` or `UPDATE` on a non-existent `total_amount` column.
- The estimated amount is computed dynamically:
  ```java
  double estimatedTotal = tourPackage.getCost() * booking.getNumberOfPeople();
  ```
- In SQL queries, this is computed using:
  ```sql
  SELECT b.*, (p.cost * b.number_of_people) AS estimated_amount
  FROM booking b
  JOIN tour_package p ON b.package_id = p.package_id;
  ```

---

## 4. Design Patterns Applied

1. **DAO (Data Access Object) Pattern:** Completely decouples business services from database query syntax and JDBC drivers.
2. **Service Layer Pattern:** Centralizes application business logic, validations, and workflow orchestration.
3. **Data Transfer Object (DTO) / Entity Models:** Encapsulates domain data cleanly across layers using POJO models in `smarttourism.model`.
4. **Connection Factory Pattern:** Encapsulated in `DBConnection` with configurable properties and environment variable overrides.
5. **Unit of Work / Transaction Pattern:** Coordinates multi-table modifications (`UserDAO.insert`, `UserDAO.delete`) within transactional boundaries.
