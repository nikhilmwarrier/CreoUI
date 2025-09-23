# Creo - HTTP Client with Database Integration

A complete HTTP client application built in Java that mimics Postman functionality, with automatic database persistence.

---

## Features

This application allows you to:

- ✅ **HTTP Operations**: Send GET, POST, PUT, DELETE requests.  
- ✅ **Custom Headers**: Set headers like `Content-Type`, `Authorization`, etc.  
- ✅ **Request Body**: Send JSON, XML, or text in POST/PUT requests.  
- ✅ **Response Analysis**: View status codes, headers, body, and response time.  
- ✅ **Database Integration**: Automatically stores all requests and responses in SQLite.  
- ✅ **Simple Architecture**: Clean, beginner-friendly code structure.

---

## Project Structure

```
Creo/
├── PostmanApp.java           # Main application demonstrating functionality
├── SimpleHTTPClientUI.java   # Main UI application (Frontend)
├── backend/                  # Backend and database integration
│   ├── DBHandle.java         # Database connection and initialization
│   ├── HttpClientService.java# HTTP client service
│   ├── Request.java          # Request data model
│   ├── Response.java         # Response data model
│   ├── RequestsDAO.java      # Database operations for requests
│   └── ResponsesDAO.java     # Database operations for responses
├── database/
│   ├── oop.db                # SQLite database (auto-created)
├── lib/
│   └── sqlite-jdbc-3.50.3.0.jar # SQLite JDBC driver
└── README.md                 # This file
```

---

## How to Run

### Compile

```bash
# Compile all Java files with SQLite JDBC driver
javac -cp "lib/sqlite-jdbc-3.50.3.0.jar;." backend/*.java *.java
```

### Run Application

```bash
# Run the main app
java -cp "lib/sqlite-jdbc-3.50.3.0.jar;." PostmanApp

# Or run the simple UI directly
java -cp "lib/sqlite-jdbc-3.50.3.0.jar;." SimpleHTTPClientUI
```

> Note: On Linux/macOS, replace `;` with `:` in the classpath.

---

## Demo Usage

- **GET Request**:  
  - URL: `https://jsonplaceholder.typicode.com/posts/1`  
  - Method: GET  

- **POST Request**:  
  - URL: `https://jsonplaceholder.typicode.com/posts`  
  - Method: POST  
  - Headers: `Content-Type: application/json`  
  - Body:
    ```json
    {
      "title": "My Post",
      "body": "Post content",
      "userId": 1
    }
    ```

- **Response**: Displays status code, headers, body, and saves to database.  
- **Database**: Requests and responses are automatically stored for verification.

---

## Key Classes

### Frontend (UI Layer)
- **Technology**: Java Swing  
- **Purpose**: Provides user interface for entering requests and viewing responses.  
- **Components**: URL field, method dropdown, headers area, body area, send button, response display.

### Backend (Business Logic)
- **Technology**: Java HTTP Client (built-in since Java 11)  
- **Purpose**: Handles actual HTTP communication.  
- **Class**: `HttpClientService` – executes HTTP requests, parses responses, handles errors.  
- **Integration**: `PostmanBackendService` combines HTTP operations with database storage.

### Database Layer
- **Technology**: SQLite  
- **Purpose**: Stores request and response history.  
- **Classes**: `DBHandle`, `RequestsDAO`, `ResponsesDAO`  
- **Tables**:
  - **Requests**: ID, Method, URL, Headers, Body, Timestamp
  - **Responses**: ID, Request_ID, Status_Code, Headers, Body, Content_Type, Timestamp

---

## Architecture

- **Pattern**: Three-tier architecture (Presentation → Business Logic → Data)  
- **Benefits**: Separation of concerns, maintainable code, easy to extend.

---

## College Project Benefits

- Demonstrates **GUI development**  
- Implements **HTTP protocol knowledge**  
- Includes **database integration**  
- Uses **object-oriented programming**  
- Shows **MVC-like architecture**  

---

**Created by**: College Project Team  
**Language**: Java 11+  
**Database**: SQLite  
**Purpose**: Learn HTTP client development with database integration  
