# 🌍 GeoMind AI — Geoscience RAG Assistant

An AI-powered **Geoscience Knowledge Assistant** that uses **Retrieval-Augmented Generation (RAG)** to answer questions from uploaded geoscience documents.

GeoMind AI combines **Java Spring Boot, PostgreSQL, Qdrant, local embeddings, and Google Gemini** to retrieve relevant information from documents and generate grounded answers with source citations.

---

## 📌 Project Overview

**GeoMind AI** is designed to provide a question-answering system specifically for geoscience-related knowledge.

Users can upload geoscience documents such as PDFs. The application extracts the document text, divides it into smaller chunks, generates embeddings, stores the embeddings in Qdrant, and retrieves the most relevant information when a user asks a question.

The retrieved context is then provided to Gemini to generate the final response.

### Basic RAG Pipeline

```text
PDF Document
     │
     ▼
Text Extraction
     │
     ▼
Automatic Chunking
     │
     ▼
Embedding Generation
     │
     ▼
Qdrant Vector Database
     │
     │
     ▼
User Question
     │
     ▼
Question Embedding
     │
     ▼
Similarity Search
     │
     ▼
Relevant Document Chunks
     │
     ▼
Gemini
     │
     ▼
Answer + Source Citations
```

---

# ✨ Features

* 📄 PDF document processing
* ✂️ Automatic text chunking
* 🧠 Local embedding generation
* 🔎 Semantic similarity search
* 🗃️ Qdrant vector database integration
* 💬 Chat with uploaded documents
* 🤖 Google Gemini integration
* 📚 Source citations
* 🗄️ PostgreSQL database
* 🌐 Web-based AI chat interface
* 📖 Chat history interface
* 📑 Knowledge document management
* 🔐 Environment-variable based API key configuration
* 🧩 Modular Spring Boot architecture
* ☁️ Architecture prepared for future AWS deployment
* 👥 Designed to be extended for multi-user document isolation

---

# 🏗️ System Architecture

```text
                         ┌──────────────────────┐
                         │       User           │
                         │   Web Application    │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     HTML / CSS / JS  │
                         │      Frontend        │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │    Spring Boot API   │
                         └──────────┬───────────┘
                                    │
                ┌───────────────────┼───────────────────┐
                │                   │                   │
                ▼                   ▼                   ▼
       ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
       │ Document       │  │ RAG Service    │  │ Search Service │
       │ Service        │  │                │  │                │
       └───────┬────────┘  └───────┬────────┘  └────────────────┘
               │                   │
               ▼                   ▼
       ┌────────────────┐  ┌────────────────┐
       │ PDF Processing │  │ Embedding      │
       │ PDFBox         │  │ Service        │
       └───────┬────────┘  └───────┬────────┘
               │                   │
               ▼                   ▼
       ┌────────────────┐  ┌────────────────┐
       │ Chunk Service  │  │    Qdrant      │
       └───────┬────────┘  │ Vector Database│
               │           └───────┬────────┘
               ▼                   │
       ┌────────────────┐          │
       │  PostgreSQL    │◄─────────┘
       │    Database    │
       └────────────────┘
                                   │
                                   ▼
                          ┌──────────────────┐
                          │  Retrieved       │
                          │  Context         │
                          └────────┬─────────┘
                                   │
                                   ▼
                          ┌──────────────────┐
                          │  Google Gemini   │
                          │       AI         │
                          └────────┬─────────┘
                                   │
                                   ▼
                          ┌──────────────────┐
                          │ Answer + Sources │
                          └──────────────────┘
```

---

# 🔄 How RAG Works in GeoMind AI

## 1. PDF Upload

A geoscience PDF is uploaded into the application.

Example:

```text
geosciences.pdf
```

---

## 2. Text Extraction

Apache PDFBox extracts readable text from the uploaded PDF.

```text
PDF
 ↓
Extracted Text
```

---

## 3. Automatic Chunking

The extracted text is divided into smaller sections called **chunks**.

For example:

```text
Chunk 0
Chunk 1
Chunk 2
Chunk 3
...
Chunk N
```

Each chunk is associated with its document.

---

## 4. Embedding Generation

Each chunk is converted into a numerical vector representation.

```text
Text Chunk
     ↓
Embedding Model
     ↓
Vector
```

These vectors allow the application to perform semantic similarity searches.

---

## 5. Qdrant Storage

The generated vectors are stored in **Qdrant**.

Qdrant is used as the vector database for similarity search.

```text
Document Chunk
      +
Embedding Vector
      ↓
    Qdrant
```

---

