# My File Explorer 📁🔒

A modern, fast, and secure Android File Manager built with **Jetpack Compose**, **Material Design 3**, **Room Database**, and **AES-256-GCM** encryption.

---

## ✨ Features

- 📂 **Full File & Folder Management**: Create folders, text files, rename, delete, restore from Recycle Bin, and calculate SHA-256 checksums.
- 🗜️ **ZIP Archive Support**: Create/compress single or multiple files into standard ZIP archives, and extract ZIP files directly into organized folders.
- 🏷️ **Color Highlight Tags**: Pin and color-code files (**Important/Yellow**, **Urgent/Red**, **Verified/Green**, **Work/Blue**, **Personal/Purple**) with quick filter chips.
- 🔐 **Password-Protected Safe Vault**: Secure sensitive documents behind a Master PIN with AES-256-GCM encryption.
- 📄 **Integrated Document Scanner & OCR**: Scan documents, apply visual filters (B&W Clean, High Contrast, Grayscale), run automated text extraction, and get auto-assigned smart tags.
- ☁️ **Cloud Storage Sync**: Multi-cloud integration (Google Drive, Nextcloud, WebDAV, AWS S3) and cross-platform device pairing keys.
- 💾 **Local Encrypted Backups**: PBKDF2 passphrase-derived encrypted local backups with integrity validation and restore functionality.
- ✈️ **Offline Travel Mode**: Queue network operations for uninterrupted offline use.
- 🌓 **Minimalist Dark & Light Modes**: Seamless theme switching with high-contrast accessibility.

---

## 🚀 How to Build & Download the APK

### Option 1: Automatic Build via GitHub Actions (Recommended)
1. Push this repository to your GitHub account.
2. Go to the **Actions** tab in your GitHub repository.
3. The **Build Android APK** workflow will run automatically on push (or you can trigger it manually via **Run workflow**).
4. Once completed, download the **`MyFileExplorer-Debug-APK`** artifact directly from the workflow summary page.

### Option 2: Build Locally with Command Line
Prerequisites: JDK 17+ installed.

```bash
# Clone the repository
git clone <your-repo-url>
cd <repo-folder>

# Grant execute permission (Linux / macOS)
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Android Studio
1. Open Android Studio.
2. Select **File -> Open** and select this project directory.
3. Let Gradle sync dependencies.
4. Click **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**, or press **Run** (Green arrow) to install directly on a connected device or emulator.

---

## 🛠️ Tech Stack & Requirements
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material 3)
- **Local Database**: Room 2.7
- **Security**: AES-256-GCM, PBKDF2 (Android KeyStore / Crypto API)
- **Image Loading**: Coil 3
- **Build System**: Gradle 8.13+ (Kotlin DSL), AGP 8.9+
- **Min SDK**: 24 (Android 7.0+)
- **Target SDK**: 36 (Android 15+)
