# Medical Claims Validator
A Spring Boot RESTful API for validating medical claims based on a set of predefined business rules.

## Table of Contents

** [About The Project](#about-the-project)
* [Features](#features)
* [Validation Rules](#validation-rules)
* [Technologies Used](#technologies-used)
* [Getting Started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Installation](#installation)
* [API Endpoints](#api-endpoints)
* [Testing](#testing)
* [License](#license)
* [Contact](#contact)

## About The Project

This project addresses the challenge of validating complex medical claims data, which arrives as three independent, inter-related lists: patients, claims, and charges. My solution focuses on **efficient data processing and robust algorithmic logic** to manage these interdependencies and produce a comprehensive validation report.

Key to the design is the strategic use of **in-memory data structures**, such as `Map` and `Set`, to create highly efficient lookup mechanisms. These structures facilitate rapid association of claims with patients, and charges with claims, as well as the quick identification of issues. The core validation logic within `MedicalClaimsValidatorService` demonstrates a keen understanding of **problem decomposition**, adopting a **process-of-elimination approach** by identifying invalid claims first. This recognition simplifies the overall validation flow, as a single invalid charge invalidates an entire claim. Furthermore, the robust detection of duplicate charges for a claim involved designing a sophisticated data transformation using **nested Stream API grouping**, creating a structured `Map` that efficiently tracks `procedureCode` occurrences per `claimId`. Developing such a design required significant logical reasoning. Despite the intricate nature of the validation rules, the solution within `MedicalClaimsValidatorService` is remarkably **concise and straightforward**, leveraging **Java's Stream API** for elegant data transformations and aggregations. The overall algorithmic approach, combined with other necessary calculations, ensures all business requirements are met.

The application leverages Spring Boot for building a scalable RESTful API and utilizes Jakarta Bean Validation for initial input data integrity. The entire validation process is encapsulated, emphasizing maintainability, testability, and **clear separation of concerns**, ultimately delivering a structured response that clearly distinguishes valid claims from invalid ones, complete with detailed reasons for rejection.

## Features
RESTful API: Exposes an endpoint to receive and process medical claims data.

Comprehensive Validation: Applies multiple business rules to claims, patients, and charges.

Detailed Error Reporting: For invalid claims, provides specific reasons/issues.

Structured Output: Returns separate lists of valid claim IDs and invalid claims with associated issues.

Data Model: Clearly defined data models for Patient, Claim, Charge, RawMedicalClaimsData (input), and ProcessedMedicalClaimsData (output).

Date-Dependent Rule Handling: Acknowledges and accounts for validation rules that depend on the current date, with a configurable Clock for testing stability.

Validation Rules
The API validates medical claims against the following rules:

* Exclude a claim if:
  * The place of service is not "office" AND for any charge, procedureCode begins with "9".
  * The place of service is "office" AND for any charge, procedureCode begins with "6".
  * For any charge, procedureCode is "99129" AND the patient's age is greater than or equal to 18.
  * For any charge, procedureCode is "99396" AND the patient's age is less than 18 OR greater than 39.
  * There are duplicate charges for the same procedureCode within a single claim.
* **Important Note on Date-Dependent Rules:** Some validation rules (e.g., those related to patient age) are dependent on the current date. The expected outcomes for curl commands assume the system clock is relative to July 5th, 2025. If these commands are run significantly later (e.g., a few years from now), the calculated patient ages might change, potentially altering the validation results for rules based on age thresholds. For stable, automated testing, a fixed Clock is used in the unit test class: com.craighdav.medical_claims_validator.service.MedicalClaimsValidatorServiceTest.

## Technologies Used
* **Java** (JDK 17+)
* **Spring Boot** (3.2.0+) - For building RESTful APIs.
* **Java Stream API** - For declarative data processing and functional programming patterns.
* **Jackson** - For JSON serialization/deserialization.
* **Jakarta Bean Validation** - For declarative data validation on input models.
* **JUnit 5** - For unit testing with AAA pattern.

## Getting Started
To get a local copy of the project up and running, follow these simple steps.

### Prerequisites
* Java Development Kit (JDK) 17 or higher
* Apache Maven 3.6.0 or higher
* Git

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/craighdav/medical-claims-validator.git
    cd medical-claims-validator
    ```
  You can also browse the repository directly [repository on GitHub](https://github.com/craighdav/medical-claims-validator).
  
2.  **Build the project using Maven:**
    ```bash
    mvn clean install
    ```

3.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```
    The application will typically start on `http://localhost:8080`.

## API Endpoints

The API exposes a single endpoint for claims validation.

**`POST /validateClaims`**

Validates a batch of medical claims.

* **Request Body (`application/json`):**
    Expects a JSON object conforming to the `RawMedicalClaimsData` structure, containing lists of `patients`, `claims`, and `charges`.

    * `patients`: A list of `Patient` objects.
        * `id`: `long` (positive integer)
        * `firstName`: `String` (not null)
        * `lastName`: `String` (not null)
        * `birthDate`: `LocalDate` (must be in the past, not null)
    * `claims`: A list of `Claim` objects.
        * `id`: `long` (positive integer)
        * `patientId`: `long` (positive integer)
        * `serviceDate`: `LocalDate` (must be in the past, not null)
        * `placeOfService`: `String` (not null)
    * `charges`: A list of `Charge` objects.
        * `id`: `long` (positive integer)
        * `claimId`: `long` (positive integer)
        * `procedureCode`: `long` (positive integer)
        * `amount`: `int` (valid integer)

* **Response Body (`application/json`):**
    Returns a JSON object conforming to the `ProcessedMedicalClaimsData` structure.

    * `validClaimIds`: A list of `long` representing the IDs of claims that passed all validations.
    * `invalidClaimIds`: A map where keys are `long` (claim IDs) and values are `Set<String>` of issues/reasons for invalidation.

**Example `curl` commands and expected outputs (assuming system clock is July 5th, 2025):**

### Test Case 1: All Invalid Claims

```bash
curl -X POST -H "Content-Type: application/json" -d '{"patients":[{"id":101,"firstName":"Alice","lastName":"Smith","birthDate":"1980-05-15"},{"id":102,"firstName":"Bob","lastName":"Johnson","birthDate":"2010-01-01"},{"id":103,"firstName":"Charlie","lastName":"Brown","birthDate":"1995-11-20"},{"id":104,"firstName":"Diana","lastName":"Prince","birthDate":"1970-03-01"}],"claims":[{"id":1,"patientId":101,"serviceDate":"2024-06-01","placeOfService":"office"},{"id":2,"patientId":102,"serviceDate":"2024-06-05","placeOfService":"hospital"},{"id":3,"patientId":103,"serviceDate":"2024-06-10","placeOfService":"office"},{"id":4,"patientId":104,"serviceDate":"2024-06-15","placeOfService":"office"},{"id":5,"patientId":101,"serviceDate":"2024-06-20","placeOfService":"office"}],"charges":[{"id":1001,"claimId":1,"procedureCode":12345,"amount":100},{"id":1002,"claimId":1,"procedureCode":99129,"amount":200},{"id":1003,"claimId":2,"procedureCode":90001,"amount":300},{"id":1004,"claimId":3,"procedureCode":60001,"amount":150},{"id":1005,"claimId":4,"procedureCode":99396,"amount":250},{"id":1006,"claimId":5,"procedureCode":70001,"amount":50},{"id":1007,"claimId":5,"procedureCode":70001,"amount":50}]}' http://localhost:8080/validateClaims
```

**Expected Output** (assuming current date is July 5th, 2025):

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

* Reasoning for Expected Output for Test Case 1:

  * **Claim 1 (Patient 101, Alice Smith, born 1980-05-15):** On July 5th, 2025, Alice is 45 years old. Charge 1002 has procedureCode 99129. Rule triggered: procedureCode == 99129 AND patientAge > 18 (45 > 18). Result: Invalid.

  * **Claim 2 (Patient 102, Bob Johnson, born 2010-01-01):** placeOfService is "hospital" (not "office"). Charge 1003 has procedureCode 90001 (starts with "9"). Rule triggered: procedureCode begins with "9" AND placeOfService != "office". Result: Invalid.

  * **Claim 3 (Patient 103, Charlie Brown, born 1995-11-20):** placeOfService is "office". Charge 1004 has procedureCode 60001 (starts with "6"). Rule triggered: procedureCode begins with "6" AND placeOfService == "office". Result: Invalid.

  * **Claim 4 (Patient 104, Diana Prince, born 1970-03-01):** On July 5th, 2025, Diana is 55 years old. Charge 1005 has procedureCode 99396. Rule triggered: procedureCode == 99396 AND (patientAge < 18 OR patientAge > 39) (55 > 39). Result: Invalid.

  * **Claim 5 (Patient 101, Alice Smith, born 1980-05-15):** Contains two charges (1006 and 1007) with the same procedureCode (70001). Rule triggered: There are duplicate charges for the same procedure code. Result: Invalid.

### Test Case 2: All Valid Claims

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

Testing
Unit and integration tests are available for key functionalities, including the core validation logic (MedicalClaimsValidatorServiceTest) and custom utilities (MathUtilsTest).

You can run all tests using Maven:

```bash
mvn test
```

License
Distributed under the MIT License. See LICENSE.md for more information.

Contact
Craig H. Davis - craighdav@msn.com

Project Link: [https://github.com/craighdav/medical-claims-validator](https://github.com/craighdav/medical-claims-validator)
