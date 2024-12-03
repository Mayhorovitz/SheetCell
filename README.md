
# Dynamic Spreadsheet Application

## Overview
This project is a **Java-based client-server application** that offers a dynamic spreadsheet environment similar to Excel, facilitating **real-time data manipulation, analysis, and visualization**. 

Built with **JavaFX** for the user interface and powered by a backend server using **Tomcat** and **OkHttp**, the system supports **multiple users** working concurrently on shared sheets. Key features include:
- **User-specific access control**
- **Version management**
- **Dynamic analysis**


---

## Table of Contents
1. [Features](#features)
    - [Core Functionalities](#core-functionalities)
    - [Advanced Features](#advanced-features)
2. [Architecture](#architecture)
3. [Technologies Used](#technologies-used)
4. [Installation](#installation)


---

## Features

### Core Functionalities
### login screen
![login](https://github.com/Mayhorovitz/sheetCell/blob/master/images/login.png)

#### User-Specific Access Control
- **Role-based permissions**: Owner, Write, Read, and None.
- Owners can manage permissions, approve or reject access requests.
- Users can view their current permissions and request upgrades.

#### Spreadsheet Management
- Load sheets from **XML files**.
- Sheets are displayed in a table with details like name, owner, size, and user access level.
- Open sheets in **read-only** or **editable** modes based on permissions.
- **Access Permissions Table** shows the permission status for each user on a selected sheet.

![sheetsManagment](https://github.com/Mayhorovitz/sheetCell/blob/master/images/sheetsManagment.png)

#### Dynamic Spreadsheet Interface
- Interactive **grid layout** for intuitive data entry and manipulation.
- **Real-time updates** with automatic recalculations.
- Support for **formulas** and complex cell functions.
- **Action Line Display** provides detailed cell information, including original and effective values.

#### Version Control
- Track changes with an **integrated versioning system**.
- View previous versions and revert if necessary.
- Collaborative editing with **version synchronization** to ensure all users have the latest data.

#### Dependency Management
- Visualize cell dependencies and influences through **color-coded highlights**.
- Automatic recalculation of dependent cells to maintain data integrity.

![sheetView](https://github.com/Mayhorovitz/sheetCell/blob/master/images/sheetView.png)
---

### Advanced Features

#### Data Sorting and Filtering
- **Multi-criteria sorting** across multiple columns.
- Advanced filtering options to display relevant data based on dynamic criteria.
- Range selection for focused data manipulation.

#### Range Management
- Add, display, and delete **ranges** to organize data effectively.
- Highlighted ranges for easy identification within the sheet.

#### Dynamic Analysis
- Perform **trend analysis** on selected data ranges.
- Customize parameters like range and step size for iterative calculations.
- Visualize how changes impact dependent cells in real-time **without affecting the original data**.


#### Expression Handling
- Support for arithmetic functions (e.g., `PLUS`, `MINUS`, `TIMES`, `DIVIDE`).
- Logical functions (e.g., `IF`, `AND`, `OR`) for conditional operations.
- String functions (e.g., `CONCAT`, `SUB`) for text manipulation.
- **Nested functions** enable advanced expressions and calculations.

---
## Architecture
### Server-Side
- **Java Servlets** handle requests and manage session data.
- **Data Persistence** maintains session states and version histories.
- **Tomcat Server** processes client requests.

### Client-Side
- **JavaFX** provides a responsive and interactive user interface.
- **FXML** is used for designing UI components across different screens.
- **OkHttp** manages HTTP requests for CRUD operations and sheet updates.
- **Gson** handles JSON serialization and deserialization.

---

## Technologies Used
- **Programming Language**: Java 11 or higher
- **User Interface**: JavaFX
- **Backend Server**: Apache Tomcat
- **Libraries**:
  - OkHttp for HTTP communication
  - Gson for JSON processing
- **Version Control**: Git and GitHub

---

## Installation

### Prerequisites
- **Java Development Kit (JDK)** 11 or higher installed.
- **Apache Tomcat server** for deploying the backend.
- An IDE like **IntelliJ IDEA** or **Eclipse**, configured for JavaFX.

### Steps

#### Clone the Repository
```bash
git clone https://github.com/your-username/your-repository.git
```
#### Build the Project
Using Maven:
```bash
mvn clean install
```
Using Gradle:
```bash
gradle build
```
---

### Set Up the Server
1. Deploy the server WAR file to your **Apache Tomcat** instance.
2. Configure the server URL in the client application to point to your Tomcat server (e.g., `http://localhost:8080`).

---

### Configure the Client
1. Ensure all dependencies, including **JavaFX**, **OkHttp**, and **Gson**, are properly set up in your IDE.
2. Adjust the client configuration to match your server settings if necessary.

---

## Usage

### Running the Server
1. Start **Apache Tomcat**.
2. Deploy the backend application to your Tomcat server.
3. Verify that the server is running by accessing:
   ```plaintext
   http://localhost:8080
   ```
   ### Running the Client
- Launch the client application by running the `AppMain` class in your IDE.
- To simulate multiple users, launch additional instances of the client application.



