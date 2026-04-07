

```md
# 🚀 MindX Support AI

A mini full-stack customer support system built using Spring Boot, MySQL, and AI integration.  
This project simulates a real-world support platform where users can raise queries, receive AI responses, and manage support tickets.

---

## 🧠 Project Overview

MindX Support AI is designed to replicate a customer support workflow:

- Users submit queries
- System creates support tickets
- AI generates responses
- Conversations are stored
- Critical issues are escalated automatically

---

## ⚙️ Tech Stack

### 🔹 Backend
- Java
- Spring Boot
- Spring Data JPA
- Hibernate (ORM)

### 🔹 Database
- MySQL

### 🔹 Tools
- Postman (API testing)
- Git & GitHub

### 🔹 AI Integration
- OpenAI API (with fallback support)

---

## 🧱 Architecture

The application follows a layered architecture:

Controller → Service → Repository → Database

### 🔄 Flow:

User → Controller → Service → AI Service → Database → Response

---

## ✨ Features

### ✅ Ticket Management
- Create support tickets
- Store user queries

### ✅ AI-Based Response
- Generates dynamic responses using AI
- Fallback mechanism if API is unavailable

### ✅ Conversation System
- Stores both USER and AI messages
- Maintains complete chat history

### ✅ Escalation Logic
Automatically escalates critical queries:

Keywords:
- refund
- complaint
- angry

➡ Status changes to: `NEEDS_HUMAN`

---

## 🔌 API Endpoints

### Create Ticket
```

POST /tickets

````

Request:
```json
{
  "query": "Where is my order?"
}
````

Response:

```
AI-generated reply
```

---

### 📌 Get All Tickets

```
GET /tickets
```

---

### 📌 Get Ticket Details

```
GET /tickets/{id}
```

Returns:

* Ticket details
* Full conversation messages

---


## ⚙️ Setup Instructions

### 1️⃣ Clone Repository

```
git clone https://github.com/SANTHOSHDEV22/mindx-support-ai
```

---

### 2️⃣ Configure Database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/support_ai
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

---

### 3️⃣ Configure API Key (IMPORTANT)

```properties
openai.api.key=${OPENAI_API_KEY}
```

Set environment variable:

#### Windows:

```
setx OPENAI_API_KEY "your-api-key"
```

---

### 4️⃣ Run Application

Run:

```
SupportaiApplication.java
```

---

### 5️⃣ Test APIs

Use Postman:

* POST → /tickets
* GET → /tickets
* GET → /tickets/{id}

---

## 🧠 Key Concepts Used

* REST API design
* Layered architecture
* JPA & Hibernate (ORM)
* DTO pattern
* Enum usage
* AI integration (API + fallback)
* Keyword-based escalation logic

---

## ⚠️ Challenges Faced

* OpenAI API quota limitation
* Debugging API integration
* Handling fallback mechanism
* Ensuring consistent message flow

---

## 👨‍💻 Author

Santhoshkumar,
B.Tech Information Technology,
Rajalakshmi Engineering College

---

