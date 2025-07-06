# Medical Claims Validator API Examples

**Validation Rules:**
Exclude a claim if:
- The place of service is **not** "office" AND for any charge, `procedureCode` begins with "9".
- The place of service **is** "office" AND for any charge, `procedureCode` begins with "6".
- For any charge, `procedureCode` is "99129" AND the patient's age is greater than or equal to 18.
- For any charge, `procedureCode` is "99396" AND the patient's age is less than 18 **OR** greater than 39.
- There are duplicate charges for the same `procedureCode` within a single claim.

**Important Note on Date-Dependent Rules:**
Some validation rules (e.g., those related to patient age) are dependent on the current date. The expected outcomes for these `curl` commands assume the system clock is relative to **July 5th, 2025**. If these commands are run significantly later (e.g., a few years from now), the calculated patient ages might change, potentially altering the validation results for rules based on age thresholds. For stable, automated testing, a fixed `Clock` is used in the unit test class: `com.craighdav.medical_claims_validator.service.MedicalClaimsValidatorServiceTest`.

## Test Case 1: All Invalid Claims

```bash
curl -X POST -H "Content-Type: application/json" -d '{"patients":[{"id":101,"firstName":"Alice","lastName":"Smith","birthDate":"1980-05-15"},{"id":102,"firstName":"Bob","lastName":"Johnson","birthDate":"2010-01-01"},{"id":103,"firstName":"Charlie","lastName":"Brown","birthDate":"1995-11-20"},{"id":104,"firstName":"Diana","lastName":"Prince","birthDate":"1970-03-01"}],"claims":[{"id":1,"patientId":101,"serviceDate":"2024-06-01","placeOfService":"office"},{"id":2,"patientId":102,"serviceDate":"2024-06-05","placeOfService":"hospital"},{"id":3,"patientId":103,"serviceDate":"2024-06-10","placeOfService":"office"},{"id":4,"patientId":104,"serviceDate":"2024-06-15","placeOfService":"office"},{"id":5,"patientId":101,"serviceDate":"2024-06-20","placeOfService":"office"}],"charges":[{"id":1001,"claimId":1,"procedureCode":12345,"amount":100},{"id":1002,"claimId":1,"procedureCode":99129,"amount":200},{"id":1003,"claimId":2,"procedureCode":90001,"amount":300},{"id":1004,"claimId":3,"procedureCode":60001,"amount":150},{"id":1005,"claimId":4,"procedureCode":99396,"amount":250},{"id":1006,"claimId":5,"procedureCode":70001,"amount":50},{"id":1007,"claimId":5,"procedureCode":70001,"amount":50}]}' http://localhost:8080/validateClaims
```

**Expected Output (assuming current date is July 5th, 2025):**
```json
{
  "validClaimIds": [],
  "invalidClaimIds": {
    "1": [
      "Charge: 1002 has procedure code 99129 with patientAge: 45"
    ],
    "2": [
      "Charge: 1003 has procedure code starting with 9 for NOT 'office'"
    ],
    "3": [
      "Charge: 1004 has procedure code starting with 6 for 'office'"
    ],
    "4": [
      "Charge: 1005 has procedure code 99396 with patientAge: 55"
    ],
    "5": [
      "Claim has duplicate charges for at least one procedure"
    ]
  }
}
```

**Reasoning for Expected Output for Test Case 1:**

* **Claim 1 (Patient 101, Alice Smith, born 1980-05-15):**
    * On July 5th, 2025, Alice is 45 years old.
    * Charge 1002 has `procedureCode` 99129.
    * **Rule triggered:** `procedureCode == 99129 AND patientAge > 18` (45 > 18).
    * Result: Invalid.
* **Claim 2 (Patient 102, Bob Johnson, born 2010-01-01):**
    * `placeOfService` is "hospital" (not "office").
    * Charge 1003 has `procedureCode` 90001 (starts with "9").
    * **Rule triggered:** `procedureCode begins with "9" AND placeOfService != "office"`.
    * Result: Invalid.
* **Claim 3 (Patient 103, Charlie Brown, born 1995-11-20):**
    * `placeOfService` is "office".
    * Charge 1004 has `procedureCode` 60001 (starts with "6").
    * **Rule triggered:** `procedureCode begins with "6" AND placeOfService == "office"`.
    * Result: Invalid.
* **Claim 4 (Patient 104, Diana Prince, born 1970-03-01):**
    * On July 5th, 2025, Diana is 55 years old.
    * Charge 1005 has `procedureCode` 99396.
    * **Rule triggered:** `procedureCode == 99396 AND (patientAge < 18 OR patientAge > 39)` (55 > 39).
    * Result: Invalid.
* **Claim 5 (Patient 101, Alice Smith, born 1980-05-15):**
    * Contains two charges (1006 and 1007) with the same `procedureCode` (70001).
    * **Rule triggered:** `There are duplicate charges for the same procedure code`.
    * Result: Invalid.
    
## Test Case 2: All Valid Claims

```bash
curl -X POST -H "Content-Type: application/json" -d '{"patients":[{"id":201,"firstName":"Valid","lastName":"Adult","birthDate":"1990-01-01"},{"id":202,"firstName":"Valid","lastName":"Teen","birthDate":"2008-03-01"},{"id":203,"firstName":"Valid","lastName":"Senior","birthDate":"1960-07-01"}],"claims":[{"id":11,"patientId":201,"serviceDate":"2025-01-10","placeOfService":"office"},{"id":12,"patientId":202,"serviceDate":"2025-02-15","placeOfService":"clinic"},{"id":13,"patientId":203,"serviceDate":"2025-03-20","placeOfService":"hospital"}],"charges":[{"id":2001,"claimId":11,"procedureCode":10001,"amount":100},{"id":2002,"claimId":11,"procedureCode":50001,"amount":150},{"id":2003,"claimId":12,"procedureCode":80001,"amount":200},{"id":2004,"claimId":12,"procedureCode":70001,"amount":250},{"id":2005,"claimId":13,"procedureCode":60001,"amount":300},{"id":2006,"claimId":13,"procedureCode":80001,"amount":350}]}' http://localhost:8080/validateClaims
```

**Expected Output:**
```json
{
  "validClaimIds": [11, 12, 13],
  "invalidClaimIds": {}
}
```
    
    
