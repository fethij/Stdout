# stdout

A modern Hacker News client with a terminal-inspired interface.

<p style="text-align: center;">
  <img src="assets/feed.png" width="250"/>
  <img src="assets/detail.png" width="250"/>
</p>

## Tech Stack

**Kotlin Multiplatform** - Share logic across platforms

**Compose Multiplatform** - UI Framework

**Circuit** - Navigation/Presentation layer

**Store** - Cache management

**Ktor** - Networking

**Room** - Local database

**Kotlin Inject Anvil** - Dependency Injection

**Kotlinx Serialization** - JSON serialization

## Architecture

**Multi-modular**
- `core/*` - Shared business logic, data layer, design system
- `features/*` - Features like feed or detail
- `app/*` - Application entry point to android/ios apps

**Convention Plugins** - Gradle build logic centralized in `build-logic/` to enforce consistency across modules and reduce build file duplication.

**Offline-first** - Store manages network/cache strategies, Room provides local persistence, ensuring the app works seamlessly without connectivity.

Follows unidirectional data flow with Circuit's Presenter pattern, repository pattern for data management, and reactive streams with Kotlin Flow.

## Module Graph

```mermaid
%%{
  init: {
    'theme': 'neutral'
  }
}%%
graph TD
    %% App Layer
    app[app:android]
    %% Shared Layer
    shared[shared]
    %% Feature Layer
    feed[features:feed]
    details[features:details]
    %% Core Layer
    data[core:data]
    network[core:network]
    database[core:database]
    designsystem[core:designsystem]
    navigation[core:navigation]
    common[core:common]
    model[core:model]
    %% App dependencies
    app --> shared
    %% Shared dependencies
    shared --> feed
    shared --> details
    shared --> data
    shared --> network
    shared --> database
    shared --> designsystem
    shared --> navigation
    shared --> common
    shared --> model
    %% Feature dependencies
    feed --> common
    feed --> model
    feed --> navigation
    feed --> designsystem
    feed --> data
    details --> common
    details --> model
    details --> navigation
    details --> designsystem
    details --> data
    %% Core dependencies
    data --> model
    data --> network
    data --> database
    network --> model
    database --> model
    database --> common
    designsystem --> model
```
