# Eco-Socratic

```markdown
# 🌿 Green Socratic Desktop

An offline, local-first Socratic tutoring desktop application built with **Java 17**, **JavaFX**, **SQLite (FTS5)**, and **local LLM inference (Phi-3 Mini via Ollama & LangChain4j)**.

Instead of giving direct answers or writing code for students, **Green Socratic Desktop** acts as a Socratic mentor—enforcing a deterministic rule engine in Java that responds with **exactly 3 probing questions** alongside offline study resources.

---

## ✨ Key Features & Design Goals

* **100% Offline & Local Inference:** Connects to a local Ollama instance (`http://localhost:11434`) with zero external cloud API calls or data telemetry.
* **Energy-Conscious Design:** Designed for local CPU execution using the 4-bit quantized `phi3:mini` model (~2.2 GB), targeting low-power local sessions (~0.005 kWh estimated per session) rather than remote cloud GPU clusters.
* **Deterministic Socratic Rule Engine:** Validates LLM responses in pure Java (`SocraticRuleEngine.java`) using multiline regular expressions to guarantee every turn presents strictly 3 numbered questions.
* **Offline Full-Text Search (SQLite FTS5):** Uses SQLite's embedded `fts5` virtual table extension via `sqlite-jdbc` for fast local keyword queries (`MATCH`) across study materials.
* **Non-Blocking Responsive UI:** Executes all LLM calls off the JavaFX Application Thread using `CompletableFuture.supplyAsync()` and updates the UI safely via `Platform.runLater()`.

---

## 🛠️ Verified Tech Stack

| Layer | Technology / Artifact | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language & Runtime** | Java Development Kit (JDK) | `17` | Core application runtime and concurrency (`CompletableFuture`) |
| **Desktop GUI** | OpenJFX (`javafx-controls`, `javafx-fxml`) | `17.0.10` | Desktop user interface and event loop |
| **On-Device Model** | Microsoft Phi-3 Mini (`phi3:mini` via Ollama) | `3.8B (Q4)` | Local instruction-tuned language model (~2.2 GB) |
| **LLM Client Wrapper** | LangChain4j (`langchain4j-ollama`) | `0.31.0` | Java HTTP wrapper for local Ollama API (`localhost:11434`) |
| **Rule Engine** | Pure Java (`java.util.regex`) | Built-in | Post-processing validation enforcing the 3-question constraint |
| **Local Database** | Xerial SQLite JDBC (`sqlite-jdbc`) | `3.45.2.0` | Serverless single-file database (`greensocratic.db`) with FTS5 |
| **Logging** | SLF4J Simple (`slf4j-simple`) | `2.0.12` | Runtime console logging |
| **Build Tool** | Apache Maven + `javafx-maven-plugin` | `0.0.8` | Dependency resolution and application execution |

---

## 🏗️ System Architecture

```text
+-----------------------------------------------------------------------+
|                     JavaFX Desktop UI (MainApp)                       |
|      [Chat Transcript]       [Status Indicator]      [Prompt Input]   |
+------------------+-------------------------------------+--------------+
                   | (Async CompletableFuture)           | (JDBC Calls)
                   v                                     v
+----------------------------------+      +-----------------------------+
|         LLMService.java          |      |       DBManager.java        |
|   (LangChain4j OllamaChatModel)  |      |  (SQLite JDBC + FTS5 Init)  |
+------------------+---------------+      +--------------+--------------+
                   |                                     |
                   v                                     v
+----------------------------------+      +-----------------------------+
|     SocraticRuleEngine.java      |      |      greensocratic.db       |
|   (Regex Extraction & Fallback)  |      | (Virtual Table: resources)  |
+------------------+---------------+      +-----------------------------+
                   |
                   v
+----------------------------------+
|   Local Ollama Server (:11434)   |
|       Model: phi3:mini           |
+----------------------------------+

```

---

## 📂 Project Structure

