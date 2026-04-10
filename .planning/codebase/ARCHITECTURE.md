# Architecture Analysis - MAD Lab (Recipe Catalog App)

## Project Overview

**Recipe Catalog** is an Android mobile application that combines a static recipe database with dynamic AI-powered recipe generation using Google's Gemini API.

**App Identifier:** `com.example.myapplication`
**Target SDK:** 34 (Android 14)
**Min SDK:** 24 (Android 7.0)
**Build System:** Gradle with Kotlin DSL (KTS)

---

## Component Architecture

### Layered Structure

The application follows a simple **MVC-like (Model-View-Controller)** pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│        Presentation Layer (Activities)       │
│  ┌──────────────────────────────────────┐   │
│  │ MainActivity   │ RecipeDetailActivity│   │
│  │               │  AiRecipeActivity   │   │
│  └──────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│      Adapter/View Layer (UI Components)     │
│  ┌──────────────────────────────────────┐   │
│  │ RecipeAdapter (RecyclerView)         │   │
│  │ Layout Resources & View Bindings     │   │
│  └──────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│      Business Logic / Service Layer         │
│  ┌──────────────────────────────────────┐   │
│  │ GeminiApiService (API Integration)   │   │
│  │ RecipeDatabase (Data Management)     │   │
│  └──────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│        Data Model Layer (POJO)              │
│  ┌──────────────────────────────────────┐   │
│  │ Recipe (Serializable Entity)         │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

---

## Core Components

### 1. **Activity Layer**

#### MainActivity
- **Purpose:** Primary entry point; displays recipe list with search/filter functionality
- **Responsibilities:**
  - Load recipes from `RecipeDatabase`
  - Manage RecyclerView with recipe list
  - Handle search input with real-time filtering
  - Navigate to recipe details
  - Navigate to AI recipe generator
  - Refresh data on resume
- **Key Features:**
  - TextWatcher for live search across name, category, difficulty
  - Empty state handling
  - FloatingActionButton (FAB) for AI recipe generation
  - Toolbar with title

#### RecipeDetailActivity
- **Purpose:** Display full recipe details
- **Responsibilities:**
  - Receive recipe via Intent (Serializable)
  - Display all recipe metadata (ingredients, instructions, cooking time, servings)
  - Format and render recipe content
  - Support back navigation via up button
- **UI Elements:**
  - Emoji display for visual identification
  - Color-coded difficulty badges (Green=Easy, Orange=Medium, Red=Hard)
  - Formatted ingredient and instruction lists

#### AiRecipeActivity
- **Purpose:** AI-powered recipe generation interface
- **Responsibilities:**
  - Accept user dish name input
  - Trigger `GeminiApiService` API calls
  - Show loading state during generation
  - Display formatted recipe results
  - Persist generated recipes to database
  - Provide error handling and user feedback
- **API Integration:**
  - Calls Google Gemini API with detailed JSON schema prompt
  - Handles API responses and parsing
  - Implements retry/error messaging

---

### 2. **Adapter Layer**

#### RecipeAdapter
- **Type:** RecyclerView.Adapter<RecipeViewHolder>
- **Responsibilities:**
  - Bind Recipe data to CardView items
  - Handle item click events
  - Apply conditional difficulty color coding
  - Support dynamic list updates
- **Features:**
  - OnRecipeClickListener callback interface
  - updateRecipes() for live list filtering
  - notifyDataSetChanged() for UI refresh

---

### 3. **Service Layer**

#### GeminiApiService
- **Purpose:** Encapsulate Google Gemini API communication
- **Key Features:**
  - Async API calls using ExecutorService (single-threaded)
  - Background thread execution with MainHandler callback posting
  - Request/Response classes for JSON serialization (Gson)
  - Robust error handling and response parsing
- **API Details:**
  - Endpoint: `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent`
  - Model: `gemini-2.0-flash-exp`
  - Authentication: Query parameter `?key={apiKey}`
  - Request format: Structured JSON with content/parts
  - Response parsing: Extracts text from nested candidate structure
- **Prompt Engineering:**
  - Explicit JSON schema requirement
  - Markdown code block cleanup
  - Fallback recipe creation on parse failure
- **Threading Model:**
  - Single-threaded ExecutorService for sequential API calls
  - Handler(Looper.getMainLooper()) for UI thread callbacks

#### RecipeDatabase
- **Purpose:** Static recipe data management
- **Storage:** In-memory ArrayList (non-persistent)
- **Features:**
  - Pre-loaded with 12 hardcoded recipes covering various cuisines
  - Dynamic AI-generated recipes stored in separate list
  - getRecipes() merges static + AI recipes
  - addAiRecipe(Recipe) for persistence during session
  - clearAiRecipes() for cleanup
- **Data Characteristics:**
  - Recipes include: ID, name, category, difficulty, cookingTime, servings, ingredients, instructions, emoji
  - No database file or SharedPreferences persistence
  - Data lost on app close

---

### 4. **Model Layer**

#### Recipe (POJO)
- **Type:** Serializable entity for Intent passing
- **Fields:**
  - `id: int` - Unique identifier
  - `name: String` - Recipe name
  - `category: String` - Cuisine category
  - `difficulty: String` - Difficulty level (Easy/Medium/Hard)
  - `cookingTime: int` - Minutes to cook
  - `servings: int` - Number of servings
  - `ingredients: ArrayList<String>` - Ingredient list
  - `instructions: ArrayList<String>` - Step-by-step instructions
  - `imageEmoji: String` - Visual emoji representation
