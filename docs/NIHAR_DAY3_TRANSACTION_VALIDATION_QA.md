# Nihar Day 3 — Transaction Validation QA

## Objective

Review the transaction flow for basic validation and identify QA requirements for invalid or incomplete transaction input.

## Validation Areas

### 1. Amount Validation

The transaction amount should be checked before saving.

QA should verify:

* Empty amount is rejected.
* Invalid amount input is rejected.
* Zero or inappropriate amount input is reviewed.
* Valid positive amount can continue to the save flow.

### 2. Transaction Type Validation

QA should verify that the user cannot save a transaction without selecting the required transaction type.

Expected types:

* Income
* Expense

### 3. Category Validation

QA should verify that required category information is selected before saving a transaction.

### 4. Required Field Validation

QA should verify that required transaction fields are not left empty.

### 5. Save Validation

The application should validate the entered information before attempting to save the transaction.

### 6. Error Feedback

When validation fails, the user should receive clear feedback explaining what needs to be corrected.

## QA Test Cases

| Test Case                | Input                     | Expected Result          |
| ------------------------ | ------------------------- | ------------------------ |
| Empty amount             | Amount empty              | Validation message shown |
| Invalid amount           | Invalid/non-numeric input | Validation prevents save |
| Missing transaction type | No type selected          | Validation message shown |
| Missing category         | No category selected      | Validation message shown |
| Missing required field   | Required field empty      | Validation message shown |
| Valid transaction        | All required data valid   | Save flow can continue   |

## Current Status

Day 3 validation QA requirements documented.

Runtime validation testing will be performed when the transaction form and save flow are available for testing.

## Ownership

Nihar responsibility:

* QA review
* Validation test-case documentation
* Bug reporting
* Retesting

Nihar should not modify transaction business logic unless explicitly assigned.

## Result

Day 3 transaction validation QA documentation completed.
