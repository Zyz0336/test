# Booking System Backend API Documentation

This repository contains the Spring Boot backend for the Service Booking Management System. It provides robust RESTful APIs for user authentication, customer bookings, specialist schedule management, and an administrative dashboard.

---

## 🚀 Quick Start Guide

### 1. Prerequisites
* **Java:** JDK 17 or higher
* **Maven:** 3.6.0+
* **Database:** MySQL 8.0+

### 2. Database Configuration
Before running the application, ensure the corresponding database is created in your local MySQL instance:

```sql
CREATE DATABASE cpt202_temp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configure the `src/main/resources/application.properties` file to ensure your database credentials are correct:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cpt202_temp?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_database_password
# Recommended for the first run to auto-generate tables
spring.jpa.hibernate.ddl-auto=update 
```

### 3. Running the Application
Open a terminal in the root directory of the project and execute the following Maven commands:

```bash
# Clean and package the application
mvn clean package -DskipTests

# Start the Spring Boot application
mvn spring-boot:run
```
> The server will start on `http://localhost:8080` by default.

---

## 📖 Global API Standard

* **Base URL:** `/api`
* **Content-Type:** `application/json`
* **Global Response Format:**

```json
{
  "code": 200,
  "message": "Success message or error detail",
  "data": { ... } 
}
```
*(Note: `data` is present only when the request is successful and returns data)*

---

## 🔐 1. Auth API
**Base Path:** `/api/auth`

| Endpoint Description | Method | Path | Request Parameters (Body) | Success Response |
| :--- | :--- | :--- | :--- | :--- |
| **User Registration** | `POST` | `/register` | `email`, `password`, `role`<br>*(Admin requires `inviteCode`; Specialist requires `expertise`, `certLink`)* | Registration successful |
| **User Login** | `POST` | `/login` | `email`, `password`, `role` | Returns `token`, `role`, `nickname` |

---

## 👤 2. User API (Common)
**Base Path:** `/api/user`

| Endpoint Description | Method | Path | Request Parameters (Query/Body) | Success Response |
| :--- | :--- | :--- | :--- | :--- |
| **Get Profile** | `GET` | `/profile` | Query: `email`, `role` | Returns the corresponding User/Admin/Specialist entity |
| **Update Profile** | `PUT` | `/profile` | Body: `email`, `role`, `nickname`, `bio` | Profile updated successfully |
| **Apply for Promotion** | `POST` | `/apply-tier` | Body: `email`, `targetLevel`, `reason` | Promotion application submitted |
| **Get Promotion Status**| `GET` | `/promotion-status`| Query: `email` | Returns the latest TierApplication record |

---

## 🧑‍💻 3. Customer API
**Base Path:** `/api/customer`

| Endpoint Description | Method | Path | Request Parameters (Query/Body) | Success Response |
| :--- | :--- | :--- | :--- | :--- |
| **Get Active Specialists** | `GET` | `/specialists` | None | Returns a list of Specialists with Status="Active" |
| **Get Specialist Slots** | `GET` | `/specialists/slots` | Query: `email` (Specialist's email) | Returns "Available" schedule slots |
| **Create Booking** | `POST` | `/bookings/create` | Body: `customerEmail`, `specialistEmail`, `specialistName`, `slotTime`, `topic`, `notes`, `fee`, `slotId` | Booking created, corresponding slot locked |
| **Get My Bookings** | `GET` | `/bookings` | Query: `email` (Customer's email) | Returns all bookings for the customer |
| **Cancel Booking** | `PUT` | `/bookings/{id}/cancel`| Path: `id` (Booking ID) | Booking cancelled, slot released, refund triggered |

---

## 💼 4. Specialist API
**Base Path:** `/api/specialist`

| Endpoint Description | Method | Path | Request Parameters (Query/Body) | Success Response |
| :--- | :--- | :--- | :--- | :--- |
| **Get Assigned Bookings** | `GET` | `/bookings` | Query: `email` (Specialist's email) | Returns all bookings assigned to the specialist |
| **Confirm Booking** | `PUT` | `/bookings/{id}/confirm` | Path: `id` | Status updated to "Confirmed" |
| **Reject Booking** | `PUT` | `/bookings/{id}/reject` | Path: `id` | Status updated to "Cancelled" |
| **Complete Booking** | `PUT` | `/bookings/{id}/complete`| Path: `id` | Status updated to "Completed" |
| **Cancel with Reason** | `PUT` | `/bookings/{id}/cancel-with-reason` | Path: `id`, Body: `cancelReason` | Booking cancelled with reason, marks cancel source |
| **Get My Schedule Slots** | `GET` | `/slots` | Query: `email` | Returns all slots for the specialist |
| **Publish Schedule Slot** | `POST` | `/slots/create` | Body: ScheduleSlot entity properties | Published successfully; returns 400 if time conflict |
| **Delete Unbooked Slot** | `DELETE`| `/slots/{id}` | Path: `id` (Slot ID) | Slot deleted (cannot delete booked slots) |

---

## 🛡️ 5. Admin API
**Base Path:** `/api/admin`

| Endpoint Description | Method | Path | Request Parameters (Query/Body) | Success Response |
| :--- | :--- | :--- | :--- | :--- |
| **Dashboard Statistics** | `GET` | `/dashboard-stats`| None | Returns total bookings, total income, and specialist performance |
| **Get Users by Role** | `GET` | `/users` | Query: `role` | Returns a list of Users by role |
| **Update Account Status** | `PUT` | `/users/{id}/status`| Path: `id`, Query: `role`, Body: `status` | Account status updated |
| **Delete Account** | `DELETE`| `/users/{id}` | Path: `id`, Query: `role` | Account permanently deleted |
| **Get All Bookings** | `GET` | `/bookings` | None | Returns all platform bookings |
| **Get Pending Specialists**| `GET` | `/specialists/pending`| None | Returns specialists awaiting approval |
| **Audit Specialist** | `PUT` | `/specialists/{id}/audit`| Path: `id`, Body: `action` (Approve/Reject) | Specialist status updated to Active or Rejected |
| **Get Pending Promotions**| `GET` | `/tier-applications/pending`| None | Returns Tier applications submitted by specialists |
| **Review Promotion** | `PUT` | `/tier-applications/{id}/review`| Path: `id`, Body: `status`, `comment`| Approved/Rejected, automatically updates specialist tier and fee |
| **Income Summary Report** | `GET` | `/income-summary` | None | Returns total platform income grouped by specialist |
| **Update Specialist Info**| `PUT` | `/specialists/{id}/info`| Path: `id`, Body: `level`, `fee`, `expertise` | Force update specialist profile via backend |
| **Force Cancel Booking** | `PUT` | `/bookings/{id}/force-cancel`| Path: `id`, Body: `cancelReason` | Force cancel booking and automatically release associated slot |
| **Generate Invite Code** | `GET` | `/generate-invite` | None | Generates a one-time invite code starting with `ADMIN-` |
