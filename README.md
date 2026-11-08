# PocketPlan — Personal Budget Manager

## 1. About the Project

PocketPlan is an Android application developed as a 10-day minor project. It helps users manage personal income, expenses and monthly budgets using locally stored transaction data.

## 2. Features

- Dashboard with financial summary
- Add income transactions
- Add expense transactions
- Edit existing transactions
- Delete transactions with confirmation
- Transaction history
- Search transactions
- Filter income and expenses
- Monthly budget management
- Remaining budget calculation
- Local data persistence using Room Database

## 3. Technology Stack

- Language: Kotlin
- IDE: Android Studio
- UI: XML Views
- Database: Room Database
- Architecture: Simple MVVM-style architecture
- Build System: Gradle
- Version Control: Git and GitHub

## 4. Android Configuration

- Application ID: `com.example.pocketplan`
- Minimum SDK: 24
- Compile SDK: 37
- Target SDK: 36

## 5. Project Architecture

The application uses a simple fragment-based structure.

Main components include:

- MainActivity
- DashboardFragment
- AddTransactionFragment
- TransactionHistoryFragment
- BudgetFragment
- Room Database and DAO
- Repository and ViewModel

## 6. How to Run the Project

1. Install Android Studio.
2. Clone the PocketPlan GitHub repository.
3. Open the project in Android Studio.
4. Allow Gradle synchronization to complete.
5. Connect an Android device or start an emulator.
6. Select the app run configuration.
7. Click the Run button.

## 7. Build the APK

Run this command from the project directory:

`.\gradlew.bat :app:assembleDebug`

The generated debug APK is located at:

`app/build/outputs/apk/debug/app-debug.apk`

## 8. Testing

The following features were manually tested:

- Dashboard loading
- Income and expense entry
- Transaction history
- Search and filters
- Transaction editing
- Transaction deletion and confirmation
- Monthly budget saving
- Remaining budget calculation

The integrated project successfully completed a clean Gradle build and emulator installation.

## 9. Team Members

1. Sk Affan Raja Kadri
2. Subham Kumar Pradhan
3. Rabin Samanta
4. Prasannajit Mohanty
5. Nihar Kumar Behera

## 10. Project Repository

GitHub: https://github.com/sk-affan-16/PocketPlan

---

PocketPlan — Personal Budget Manager

Developed as an Android minor project.
