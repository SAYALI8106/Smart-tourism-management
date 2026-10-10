# Manual and Automated Testing Checklist — Assignment 9

**Project:** Smart Tourism Management System (STMS)  
**Student:** Sayali Gayke (UCE2026005, Batch A4)  
**Database:** `smart_tourism_db` on MySQL Server 8.0  

---

## 1. Automated Verification Suite (`SystemIntegrationTest.java`)

The automated integration test suite directly connects to the live MySQL database, performs transactions, and exercises every business rule and RBAC security guard.

### Test Execution Output (Executed Live on System)
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

## 2. Interactive Manual Testing Checklist

Use this checklist during laboratory evaluation or live demonstration:

### Module 1: Authentication & RBAC Login
| Test ID | Action | Expected Result | Status |
| :--- | :--- | :--- | :--- |
| **TC-01** | Launch application via `Main.java` | Status indicator displays green: `System Status: Online (MySQL smart_tourism_db)` with clean, empty input fields | Verified |
| **TC-02** | Enter invalid password for `priya@example.com` | Error dialog: `Invalid credentials. Please verify your email/ID and password.` | Verified |
| **TC-03** | Attempt login with unconfigured database user | Error dialog: `This account has not been configured for login. Please contact the administrator.` | Verified |
| **TC-04** | Login as Admin `priya@example.com` / `admin123` | Redirects to **Administrator Dashboard** with KPI cards and management modules | Verified |
| **TC-05** | Login as Tourist `aarav@example.com` / `tourist123` | Redirects to **Tourist Dashboard** with Browse, Book, My Bookings, My Payments tabs | Verified |
| **TC-06** | Click `Register as Tourist` on Login screen | Opens registration modal dialog | Verified |
| **TC-07** | Register new tourist with valid details & password | Creates `USER`, `TOURIST`, and `USER_CREDENTIAL` with BCrypt hash; confirms success | Verified |
| **TC-08** | Attempt to register with duplicate email | Error dialog: `An account with email ... is already registered.` | Verified |

### Module 2: Tourist Dashboard & Permitted Operations
| Test ID | Action | Expected Result | Status |
| :--- | :--- | :--- | :--- |
| **TC-09** | Open `Browse Destinations` tab | Displays destinations table with search filter | Verified |
| **TC-10** | Search for `Goa` or `Beach` | Filters table to matching destinations | Verified |
| **TC-11** | Click `View Attractions for Selected` | Automatically opens `View Attractions` tab filtered to that destination | Verified |
| **TC-12** | Open `Explore Tour Packages` tab | Displays package name, duration, cost, and included destinations | Verified |
| **TC-13** | Click `Book Selected Package` | Navigates to `Book a Package` tab with selected package pre-filled | Verified |
| **TC-14** | Select package and change number of people | Calculated total dynamically updates (`cost × people`) in real-time | Verified |
| **TC-15** | Click `Confirm & Submit Booking` | Prompts confirmation; creates booking for active tourist; switches to `My Bookings` | Verified |
| **TC-16** | View `My Bookings` tab | Displays only bookings belonging to this tourist | Verified |
| **TC-17** | Click `Cancel Selected Booking` | Prompts confirmation; updates booking status to `Cancelled` | Verified |
| **TC-18** | View `My Payments` tab | Displays only payments associated with this tourist's bookings | Verified |
| **TC-19** | Edit profile fields in `My Profile` and save | Updates user contact details; cannot modify role or user ID | Verified |
| **TC-20** | Click `Change Password` in header | Requires current password; validates confirmation; updates BCrypt hash | Verified |

### Module 3: Service-Layer Security & Restricted Operations
| Test ID | Action | Expected Result | Status |
| :--- | :--- | :--- | :--- |
| **TC-21** | Tourist invokes `DestinationService.addDestination()` | Throws `ValidationException: Access Denied: Administrator privileges are required...` | Verified |
| **TC-22** | Tourist invokes `PackageService.addPackage()` | Throws `ValidationException: Access Denied: Administrator privileges are required...` | Verified |
| **TC-23** | Tourist invokes `BookingService.getAllBookings()` | Throws `ValidationException: Access Denied: Administrator privileges are required...` | Verified |
| **TC-24** | Tourist invokes `PaymentService.updateStatus()` | Throws `ValidationException: Access Denied: Administrator privileges are required...` | Verified |
| **TC-25** | Tourist invokes `BookingService.getBookingById()` with another user's booking ID | Throws `ValidationException: Access Denied: You cannot view or modify another user's records.` | Verified |
| **TC-26** | Tourist attempts booking with forged `tourist_id` | Service layer overrides ID with `SessionManager.getCurrentUserId()` | Verified |

### Module 4: Administrator Management Capabilities
| Test ID | Action | Expected Result | Status |
| :--- | :--- | :--- | :--- |
| **TC-27** | Open Destination Management from Admin Dashboard | Displays full CRUD form with Add, Update, Delete buttons | Verified |
| **TC-28** | Add a new Destination as Administrator | Successfully inserts into MySQL `destination` table | Verified |
| **TC-29** | Attempt to delete Destination referenced by attractions | Prevented with clear message: `Referenced by attractions... Delete associated records first.` | Verified |
| **TC-30** | Open Attraction Management | Displays composite key form `(destination_id, attraction_id)` and allows full CRUD | Verified |
| **TC-31** | Open Package Management | Allows managing packages and associating destinations via `DESTINATION_PACKAGE` | Verified |
| **TC-32** | Open Payment Management | Displays all payments; allows updating status (`Completed`, `Refunded`); records refund info | Verified |
| **TC-33** | Open User Management | Displays all registered Tourists and Administrators | Verified |
| **TC-34** | Click `Logout` from Admin or Tourist Dashboard | Clears session; subsequent operations blocked until re-authenticated | Verified |