```text
green-socratic-desktop/
├── pom.xml                                      # Maven dependencies & JavaFX plugin config
├── README.md                                    # Project documentation
├── greensocratic.db                             # Auto-created SQLite database on first DBManager init
└── src/
    └── main/
        ├── java/com/greensocratic/
        │   ├── Launcher.java                    # Non-Application entry point (fixes Java 17 module check)
        │   ├── MainApp.java                     # JavaFX Application UI & async event handlers
        │   ├── LLMService.java                  # OllamaChatModel setup & CompletableFuture wrapper
        │   ├── SocraticRuleEngine.java          # Regex question extractor & Socratic fallback template
        │   ├── DBManager.java                   # SQLite JDBC connection & FTS5 virtual table query
        │   ├── ResourceRepository.java          # [Planned - Week 2] CSV batch importer for resources
        │   └── TimerService.java                # [Planned - Week 2] ScheduledExecutorService Pomodoro timer
        └── resources/
            └── resources.csv                    # [Planned - Week 2] Local study resource dataset

```

---

## 🚀 Prerequisites & Setup

### 1. System Requirements

* **JDK 17** or newer installed (verify with `java -version`).
* **Apache Maven 3.8+** (or VS Code with the *Extension Pack for Java* / IntelliJ IDEA).
* **Ollama** installed on the host machine ([https://ollama.com](https://ollama.com)).

### 2. Pull the Local Model

Before launching the Java app, open a standard terminal (Command Prompt, PowerShell, or Bash) and pull `phi3:mini`:

```bash
ollama run phi3:mini

```

* Wait for the ~2.2 GB download to complete.
* When the interactive `>>> Send a message` prompt appears, type `/bye` and press **Enter** to exit.
* *(Note: If running `ollama` by itself opens a CLI agent selection menu in newer Ollama releases, press `Ctrl + C` to exit and run the full command `ollama run phi3:mini` directly.)*

### 3. Run the Application

**Method A: Run via VS Code or IntelliJ IDEA (Recommended for Development)**

1. Open the root `green-socratic-desktop` folder in your IDE.
2. Open `src/main/java/com/greensocratic/Launcher.java`.
3. Click **Run** above `public static void main(String[] args)` (or press `F5` in VS Code).

**Method B: Run via Maven Command Line**
From the root directory containing `pom.xml`, run:

```bash
mvn clean javafx:run

```

---

## 🧠 How the Socratic Rule Engine Works

Small local language models can occasionally ignore system instructions and provide direct code solutions or conversational filler. `SocraticRuleEngine.java` guarantees compliance using a two-layer defense:

1. **Prompt Conditioning (`LLMService.java`):** Sets temperature to `0.3` and prepends a strict system instruction commanding the model to act as Socrates and output only 3 numbered questions.
2. **Deterministic Regex Verification (`SocraticRuleEngine.java`):** Evaluates the model's raw string output against the multiline pattern:
```java
Pattern.compile("(?m)^\\s*\\d+[.)]\\s*(.+\\?)")

```


* **Pass Condition:** If exactly 3 numbered questions ending with `?` are matched, the engine strips any surrounding prose/chatter and returns only the 3 clean questions.
* **Fallback Condition:** If the model outputs fewer or more than 3 questions (or provides direct answers), the engine discards the non-compliant output and returns 3 structured Socratic meta-questions focusing on *fundamental assumptions*, *edge cases/counterexamples*, and *empirical evidence*.



---

## 🔧 Troubleshooting

* **`[System Error]: Could not reach Ollama at localhost:11434`**
Ensure Ollama is running in your system tray/background and that `phi3:mini` has been downloaded via `ollama run phi3:mini`.
* **`Error: JavaFX runtime components are missing, and are required to run this application`**
Do not launch `MainApp.java` directly from the IDE Run button. In Java 17+, launching a class that extends `javafx.application.Application` directly without module-path arguments triggers a JVM check failure. Always launch **`Launcher.java`** instead, or use `mvn javafx:run`.
* **Initial Response Delay (5–15 seconds)**
On the first prompt of a session, Ollama loads the 2.2 GB `phi3:mini` weights from disk into RAM. Subsequent queries in the same session execute faster.

---

## 📜 License & Copyright

**Copyright © 2026 [Abigail Rita Rayan, Bhavadharani M.S, Dhivyashree S.N, Nifana Parveen N, Shruthika K.K]. All Rights Reserved.**

This software, source code, architecture, and associated documentation files (the "Software") are the proprietary and confidential property of the author.

1. **No Copying or Redistribution:** Unauthorized copying, modification,forking, distribution, public display, or commercial use of this Software, via any medium, is strictly prohibited without the express written permission of the copyright holder.
2. **Academic Evaluation Exception:** A limited, non-exclusive, non-transferable license is granted solely to the assigned course instructor(s) and teaching assistants for the purpose of compiling, executing, and evaluating this Software for academic grading.

```