## 6. User Question

The user asks a question through the GeoMind AI interface.

Example:

```text
What is geophysics?
```

---

## 7. Similarity Search

The question is converted into an embedding.

GeoMind searches Qdrant for the most relevant document chunks.

Example:

```text
Question
   ↓
Question Embedding
   ↓
Qdrant Similarity Search
   ↓
Top Relevant Chunks
```

---

## 8. Context Retrieval

The retrieved chunks are combined to create context for the AI model.

```text
Retrieved Chunk 1
Retrieved Chunk 2
Retrieved Chunk 3
        ↓
     Context
```

---

## 9. Gemini Generation

The retrieved context and user question are sent to Google Gemini.

Gemini generates the final response using the retrieved document information.

---

## 10. Source Citations

The response contains the relevant document and chunk information.

Example:

```text
📚 Sources

- geosciences.pdf | Chunk 3
- geosciences.pdf | Chunk 0
- geosciences.pdf | Chunk 31
```

This allows users to identify where the retrieved information came from.

---

# 🛠️ Technology Stack

## Backend

* Java 17
* Spring Boot
* Spring Web MVC
* Spring Data JPA
* Hibernate
* Maven

## Artificial Intelligence

* Google Gemini API
* Retrieval-Augmented Generation
* Embedding generation
* ONNX Runtime
* Hugging Face Tokenizers

## Vector Database

* Qdrant

## Relational Database

* PostgreSQL

## Document Processing

* Apache PDFBox

## Frontend

* HTML5
* CSS3
* JavaScript
* Bootstrap 5
* Bootstrap Icons

## Development Tools

* IntelliJ IDEA
* Maven
* Git
* GitHub

---

# 📂 Project Structure

```text
Geoscience-RAG-Assistant/
│
├── .mvn/
│   └── wrapper/
│
├── src/
│   │
│   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── org/example/geoscienceragassistant/
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── QdrantConfig.java
│   │   │       │   └── QdrantInitializer.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── DocumentController.java
│   │   │       │   ├── EmbeddingController.java
│   │   │       │   ├── RagController.java
│   │   │       │   ├── SearchController.java
│   │   │       │   └── TestGeminiController.java
│   │   │       │
│   │   │       ├── embedding/
│   │   │       │   └── EmbeddingService.java
│   │   │       │
│   │   │       ├── ingestion/
│   │   │       │   └── DocumentIngestionService.java
│   │   │       │
│   │   │       ├── model/
│   │   │       │   ├── Document.java
│   │   │       │   ├── DocumentChunk.java
│   │   │       │   └── RagResponse.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── DocumentRepository.java
│   │   │       │   └── DocumentChunkRepository.java
│   │   │       │
│   │   │       └── service/
│   │   │           ├── ChunkService.java
│   │   │           ├── DocumentService.java
│   │   │           ├── GeminiService.java
│   │   │           ├── PdfService.java
│   │   │           ├── QdrantService.java
│   │   │           └── RagService.java
│   │   │
│   │   └── resources/
│   │       │
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── documents.html
│   │       │   ├── history.html
│   │       │   ├── script.js
│   │       │   ├── documents.js
│   │       │   ├── history.js
│   │       │   └── style.css
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│
├── .gitignore
├── .gitattributes
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

# 🌐 Frontend

GeoMind AI provides a simple web interface with three main sections.

### AI Assistant

```text
AI Assistant
```

Used to ask questions and receive AI-generated answers.

### Documents

```text
Documents
```

Used to manage knowledge documents.

### Chat History

```text
Chat History
```

Used to view previous conversations.

---

# 🔌 API Endpoints

## RAG API

```http
GET /api/rag?question={question}
```

Example:

```text
/api/rag?question=What%20is%20geophysics?
```

The endpoint retrieves relevant document chunks and generates an answer.

---

## Documents API

### Get documents

```http
GET /api/documents
```

### Add document

```http
POST /api/documents
```

### Delete document

```http
DELETE /api/documents/{id}
```

---

# 📚 Example

### User Question

```text
What is geophysics?
```

### GeoMind AI

```text
Based on the provided context, geophysics is a branch
of the geosciences that investigates physical Earth
processes, such as earthquakes, and images the interior
of the Earth through surface-based physical measurements.
```

### Sources

```text
📚 Sources

