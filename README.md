# 
## UC2 - Feet and Inches Measurement Equality
### Overview
> UC2 extends the initial system to support both **Inches** and **Feet**. This allows for handling multiple measurement units independently.  While both units are supported, they are treated as separate entities, ensuring strict type safety and preventing logical errors in comparisons.
### Date : 18 Feb 2026
* Implemented a separate `Inches` class mirroring the `Feet` class structure.
* Added static methods `compareFeet()` and `compareInches()` for cleaner API usage.
---
### Performed operations in the following steps:
* **Step 1** – Implementing the Inches Class
  > 1. Developed a standalone `Inches` inner class with a private final field for value storage.
  > 2. Overrode the `equals()` method to handle identity, null, and type-specific value checks.
* **Step 2** – Refactoring the Application API
  > 1. Moved instantiation logic from the `main` method into dedicated static comparison methods.
  > 2. Ensured both classes utilize `Double.compare()` to handle floating-point precision consistently.
* **Step 3** – Validation and Testing
  > 1. Verified same-unit equality (Inch vs. Inch and Foot vs. Foot).
  > 2. Confirmed that cross-unit equality (Foot vs. Inch) correctly returns `false` due to type checking.
---
### Features
* **Ultimate Unit Support:**  Independent implementation of `Feet` and `Inches` classes.

* **Decoupled Comparison Logic:**  Static helper methods are introduced for equality checks, freeing up the `main` method.
* **Value-Based Equality:**  Comparison is based on internal numeric values rather than memory references.
* **Strict Type Safety:**  Prevents a `Feet` object from being considered equal to an `Inches` object even if their numeric values match.
---
### Concepts Learned in UC2
* **Encapsulation Expansion:** Applying object-oriented principles across multiple similar domains.
* **Type-Based Guardrails:** Using `getClass()` to isolate distinct measurement categories
---
code link [UC 2](https://github.com/harsh-vardhan36/QuantityMeasurementApp/tree/feature/UC2-InchEquality/src)
