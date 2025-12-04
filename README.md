# Stdout

A modern Hacker News client with a terminal-inspired interface, built with Kotlin Multiplatform to demonstrate clean architecture and modern Android development practices.

## Features

- Browse HN stories (Top, New, Best, Ask, Show, Job)
- Nested comment threads with pagination
- Pull-to-refresh and infinite scroll
- Terminal-inspired UI design
- Offline-first architecture
- Dark mode support

## Tech Stack

**Kotlin Multiplatform** - Share logic across platforms

**Compose Multiplatform** - UI Framework

**Circuit** - Navigation/Presentation layer

**Store5** - Cache management

**Ktor** - Networking

**Room** - Local database

**KStore** - Disk storage

**Kotlin Inject Anvil** - Dependency Injection

**Kotlinx Serialization** - JSON serialization

**Turbine** - Testing flows

## Architecture

**Multi-modular** - Organized by layer and feature for scalability, parallel builds, and clear boundaries:
- `core/*` - Shared business logic, data layer, design system
- `features/*` - Feature modules with Circuit presenters/UI
- `app/android` - Android application entry point
- `shared` - Multiplatform shared code

**Convention Plugins** - Gradle build logic centralized in `build-logic/` to enforce consistency across modules and reduce build file duplication.

**Offline-first** - Store5 manages network/cache strategies, Room provides local persistence, ensuring the app works seamlessly without connectivity.

Follows unidirectional data flow with Circuit's Presenter pattern, repository pattern for data management, and reactive streams with Kotlin Flow.

## Getting Started

**Prerequisites**
- JDK 17+
- Android Studio Ladybug or later

**Build**
```bash
./gradlew assembleDebug
```

**Run**
Open in Android Studio and run the `app:android` configuration.

## License

MIT
