**3.  Functional Requirements**

**FR-1: Customer Dashboard, Policy Discovery and Registration**

Business Objective: Provide customers with a personalized dashboard to view their existing insurance policies, explore available insurance products and initiate registration of suitable insurance policies.

**A.**  **Customer Authentication**

* Develop a login interface using React.js.
* Implement customer authentication using Spring Security.
* Authenticate customers against the configured customer credentials.
* On successful authentication, redirect the customer to the dashboard.
* Display an appropriate authentication error message for invalid credentials.
* Restrict customer-specific dashboard information to the authenticated customer.

**B. Customer Dashboard**

* Develop a customer dashboard displaying all insurance policies registered by the logged-in customer.
* Retrieve the policy information associated with the authenticated customer.
* Display the following information in a tabular format:



| Field | Description |

|---------|-------------|

| Policy Identifier | Unique policy number |

| Policy Name | Registered insurance product |

| Policy Type | Insurance category |

| Policy Term | Duration of the policy |

| Policy Start Date | Policy commencement date |

| Policy Maturity Date | Policy maturity date |

| Premium Frequency | Monthly, Quarterly, Half-Yearly or Yearly |

| Premium Amount | Premium payable per installment |

| Sum Insured | Insurance coverage amount |

| Policy Status | Current policy status |



* Display an appropriate message when the customer has not registered any insurance policy.

**C. View Available Insurance Policies**

* Retrieve all active insurance products from the PostgreSQL database.
* Retrieve the existing policies of the logged-in customer.
* Exclude products for which the customer has already registered a policy, according to the defined duplicate-registration business rule.
* Display the remaining available insurance products in a tabular format:

| Field | Description |

|---------|-------------|

| Product Code | Unique insurance product code |

| Policy Name | Name of the insurance product |

| Policy Type | Insurance category |

| Policy Term | Duration of the policy |

| Sum Insured | Insurance coverage amount |

| Premium Frequency | Premium payment frequency |

| Premium Amount | Premium payable per installment |

| Action | Register button |

* Provide a Register button against every available insurance product.
* On clicking Register, navigate the customer to the policy registration interface with the selected product information pre-populated.

**FR-2: AI Insurance Recommendation**

Business Objective: Generate personalized, explainable insurance product recommendations using Spring AI while restricting recommendations to eligible products available in the application database.

**D. AI-Based Policy Recommendations**

* Provide an option on the customer dashboard to generate personalized insurance policy recommendations using Spring AI.
* Allow customers to provide one or more of the following inputs:

Date of Birth (DOB)

Insurance Goals and Preferences

Preferred Policy Term

Insurance Type, wherever applicable

* Automatically calculate the customer's age using the Java Time API.
* Retrieve active insurance products from the database.
* Retrieve the customer's existing registered policies.
* Exclude products already availed by the customer.
* Integrate Spring AI with a configured AI model to generate personalized recommendations based on customer inputs and available insurance products.
* Ensure that AI-generated recommendations reference only products retrieved from the database.
* Display the generated recommendations in a tabular format:



| Field | Description |

|---------|-------------|

| Product Code | Unique insurance product code |

| Policy Name | Recommended insurance product |

| Policy Type | Insurance category |

| Policy Term | Duration of the policy |

| Sum Insured | Insurance coverage amount |

| Premium Frequency | Premium payment frequency |

| Premium Amount | Premium payable per installment |

| Recommendation Reason | AI-generated recommendation rationale |

| Suitability | Suitability for the customer's goals |

| Action | Register button |Provide a Register button against every recommended insurance product.|



* On clicking Register, navigate the customer to the policy registration interface with the selected product information pre-populated.
* Save generated recommendations against the customer in the database.

**E. Java 21 Implementation Expectations**

Records for request and response DTOs.

LocalDate and Java Time API for date calculations.

Optional for repository lookups.

Stream API for filtering available products and excluding existing policies.

Collectors and suitable collection operations.

Pattern matching with instanceof wherever appropriate.

**FR-3: Insurance Policy Registration**

Business Objective: Enable customers to register new insurance policies and automatically calculate policy-related financial and date information.

**A. Policy Registration Interface**

Develop a React-based registration form that captures and displays the following information:



| Field | Expected Behaviour |

|---------|-------------------|

| Customer Name | Automatically populated from logged-in customer details |

| Date of Birth | Automatically populated from customer details |

| Contact Information | Automatically populated from customer details |

| Policy Type | Retrieved from the selected insurance product |

| Policy Name | Pre-populated based on selected product |

| Policy Term | Automatically populated based on selected product |

| Policy Start Date | Default to current date |

| Policy Maturity Date | Automatically calculated based on policy term |

