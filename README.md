# 📝 QuickNotes AI

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android_14+-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![AI: Gemini API](https://img.shields.io/badge/AI-Google_Gemini-8E75B2.svg?logo=google&logoColor=white)](https://ai.google.dev)

**QuickNotes AI** is a native Android note-taking application designed for zero-latency capture, intelligent organization, and multimodal interaction. It pairs custom multi-touch gesture shortcuts, pressure-sensitive stylus sketching, and voice dictation with an integrated **Google Gemini AI** cognitive assistant.

---

## ✨ Features

- 🖐️ **Instant Capture Gestures**: Custom multi-touch gesture engine (`TwoFingerTapDetector`) supporting rapid 3-tap 2-finger shortcuts to launch instant notes from anywhere.
- ✍️ **Smart Stylus Canvas**: Low-latency freehand handwriting and drawing surface (`StylusCanvas`) supporting pressure sensitivity, stroke smoothing, and stroke models.
- 🎙️ **Hands-free Voice Dictation**: Integrated speech-to-text engine (`VoiceSpeechManager`) for real-time thought capture.
- 🤖 **AI Assistant (Gemini)**:
  - Automated note summarization.
  - Action-item and todo extraction.
  - Semantic tagging and categorizing.
- 🔔 **Android System Integration**: Custom Quick Settings Tile (`QuickNoteTileService`) and lockscreen simulator for frictionless access.
- 🏛️ **Clean MVVM Architecture**: Built with Jetpack Compose declarative UI, Coroutines/Flow state management, and offline-first Room SQLite ORM.

---

## 🏗️ Architecture Overview

```mermaid
graph TD
    UI[Jetpack Compose UI (Screens & Canvas)] --> VM[MainViewModel]
    VM --> Repo[NoteRepository]
    Repo --> Room[(Room Database / SQLite)]
    VM --> Gemini[Gemini AI Service]
    VM --> Speech[VoiceSpeechManager]
    Input[Touch / Stylus / Gestures] --> UI
```

---

## 🛠️ Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Language** | Kotlin (Coroutines, Flow, StateFlow) |
| **UI Framework** | Jetpack Compose, Material Design 3 |
| **Persistence** | Room DB, SQLite, DataStore |
| **AI Integration** | Google Gemini API (Server-side & Client integration) |
| **Hardware APIs** | Android Stylus APIs, SpeechRecognizer, Quick Settings Tile Service |
| **Testing** | JUnit 4, Robolectric, Compose Screenshot Tests |

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/NezerkC/quicknote-ai.git
cd quicknote-ai
```

### 2. Configure Environment

Copy `.env.example` and supply your Gemini API credentials if running the AI assistant backend:

```bash
cp .env.example .env
```

### 3. Build with Gradle

Open the project in **Android Studio** (Ladybug or newer) or compile via command line:

```bash
# Debug build
./gradlew assembleDebug

# Run unit and screenshot tests
./gradlew testDebugUnitTest
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
