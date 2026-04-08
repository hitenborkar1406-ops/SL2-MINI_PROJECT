# 📌 Product Requirements Document (PRD)

## 🧾 Product Name

Campus Lost & Found Board

---

## 🎯 Objective

Build an Android application that allows students to post, browse, and manage lost and found items within a campus environment. The app should function fully offline using local storage.

---

## 👥 Target Users

* College students
* Campus staff

---

## 📱 Platform

* Android (Java)
* Minimum SDK: 21+

---

## 🧠 Core Functional Requirements

### 1. User Setup (First Launch)

* On first app launch:

    * Prompt user to enter:

        * Name
        * Contact information
* Store using SharedPreferences
* Skip this screen on subsequent launches

---

### 2. Add Item (Lost/Found Post)

#### Input Fields:

* Item Name (required)
* Description
* Location
* Type:

    * Lost / Found (dropdown or toggle)
* Image:

    * Select from gallery

#### Behavior:

* Validate required fields
* Save image to internal storage
* Store only image path in database
* Insert data into SQLite database

#### Output:

* Toast: "Item Added"
* Trigger notification

---

### 3. View Items (Home Screen)

#### UI:

* RecyclerView list

#### Each Item Card:

* Item Name
* Type (Lost/Found)
* Location
* Thumbnail image

#### Behavior:

* Fetch all records from SQLite
* Display in scrollable list
* Clicking item → opens detail screen

---

### 4. Item Detail Screen

#### Display:

* Full item details:

    * Name
    * Description
    * Location
    * Type
    * Full image

#### Actions:

* Delete button

    * Show AlertDialog confirmation
    * On confirm:

        * Delete item from database
        * Navigate back

---

### 5. Notifications

#### Trigger:

* When new item is added

#### Behavior:

* Show system notification:

    * Title: "Lost & Found"
    * Message: "Item may be found!"

---

### 6. Database (SQLite)

#### Table: items

| Column   | Type                         |
| -------- | ---------------------------- |
| id       | INTEGER (PK, Auto Increment) |
| name     | TEXT                         |
| desc     | TEXT                         |
| location | TEXT                         |
| type     | TEXT                         |
| image    | TEXT (file path)             |

---

### 7. Image Handling

* Pick from gallery
* Convert to Bitmap
* Save in internal storage
* Store file path
* Load using URI when needed
* Prevent crashes on null image

---

## ⚙️ Non-Functional Requirements

* App must work offline
* No crashes on invalid input
* Handle null/empty states safely
* Smooth RecyclerView scrolling
* Lightweight storage usage

---

## 🧱 Architecture

* Language: Java
* Pattern: Basic MVC
* Components:

    * Activities (UI + Controller)
    * SQLite (Model)
    * Adapter (RecyclerView binding)

---

## 🔐 Permissions

* READ_EXTERNAL_STORAGE (for gallery access)

---

## 🔄 Navigation Flow

1. Launch App
   → Check SharedPreferences
   → If user not set → UserSetupActivity
   → Else → MainActivity

2. MainActivity
   → View list
   → Add button → AddItemActivity

3. AddItemActivity
   → Save → Back to MainActivity

4. Item Click
   → DetailActivity

5. DetailActivity
   → Delete → Back to MainActivity

---

## 🧪 Edge Cases

* No image selected
* Empty item name
* Database empty state
* Invalid image URI
* App reopened after deletion

---

## 🚀 Optional Enhancements (Bonus)

* Search items by name
* Filter Lost vs Found
* Sort by latest
* Add timestamps
* Dark mode UI

---

## 📦 Deliverables

* Fully runnable Android Studio project
* Clean code with proper structure
* No hardcoded crashes
* All features implemented as specified

---

## ❌ Out of Scope

* Backend / cloud sync
* User authentication
* Real-time updates
* Push notifications (server-based)

---

## 🧠 Success Criteria

* User can:
  ✔ Add item
  ✔ View items
  ✔ Delete item
  ✔ See notification
  ✔ Use app offline

* App runs without crashes on standard Android devices

---

END OF PRD
