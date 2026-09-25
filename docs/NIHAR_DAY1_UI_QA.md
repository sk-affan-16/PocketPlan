# PocketPlan — Nihar Day 1 UI/QA Plan

## 1. Purpose

This document defines the Day 1 UI consistency and QA expectations for PocketPlan.

The goal is to keep the application simple, clear, consistent, and easy to test during the 10-day development period.

---

## 2. UI Consistency Checklist

### General UI

* Keep screens clean and simple.
* Use consistent spacing between related elements.
* Keep text readable and avoid unnecessary decorative elements.
* Use clear and consistent button wording.
* Keep similar actions visually consistent across screens.
* Avoid unnecessary animations and complex UI effects.
* Do not introduce new UI patterns without coordination when shared resources are affected.

### User Feedback

* Feedback messages should be short and understandable.
* Do not show technical exception messages directly to users.
* Errors should clearly explain what the user needs to correct.
* Successful actions should provide appropriate feedback when needed.

### Input Fields

* Required fields should be clearly indicated through appropriate validation feedback.
* Amount fields should accept valid monetary values only.
* Invalid input should receive clear feedback.
* Missing category information should be handled clearly.
* Validation should not cause the application to crash.

---

## 3. Empty-State Specification

Empty states should explain the current situation without making it look like an application error.

### No Transactions

The screen should clearly indicate that there are currently no transactions.

Where appropriate, the empty state should tell the user what they can do next, such as adding a transaction.

### No Search Results

The screen should clearly indicate that no transactions match the current search or filter.

The user should be able to understand that the search produced no matching results.

### No Dashboard Data

When there is no relevant income, expense, or transaction data, the dashboard should handle the empty situation safely.

The application should not crash because data is unavailable.

### No Budget

If no monthly budget has been configured, the budget area should clearly indicate that a budget has not been set.

---

## 4. Validation-Feedback Specification

Validation feedback should be simple and user-friendly.

### Empty Required Field

Tell the user which required field needs information.

### Invalid Amount

Tell the user that the entered amount is invalid and needs to be corrected.

### Missing Category

Tell the user that a category needs to be selected.

### Invalid Budget

Tell the user that the budget value is invalid and needs to be corrected.

### Failed Operation

Show a short user-friendly message.

Do not expose database, Kotlin, Android, or exception details directly to the user.

---

## 5. Confirmation Dialog Specification

Confirmation dialogs should be used mainly for destructive actions.

### Delete Transaction

The dialog should:

* Clearly identify that the transaction will be deleted.
* Provide a Cancel option.
* Provide a confirmation action.
* Avoid unnecessary confirmation for harmless actions.

The wording should be simple and understandable.

---

## 6. QA Testing Checklist

### Application Startup

* [ ] Application launches successfully.
* [ ] No immediate crash occurs.
* [ ] Main screen loads correctly.

### Transactions

* [ ] Add income works.
* [ ] Add expense works.
* [ ] Transaction validation works.
* [ ] Transaction history displays correctly.
* [ ] Edit transaction works.
* [ ] Delete transaction works.
* [ ] Delete confirmation behaves correctly.

### Database Persistence

* [ ] Added data remains after application restart.
* [ ] Edited data appears correctly after restart.
* [ ] Deleted data remains deleted after restart.

### Dashboard

* [ ] Income summary displays correctly.
* [ ] Expense summary displays correctly.
* [ ] Balance displays correctly.
* [ ] Budget displays correctly.
* [ ] Remaining budget displays correctly.
* [ ] Empty dashboard data is handled safely.

### Search and Filter

* [ ] Search works with matching transactions.
* [ ] Search handles no matching results.
* [ ] Filters behave correctly.
* [ ] Clearing search/filter restores the expected transaction list.

### UI

* [ ] No text is clipped.
* [ ] No views overlap.
* [ ] Buttons are usable.
* [ ] Input fields are readable.
* [ ] Empty states are understandable.
* [ ] UI remains usable on supported screen sizes.
* [ ] Navigation works correctly.

---

## 7. Regression Testing

When a new feature is integrated, test the new feature and perform basic checks on existing features.

### After Dashboard Integration

Check:

* Application startup
* Add transaction
* Transaction history
* Dashboard
* Edit transaction
* Delete transaction

### After UI Changes

Check:

* Navigation
* Input fields
* Database persistence
* Dashboard
* Transaction history
* Empty states

Regression testing should remain practical for the 10-day project.

---

## 8. Bug Reporting Format

When a bug belongs to another member's module, report it instead of silently modifying that module.

### BUG

**PERSON / MODULE:**
...

**SCREEN:**
...

**STEPS TO REPRODUCE:**

1. ...
2. ...
3. ...

**EXPECTED:**
...

**ACTUAL:**
...

**ERROR MESSAGE:**
...

**SCREENSHOT:**
If available

**SEVERITY:**
Blocker / High / Medium / Low

### Ownership

* Affan — project setup, navigation, integration
* Rabin — transactions
* Subham — database/Room
* Prasannajit — dashboard, budget, calculations
* Nihar — UI/QA

Cross-module problems should be reported to MASTER CONTROL.

---

## 9. Day 1 Deliverables

Nihar's Day 1 deliverables are:

* UI consistency checklist
* QA/testing checklist
* Empty-state specification
* Validation-feedback specification
* Confirmation-dialog expectations
* Bug reporting format

No application implementation files are required for this Day 1 planning task.

---

## 10. Files to Avoid on Day 1

Nihar should not modify:

* MainActivity.kt
* AndroidManifest.xml
* Gradle files
* libs.versions.toml
* Navigation configuration
* Shared theme configuration
* Database contract
* Shared database models
* Other members' feature implementation files

Any shared-file UI change should be coordinated before modification.

---

## 11. Testing Responsibility

Testing begins during development.

Nihar should test features as they become available rather than waiting until the final day.

The final testing process should include both:

* Testing the newly integrated feature
* Basic regression testing of existing features
