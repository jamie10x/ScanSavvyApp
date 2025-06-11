# ScanSavvy AI

<p align="center">
  <img src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMXZ2bm5kNWQwdHZweDR4NWY0b3N6aHlwazBpdHpxamhuenUyMDQ3NyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/LKVyY24iI8Gg1C4t7p/giphy.gif" width="300" alt="Scanning Demo"/>
</p>

<p align="center">
  <strong>An intelligent, offline-first document scanner built with modern Android best practices.</strong>
</p>

---

## 📖 About This Project

ScanSavvy AI is a complete modernization of the legacy open-source application "OpenNoteScanner". This project showcases a ground-up refactoring using a 100% Kotlin, Jetpack Compose, and modern MVVM architecture. The complex OpenCV pipeline was replaced with Google's on-device **ML Kit** for a smarter and more reliable scanning experience.

## ✨ Features

*   **Effortless Scanning:** Uses Google's ML Kit for a polished UI with auto-crop and enhancement.
*   **On-Device OCR:** Extracts text from documents to make them searchable.
*   **Smart Titling:** Automatically names new scans based on their content.
*   **Full-Text Search:** Instantly find documents by searching for words inside them.
*   **Secure & Private:** 100% offline with optional biometric app lock.
*   **PDF Export:** Share documents as searchable, multi-page PDFs.
*   **Modern UI:** Built entirely with Jetpack Compose, including Dark Mode and Material You theming.

## 🛠️ Tech Stack

| Category | Technology |
| :--- | :--- |
| **UI** | ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white) |
| **Architecture** | MVVM, Repository, Hilt |
| **Asynchronicity** | ![Kotlin Coroutines](https://img.shields.io/badge/Coroutines-00949D?style=for-the-badge&logo=kotlin&logoColor=white) |
| **Database** | ![Room](https://img.shields.io/badge/Room-7484B8?style=for-the-badge&logo=android&logoColor=white) + FTS5 |
| **DI** | ![Hilt](https://img.shields.io/badge/Hilt-007396?style=for-the-badge&logo=dagger&logoColor=white) |
| **Intelligence** | ![ML Kit](https://img.shields.io/badge/ML%20Kit-FFA000?style=for-the-badge&logo=google&logoColor=white) |
| **Settings** | ![DataStore](https://img.shields.io/badge/DataStore-592B88?style=for-the-badge&logo=android&logoColor=white) |

## 🚀 How to Build

1.  Clone this repository.
2.  Open the project in the latest version of Android Studio.
3.  Build and run.

---