| Premium Payment Frequency | Automatically populated based on selected product |

| Premium Amount | Retrieved from the selected insurance product |

| Sum Insured | Automatically populated based on selected product |

| Total Premium Payable | Calculated based on premium amount, frequency and policy term |



**B. Business and Validation Requirements**

* Validate all mandatory fields before submitting the registration request.
* Ensure that the customer's date of birth is not in the future.
* Validate customer eligibility against the minimum and maximum entry age configured for the insurance product.
* Validate the logical consistency of policy start and maturity dates.
* Ensure premium amount, sum insured and policy term are positive.
* Generate a unique policy identifier for every registered policy.
* Prevent duplicate policy registration for the same customer and insurance product according to the defined business rules.
* Calculate the total premium payable based on the selected premium frequency and policy term.
* Generate the premium installment schedule for the registered policy.
* Persist customer policy information and the associated installment schedule in PostgreSQL using Spring Data JPA.
* Display the generated policy identifier and a confirmation message after successful registration.
* Handle validation errors, database failures and business-rule violations appropriately.

**C. Java 21 Implementation Expectations**

* Records for immutable request and response DTOs.
* LocalDate and Java Time API for date calculations and validations.
* BigDecimal for financial calculations.
* Optional for handling repository lookups.
* Appropriate Stream API and collection operations.

**FR-4: Insurance Premium Payment Management**

Business Objective: Provide functionality to retrieve an existing policy, view its premium payment schedule and update premium payment information whenever a customer makes a payment.

**A. Policy Search**

Develop a React interface that allows users to search for an existing policy using its unique policy identifier. Display the following information:

Policy Identifier

Customer Name

Policy Name

Policy Term

Policy Start Date

Policy Maturity Date

Total Premium Payable

Total Premium Paid

Total Remaining Premium Amount

**B. Premium Payment Schedule**

Display all premium installments in a tabular view.

| Field | Description |

|---------|-------------|

| Installment Number | Sequential installment number |

| Premium Amount | Scheduled premium amount |

| Due Date | Premium installment due date |

| Grace Period End Date | Last date of the grace period |

| Payment Status | PAID / PENDING / PARTIALLY\_PAID / OVERDUE |

| Payment Selection | Checkbox for selecting eligible pending installments |

**C. Premium Payment Processing**

Allow users to select one or multiple pending premium installments and update their payment information. The application must:

* Capture the premium amount paid.
* Automatically update the payment status after successful payment.
* Automatically record the payment date and transaction details.
* Apply a 10% penalty on the outstanding premium amount if payment is made after the one-month grace period.
* Automatically recalculate the outstanding premium amount.
* Maintain an accurate premium payment history.
* Prevent duplicate payments against the same scheduled installment.
* Prevent payments exceeding the outstanding amount.
* Support partial payments, wherever applicable.
* Ensure that a policy is marked as fully paid only when all applicable premium installments have been paid.

**D. Java 21 Implementation Expectations**

Records for premium payment request and response DTOs.

YearMonth for representing premium payment periods.

BigDecimal for financial calculations.

Switch expressions or pattern matching with switch for payment-status processing.

Stream API and collection operations for processing payment records.

Transaction management for payment processing.

**FR-5: Customer Insurance Policy Search**

Business Objective: Enable users to retrieve and view insurance policies associated with a customer using different search criteria.

**A. Search Interface**

Develop a React-based search interface with the following fields:

Customer Name

Policy Identifier

Date of Birth

Allow users to perform searches using:

* Policy Identifier.
* Customer Name.
* Policy Identifier and Date of Birth.

**B. Search Results**

Retrieve all matching insurance policies and display the results in a tabular format.

| Field | Description |

|---------|-------------|

| Policy Identifier | Unique policy reference |

| Customer Name | Customer's name |

| Date of Birth | Customer's date of birth |

| Policy Name | Registered insurance product |

| Policy Type | Insurance category |

| Policy Start Date | Policy commencement date |

| Policy Maturity Date | Policy maturity date |

| Total Premium Payable | Total policy premium |

| Premium Payment Status | Current payment status |

| Total Premium Paid | Cumulative paid amount |

| Remaining Premium | Outstanding premium amount |

**C. Additional Requirements**

* Retrieve all policies matching the supplied search criteria.
* Display an appropriate message when no matching records are found.
* Validate the search criteria before processing.
* Implement efficient database queries using Spring Data JPA.
* Support pagination or bounded result retrieval wherever appropriate.

**D. Java 21 Implementation Expectations**

* Records for search response DTOs.
* Stream API for transforming entity objects into DTOs.
* Collectors.groupingBy() or other suitable collectors wherever required.
* Optional for single-record lookups.
* Pagination for handling larger result sets.
