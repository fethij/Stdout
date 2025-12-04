# Stdout

A Kotlin Multiplatform application for reading Hacker News stories and comments.

## Module Graph

```mermaid
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

    %% Styling
    classDef appStyle fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    classDef sharedStyle fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef featureStyle fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    classDef coreStyle fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px

    class app appStyle
    class shared sharedStyle
    class feed,details featureStyle
    class data,network,database,designsystem,navigation,common,model coreStyle
```

## Module Overview

### App Layer
- **app:android** - Android application entry point

### Shared Layer
- **shared** - Shared Kotlin Multiplatform module that aggregates all features and core modules

### Feature Layer
- **features:feed** - Feed screen displaying Hacker News stories with paging support
- **features:details** - Details screen for viewing story details and comments

### Core Layer
- **core:data** - Data repository layer with offline-first support using Store5
- **core:network** - Network layer using Ktor for API communication
- **core:database** - Local persistence using Room database
- **core:designsystem** - Compose UI design system and reusable components
- **core:navigation** - Navigation utilities using Circuit
- **core:common** - Common utilities and dependency injection setup
- **core:model** - Data models shared across modules

## Architecture

This project follows a multi-module architecture pattern:

1. **Separation of Concerns** - Each module has a single, well-defined purpose
2. **Dependency Rule** - Dependencies flow downward (app → shared → features → core)
3. **Kotlin Multiplatform** - Core modules and features are shared between Android and iOS
4. **Compose Multiplatform** - UI is built using Compose Multiplatform for code sharing