- geosciences.pdf | Chunk 3
- geosciences.pdf | Chunk 0
- geosciences.pdf | Chunk 31
- geosciences.pdf | Chunk 16
- geosciences.pdf | Chunk 2
```

---

# ⚙️ Configuration

GeoMind AI uses external services that need to be configured locally.

Required services include:

```text
PostgreSQL
Qdrant
Gemini API
```

Sensitive credentials should **not** be stored directly in the GitHub repository.

---

# 🔐 Environment Variables

The Gemini API key should be provided through an environment variable.

### Windows PowerShell

```powershell
$env:GEMINI_API_KEY="YOUR_API_KEY"
```

The application reads the key using:

```java
System.getenv("GEMINI_API_KEY")
```

### Important

Never commit your actual API key to GitHub.

Do not put:

```text
GEMINI_API_KEY=AIza...
```

directly into a committed configuration file.

---

# 🗃️ Local Model Files

The project may use local model files for embedding generation.

The `models/` directory is intentionally excluded from Git using `.gitignore`.

```text
models/
```

This prevents large model files from being unnecessarily uploaded to the repository.

A developer setting up the project should obtain/configure the required model separately.

---

# ▶️ Running the Application

## 1. Clone the Repository

```bash
git clone https://github.com/sumit-1137/Geoscience-RAG-Assistant.git
```

Then:

```bash
cd Geoscience-RAG-Assistant
```

---

## 2. Configure PostgreSQL

Create/configure the PostgreSQL database required by the application.

Update the local database configuration according to your environment.

---

## 3. Start Qdrant

Run your Qdrant instance and make sure the application can connect to it.

---

## 4. Configure Gemini API

Set your Gemini API key.

Windows PowerShell:

```powershell
$env:GEMINI_API_KEY="YOUR_API_KEY"
```

---

## 5. Configure the Embedding Model

Place/configure the required local embedding model according to the project's embedding service configuration.

The model directory is not included in GitHub.

---

## 6. Build the Project

Using Maven Wrapper:

```powershell
.\mvnw.cmd clean package
```

---

## 7. Run the Application

```powershell
.\mvnw.cmd spring-boot:run
```

---

## 8. Open the Application

Open:

```text
http://localhost:8080
```

---

# 🔒 Security

The following items should not be committed to GitHub:

```text
API keys
Passwords
Database credentials
.env files
Local model files
target/
IDE configuration
```

The repository's `.gitignore` excludes sensitive and unnecessary files.

---

# 🧪 Current Development Status

The current version supports the core RAG workflow:

```text
PDF
 ↓
Text Extraction
 ↓
Chunking
 ↓
Embedding
 ↓
Qdrant
 ↓
Similarity Search
 ↓
Retrieved Context
 ↓
Gemini
 ↓
Answer
 ↓
Source Citations
```

The web interface is also connected to the Spring Boot backend.

---

# 🚧 Future Improvements

The following features can be developed further:

* 👤 User authentication
* 🔐 User-specific document access
* 👥 Complete multi-user architecture
* 💾 Persistent database-backed chat history
* ☁️ AWS deployment
* 🐳 Docker containerization
* 📊 RAG evaluation metrics
* 🔍 Improved retrieval strategies
* ⚡ Streaming AI responses
* 📄 Support for additional document formats
* 📈 Monitoring and logging
* 🔒 Production-level security
* 🚀 Scalable cloud architecture

---

# ☁️ Future AWS Architecture

The application can be extended to a cloud deployment architecture such as:

```text
                    Internet
                       │
                       ▼
                ┌──────────────┐
                │     AWS      │
                │ Load Balancer│
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │ Spring Boot  │
                │ Application  │
                └──────┬───────┘
                       │
             ┌─────────┼─────────┐
             │         │         │
             ▼         ▼         ▼
        PostgreSQL  Qdrant    Gemini API
```

This architecture is a future deployment direction and is not claimed as the current production deployment.

---

# 🎓 Academic Project

**Project Title:**

> **AI-Powered Geoscience Knowledge Assistant Using Retrieval-Augmented Generation**

**Project Name:**

> **GeoMind AI**

The project focuses on applying Retrieval-Augmented Generation to geoscience knowledge retrieval and question answering.

---

# 👨‍💻 Author

**Sumit Chavan**

GitHub:

[https://github.com/sumit-1137](https://github.com/sumit-1137)

Repository:

[https://github.com/sumit-1137/Geoscience-RAG-Assistant](https://github.com/sumit-1137/Geoscience-RAG-Assistant)

---

# 📜 License

This project is currently intended as an academic/development project.

A formal open-source license can be added in the future if the project is released for broader use.

---

## ⭐ GeoMind AI

```text
        🌍
      GeoMind
     ─────────
   Geoscience AI
```

**AI-powered knowledge retrieval for geoscience research.**
