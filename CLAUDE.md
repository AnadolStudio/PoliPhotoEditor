# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PoliPhotoEditor is an Android photo editing application (min SDK 23, target SDK 33) with GPU-accelerated image processing. Application ID: `art.intel.soft`.

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Develop variant build
./gradlew assembleDevelop

# Run unit tests
./gradlew test

# Run a single test class
./gradlew :app:test --tests "art.intel.soft.ResizeTest"

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Clean
./gradlew clean
```

## Module Structure

This is a multi-module Gradle project:

- **`:app`** — Main application
- **`:photoeditor`** — Core photo editing library (v1.7.21)
- **`:androidGpuImagePlusMapper`** — GPU image processing wrapper
- **`:curveslibrary`** — Curves adjustment tool (v1.0.3)
- **`:colorseekbar`** — Color picker seekbar (v1.0.2)

## Architecture

**Pattern:** MVVM with Fragments and Activities, RxJava2 for reactive streams.

**Activity flow:**
`StartActivity` (splash/ads) → `MainActivity` (menu) → `GalleryListActivity` (photo picker) → `EditActivity` (editor) → `SaveActivity` (export)

**App source root:** `app/src/main/java/art/intel/soft/`

| Package | Purpose |
|---------|---------|
| `base/` | Base Activity/ViewModel classes, Firebase integration |
| `ui/edit/` | Editing features: background, body, collage, effects, filters, text, stickers, brush, frames, crop, improve |
| `ui/gallery/` | Gallery selection |
| `ui/main/` | Main menu |
| `ui/start/` | Splash screen |
| `model/` | Data models (AssetData, CollageService, GalleryService) |
| `utils/` | Bitmap utilities, image loading, permissions, animations |
| `view/` | Custom views (toolbars, buttons) |
| `extention/` | Kotlin extension functions |

## Build Variants

- `debug` — Debuggable, uses `debug.keystore`
- `develop` — App ID suffix `.develop`, for development testing
- `release` — Minified with ProGuard (`proguard-rules.pro`), uses production keystore (`keystore_photo_editor.jks`)

## Key Technologies

- **GPU Processing:** `android-gpuimage-plus` for real-time filter effects
- **ML:** ML Kit Selfie Segmentation (background removal)
- **Image Loading:** Glide 4.13.2
- **Ads/Analytics:** Firebase Analytics, Crashlytics, Cloud Messaging
- **Preferences:** BinaryPrefs (lightweight key-value storage)
- **NDK:** Native libs for armeabi-v7a, x86, arm64-v8a, x86_64

## Tests

Unit tests are in `app/src/test/art/intel/soft/`:
- `FirebaseTest.kt` — Validates Firebase analytics event name formatting
- `ResizeTest.kt` — Tests bitmap resize ratio calculations

Test setup uses Robolectric (unit tests with Android resources) and Espresso (UI tests).

## Notes

- View binding is enabled; avoid `findViewById` in new code.
- RenderScript is enabled for image processing operations.
- Assets (filters, stickers, frames) are stored in Android assets folders, not res/.
- ProGuard rules in `app/proguard-rules.pro` must be updated when adding new reflection-heavy dependencies.