- **Serialization:** Implements Serializable for Intent Bundle passing

---

## Data Flow

### Recipe Discovery Flow
```
MainActivity
    ↓
RecipeDatabase.getRecipes()
    ↓
ArrayList<Recipe> (static + AI)
    ↓
RecipeAdapter (binds to RecyclerView)
    ↓
User clicks item
    ↓
Intent → RecipeDetailActivity
```

### AI Recipe Generation Flow
```
AiRecipeActivity (user input: dish name)
    ↓
GeminiApiService.generateRecipe()
    ↓
ExecutorService (background thread)
    ↓
HTTP POST to Gemini API
    ↓
Response parsing + JSON deserialization (Gson)
    ↓
Recipe object creation
    ↓
MainHandler.post() → UI callback
    ↓
RecipeDatabase.addAiRecipe()
    ↓
Toast notification + MainActivity refresh
```

### Search & Filter Flow
```
MainActivity.searchEditText (TextWatcher)
    ↓
filterRecipes(query)
    ↓
Query against Recipe.name, category, difficulty
    ↓
filteredRecipes ArrayList (cleared & repopulated)
    ↓
RecipeAdapter.updateRecipes()
    ↓
notifyDataSetChanged()
    ↓
RecyclerView UI refresh
```

---

## Component Interactions

### Intent-based Navigation
- **MainActivity → RecipeDetailActivity:** Passes Recipe object via Bundle
- **MainActivity → AiRecipeActivity:** No data passing; activity manages own state
- **Back Navigation:** Uses up button (setSupportActionBar with DisplayHomeAsUpEnabled)

### Listener Patterns
- **RecipeAdapter.OnRecipeClickListener:** MainActivity implements to handle recipe selection
- **GeminiApiService.RecipeCallback:** AiRecipeActivity implements for async API responses

### Data Sharing
- **Static State:** RecipeDatabase holds recipe list (in-memory)
- **Session Data:** AI-generated recipes persisted in RecipeDatabase during session
- **Transient Data:** Search filters, UI state held in Activity

---

## UI Structure

### Screens & Layouts

| Screen | Layout File | Purpose |
|--------|-------------|---------|
| Recipe List | `activity_main.xml` | Main screen with search, toolbar, RecyclerView |
| Recipe Item | `item_recipe.xml` | CardView item showing recipe preview |
| Recipe Details | `activity_recipe_detail.xml` | Full recipe display with ingredients & instructions |
| AI Generator | `activity_ai_recipe.xml` | Input field, generate button, result display |

### Resource Structure
- **res/layout/** - Activity and item layouts
- **res/drawable/** - App icon assets
- **res/mipmap-*/** - Launcher icons (multiple densities)
- **res/values/** - String resources and theme definitions
- **res/values-night/** - Dark theme (if applicable)
- **res/xml/** - Backup and data extraction rules

---

## Dependency Injection & Configuration

**Current State:** No DI framework (Dagger/Hilt)
- Components instantiated directly (tight coupling)
- GeminiApiService created in AiRecipeActivity
- RecipeDatabase accessed statically

**Configuration:**
- API key hardcoded in AiRecipeActivity.GEMINI_API_KEY (⚠️ security concern)
- Build configuration in Gradle (compileSdk=34, minSdk=24)

---

## Key Design Patterns

1. **MVC Pattern:** Separation between View (Activities/Layouts), Controller (Activities), Model (Recipe/RecipeDatabase)
2. **Adapter Pattern:** RecyclerView.Adapter for list rendering
3. **Observer Pattern:** TextWatcher for search input monitoring
4. **Callback Pattern:** GeminiApiService.RecipeCallback for async API responses
5. **Facade Pattern:** GeminiApiService abstracts API complexity
6. **Static Factory:** RecipeDatabase provides static methods for recipe access

---

## Architectural Concerns & Limitations

### 1. **No Persistence Layer**
- Recipe data lost on app close
- No database (Room, SQLite, or Realm)
- No SharedPreferences for settings
- AI-generated recipes temporary

### 2. **Tight Coupling**
- Activities directly instantiate services (no DI)
- RecipeDatabase accessed via static methods
- No abstraction layers for testability

### 3. **Security Issues**
- API key hardcoded in source code (should use BuildConfig or secure storage)
- No API key obfuscation or ProGuard rules applied

### 4. **Threading Model**
- Single-threaded ExecutorService (could be bottleneck for multiple API calls)
- No proper lifecycle-aware coroutine support (uses legacy Handler)

### 5. **Memory Management**
- Serializable Recipe objects passed via Intent (could be large for complex recipes)
- No pagination or lazy loading for recipe lists
- RecyclerView.notifyDataSetChanged() (inefficient for large lists)

### 6. **Error Handling**
- Generic error messages in GeminiApiService
- No retry logic for API failures
- No validation of user input beyond emptiness check

### 7. **Scalability Issues**
- All recipes loaded into memory at startup
- No caching strategy
- No search indexing (linear O(n) search)

---

## Summary

The Recipe Catalog app follows a simple, understandable architecture suitable for a learning project. It demonstrates:
- ✅ Clear activity-based navigation
- ✅ RecyclerView implementation
- ✅ Async API integration with callbacks
- ✅ Real-time search filtering
- ✅ Proper Toolbar usage

However, production readiness would require:
- Persistence layer (database)
- Dependency injection
- Security hardening (API key management)
- Modern async/await (Coroutines)
- Comprehensive error handling and testing
