# 📱 Campus Lost & Found — Feature Documentation

> A modern, production-grade Android app to help campus communities report, discover, and reclaim lost items.

---

## 🗂️ Table of Contents

1. [Core Features](#-core-features)
2. [UI & Design System](#-ui--design-system)
3. [User Management](#-user-management)
4. [Item Management](#-item-management)
5. [Search & Filtering](#-search--filtering)
6. [Sharing & Notifications](#-sharing--notifications)
7. [Dark Mode](#-dark-mode)
8. [Database Architecture](#-database-architecture)
9. [What Differentiates This App](#-what-differentiates-this-app)

---

## ✅ Core Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | **Splash Screen** | Branded launch screen with animated gradient, logo, and tagline. Automatically routes to Setup or Home. |
| 2 | **User Onboarding** | First-launch setup captures name and contact info — no accounts or internet required. |
| 3 | **Post Lost/Found Items** | Users can report lost or found items with a name, description, location, category, type, photo, and event date. |
| 4 | **Edit Items** | Any post can be fully edited (name, description, location, type, category, image, date) without deleting and re-posting. |
| 5 | **Mark as Returned** | Once an item is reunited with its owner, it can be marked "Returned" with a green ✓ banner overlay. |
| 6 | **Delete Items** | Posts can be permanently deleted via a confirmation dialog — with swipe support planned. |
| 7 | **Dark Mode** | Full system-level dark mode with semantic color tokens that adapt every screen automatically. |
| 8 | **Share via WhatsApp** | Item details and photos can be shared directly to WhatsApp (or any app via system chooser). |
| 9 | **Push Notifications** | A local notification is fired every time a new item is posted, keeping the campus community informed. |
| 10 | **Category Filtering** | Items can be filtered by 7 categories with a horizontal scrollable chip row on the home screen. |

---

## 🎨 UI & Design System

The app is built using **Material Design 3** components with a rich, custom design system — far beyond the default Android look.

### Color Palette
- **Primary:** Deep Blue (`#1565C0`) — trust, clarity
- **Accent / Found:** Teal Green — success, positivity
- **Lost:** Warm Red — urgency, alert
- **Semantic tokens:** `colorSurface`, `colorToolbarBg`, `textPrimary`, `textSecondary`, `textHint` — all adapt automatically for light and dark modes

### Typography
- `sans-serif-medium` for headings and item names
- `sans-serif` for body and secondary text
- Letter spacing tuned for readability across card and detail views

### Components Used
| Component | Usage |
|-----------|-------|
| `MaterialToolbar` | All screens — with navigation icon and subtitle |
| `ExtendedFloatingActionButton` | Home screen — shrinks on scroll, expands on scroll up |
| `MaterialCardView` | Each item in the list; info, poster, and action cards in detail |
| `ChipGroup` | Lost/Found type filter + category filter row |
| `TextInputLayout (Outlined)` | All form fields — with hint animation |
| `ExposedDropdownMenu` | Type and Category dropdowns |
| `MaterialDatePicker` | Event date picker — Material calendar dialog |
| `MaterialButton` (filled + outlined) | All CTAs, action buttons |
| `NestedScrollView` | Detail screen scroll container |

### Micro-animations & Polish
- FAB **shrinks** when scrolling down, **expands** on scroll up
- Resolved items show a **green overlay banner** with ✓ RETURNED text
- Lost card = red left strip + warm card tint; Found card = green left strip + cool card tint
- Splash screen shows a **semi-transparent circle** around the logo for a glowing effect
- Relative timestamps ("Just now", "2h ago", "Apr 8") instead of raw dates on cards

---

## 👤 User Management

### First-Launch Onboarding (`UserSetupActivity`)
- Captures **Name** and **Contact (phone/email)**
- Stored in `SharedPreferences` — persists across sessions
- Setup guard: `MainActivity` and `SplashActivity` check if setup is done; redirect if not
- **Re-sign in supported** via `ProfileActivity` → user can update name & contact anytime

### Profile Screen (`ProfileActivity`)
- Displays and edits user name and contact info
- Accessible from the **toolbar menu** on the home screen
- Changes reflect immediately as the toolbar subtitle ("Hi, John 👋")

---

## 📦 Item Management

### Adding Items (`AddItemActivity`)
Every new post captures:

| Field | Input Type |
|-------|-----------|
| Item Name *(required)* | Text input |
| Description | Multi-line text |
| Location | Text (e.g. "Library 2nd Floor") |
| **Date Lost / Found** | Material Date Picker calendar |
| Type | Dropdown: `Lost` / `Found` |
| Category | Dropdown: 7 options |
| Photo | Photo Picker (Android 13+ `PickVisualMedia`) |

- Images are **copied to internal storage** (safe against URI revocation)
- Poster name and contact are **auto-attached** from the user profile

### Editing Items (Dual-mode `AddItemActivity`)
- Launched from the **Detail screen** with `EXTRA_EDIT_ID`
- All fields pre-filled — including the existing photo preview and saved date
- Toolbar title changes to **"Edit Item"**, Save button changes to **"Save Changes"**
- Image is only overwritten if the user explicitly picks a new one
- Detail screen **auto-refreshes** after saving via `ActivityResultLauncher`

### Detail Screen (`DetailActivity`)
Displays full item info in structured cards:
- **Hero image** (full-width, with type badge + item name overlaid)
- **Info card:** Description, Location → Category → **Event Date** (hidden if not set) → Posted timestamp
- **Poster card:** Poster name + contact info
- **Action buttons:** Share, Edit, Mark Returned, Delete

---

## 🔍 Search & Filtering

The home screen offers a **3-layer filtering system** — all working simultaneously:

### 1. Live Search
- Searches across **Name, Description, and Location** as the user types
- No "Search" button needed — results update instantly via `TextWatcher`

### 2. Type Filter (Lost / Found)
- `All` | `Lost` | `Found` chip group
- Single-selection, always active

### 3. Category Filter
- Horizontal scrollable chip row with **8 chips**: All, Electronics, Keys, Wallet/ID, Books, Clothing, Accessories, Other
- Chips are created **dynamically** from the `CATEGORY_LABELS` array
- Works in combination with Search + Type filter

### Item Count Label
- Shows live count: `"3 items · Electronics"` — updates with every filter change

---

## 🔔 Sharing & Notifications

### Share via WhatsApp
- Tapping **"📤 Share via WhatsApp"** on the Detail screen builds a rich message:
  ```
  📢 Lost & Found — Lost
  📦 Item: MacBook Charger
  📍 Location: Library
  📝 Description: Black 65W charger, left near window seat
  👤 Posted by: Hiten Borkar
  📞 Contact: 9876543210
  ```
- If a photo exists, it's shared as an **image attachment** via `FileProvider`
- Falls back to system share chooser if WhatsApp is not installed

### Local Notifications (`NotificationHelper`)
- A notification channel `"new_items"` is created on first launch
- A push notification fires whenever a new item is posted
- Handles Android 13+ `POST_NOTIFICATIONS` runtime permission

---

## 🌙 Dark Mode

- Toggled via the **toolbar menu** (moon icon) on the home screen
- Preference stored in `SharedPreferences` and applied at app startup via `MyApplication`
- Uses **semantic color tokens** defined in `values/colors.xml` + `values-night/colors.xml`:

| Token | Light | Dark |
|-------|-------|------|
| `colorToolbarBg` | Deep Blue | Dark Navy |
| `colorSurface` | White | Dark Charcoal |
| `textPrimary` | Near-black | Near-white |
| `textSecondary` | Medium grey | Light grey |
| `textHint` | Light grey | Dim grey |
| `colorBackgroundSecondary` | Off-white | Dark grey |

All layouts use **only semantic tokens** — no hardcoded colors anywhere.

---

## 🗃️ Database Architecture

### SQLite via `DatabaseHelper` (v4)

| Version | Migration |
|---------|-----------|
| v1 | Initial schema: `_id, name, description, location, type, image_path` |
| v2 | Added `poster_name, poster_contact, status, created_at` |
| v3 | Added `category TEXT DEFAULT 'Other'` |
| v4 | Added `event_date INTEGER DEFAULT 0` |

All migrations are **non-destructive** — existing user data is never lost on upgrade.

### Key Methods

| Method | Purpose |
|--------|---------|
| `insertItem(Item)` | Insert new item with all 12 fields |
| `getItemById(int)` | Fetch a single item for detail/edit |
| `getAllItems()` | Fetch all items (newest first) |
| `searchAndFilterItems(query, type, category)` | Combined 3-param search + filter |
| `updateItem(id, ...)` | Full edit — only overwrites image if non-null |
| `updateItemStatus(id, status)` | Mark as `active` or `resolved` |
| `deleteItem(id)` | Permanent delete by ID |

### Item Model Fields

```java
int    id
String name, desc, location, type, imagePath
String posterName, posterContact, status, category
long   createdAt   // when the post was made
long   eventDate   // when the item was actually lost or found
```

---

## 🏆 What Differentiates This App

Most campus lost & found apps are simple notice boards. Here's what makes this one stand out:

### 1. 📅 "When It Actually Happened" Date
Unlike apps that only show when a post was made, this app captures the **actual date the item was lost or found** — helping users match reports more accurately (e.g., "I lost it 3 days ago").

### 2. 🏷️ Smart 3-Layer Filtering
Search + Type + Category all work **simultaneously** with a live item count. Most apps have at most a basic search. Our chip-based category row is fast, intuitive, and scrollable.

### 3. ✏️ Full Edit Support
Most notice-board apps require delete + re-post to fix a mistake. This app allows **in-place editing** of every field, with the existing image pre-loaded so you don't have to re-pick it.

### 4. 📤 WhatsApp-First Sharing
Designed for campus life, where WhatsApp groups are the primary communication channel. Share includes formatted text **and** the item photo — not just a link.

### 5. 🌙 True Dark Mode (No Hardcoded Colors)
The dark mode isn't just a black background slapped on. Every color in every layout is a **semantic token** that resolves correctly for both themes, including the toolbar, card backgrounds, chips, and surfaces.

### 6. 🔒 Secure Image Handling
Photos are copied to **internal app storage** on pick — preventing broken images when the user clears their gallery or revokes permissions. Shared via `FileProvider` for security compliance.

### 7. 🎨 Premium UI Out of the Box
- Gradient splash screen with glowing logo circle
- FAB that shrinks/extends on scroll
- Colored left-strip + card tint per item type (red for Lost, green for Found)
- Resolved items show a green overlay banner
- Relative timestamps on cards ("Just now", "3h ago", "Apr 7")

### 8. 📲 Zero Backend Required
No Firebase, no server, no accounts, no internet. The app works **100% offline**, making it reliable in campus environments with poor connectivity.

---

## 📁 Project Structure

```
app/src/main/
├── java/com/example/mini_project/
│   ├── SplashActivity.java          # Launch screen + routing
│   ├── UserSetupActivity.java       # First-launch onboarding
│   ├── MainActivity.java            # Home: list + search + filters
│   ├── AddItemActivity.java         # Add & Edit (dual-mode)
│   ├── DetailActivity.java          # Full item detail + actions
│   ├── ProfileActivity.java         # View & edit user profile
│   ├── Item.java                    # Data model (12 fields)
│   ├── ItemAdapter.java             # RecyclerView adapter with cards
│   ├── DatabaseHelper.java          # SQLite CRUD (v4 schema)
│   ├── NotificationHelper.java      # Local push notifications
│   └── MyApplication.java           # Dark mode initialisation
├── res/
│   ├── layout/                      # 6 activity layouts
│   ├── drawable/                    # Icons + backgrounds + badges
│   ├── values/colors.xml            # Light mode color tokens
│   └── values-night/colors.xml      # Dark mode color tokens
└── AndroidManifest.xml
```

---

*Generated: April 2026 · Campus Lost & Found v1.0*
