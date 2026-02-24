# QuantityMeasurementApp

## UC4 - Extended Unit Support
> This branch contains the code demonstrating the extended units of measurement. It essentially illustrates how we can add more units to an enum  and therefore with a generic class, we can use multiple units.

### Date : 20 Feb 2026
* I worked on feature 4, extending the unit class and adding more units and their conversion rates.
* I modified the Length class’s Enum to include two new units: Yards and Centimetres, along with their respective conversion rates.
* I performed the following steps:
  * Step 1: Update the LengthUnit Enum.
        - I added a YARDS constant with a conversion factor of 3.0 (since 1 yard equals 3 feet).
        - I added a CENTIMETRES constant with a conversion factor of 0.393701 (since 1 cm equals 0.393701 inches).
        - I verified the conversion factor calculations: 1 yard divided by 1 foot equals 3.0 and 1 cm divided by 1 inch equals 0.39307.
        - I confirmed that the conversion factor calculation for 1 yard divided by 1 foot is correct.
    * Step 2: Verify the QuantityLength Class.
        - I confirmed that the existing equals() method and convertToFeet() logic work correctly for the new unit.
        - No changes to the QuantityLength class are needed due to its generic design from UC3.
    * Step 3: Test Coverage.
        - I ensured comprehensive test cases cover comparisons between yards, yards to feet, and yards to inches.
        - I ensured comprehensive test cases cover comparisons between yards, centimetres, centimetres to centimetres, and centimetres to inches, and so on.
        - I verified cross-unit equality with various combinations.
* Based on the test cases, the program was modified.
* I pushed the code to the repository.
---
* Code : [UC 4](https://github.com/harsh-vardhan36/QuantityMeasurementApp/tree/feature/UC4-YardEquality/src)
---
