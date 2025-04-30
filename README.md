# LibraTrack

A simple library management system application built with JavaFX and MySQL.

## Overview

LibraTrack is a desktop application for managing library operations. It supports two types of users: **Members** and **Admins**. Members can request to issue and return books, while Admins oversee users, inventory, and all borrowing activities.

## Features

### Member

- **Request Issue:** Submit requests to borrow books.
- **Request Return:** Submit requests to return borrowed books.

### Admin

- **Manage Users:** Create, update, and delete member accounts.
- **Manage Inventory:** Add, update, and remove books from the catalog.
- **Track Borrowers:** View and monitor all issued and returned books.
- **Issue & Return Books:** Approve or reject issue and return requests.

## Database Schema

LibraTrack uses MySQL to store all data. The main tables are:

### Key Tables

1. **user**: Stores account details for Admins and Members.
2. **books_inventory**: Contains information about all books in the library.
3. **issued_book_details**: Records details of books currently issued to members.
4. **issue_requests**: Tracks pending book issue requests from members.
5. **return_requests**: Tracks pending book return requests from members.

### ER Diagram

![ER Diagram](/images/RDBMS_ERD.png)

## Technology Stack

- **Java Version:** Java 21
- **Database:** MySQL 8
- **Build Tool:** Maven

## Usage

1. Launch the application.
2. **Admin Login** to manage users, inventory, and requests.
3. **Member Login** to browse catalog and submit requests.
4. Admin approves or rejects requests via the dashboard.

## Contributing

Contributions are welcome! Please:

1. Fork the repository.
2. Create a feature branch.
3. Submit a pull request.

