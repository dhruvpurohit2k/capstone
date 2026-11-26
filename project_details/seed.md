**5.5 Sample Data**

The following illustrative records are internally consistent and may be used for database seeding, API demonstrations and functional testing. Password values are placeholders and must be replaced with BCrypt hashes in implementation.

**5.5.1 customer**



| customer\_id | email | password\_hash | full\_name | date\_of\_birth | phone | status |

|------------|-------|--------------|-----------|---------------|-------|--------|

| 1001 | aarav.sharma@example.com | {bcrypt}$2a$10$sample01 | Aarav Sharma | 1990-04-15 | 9876501001 | ACTIVE |

| 1002 | meera.iyer@example.com | {bcrypt}$2a$10$sample02 | Meera Iyer | 1985-09-22 | 9876501002 | ACTIVE |

| 1003 | rohan.verma@example.com | {bcrypt}$2a$10$sample03 | Rohan Verma | 2000-01-10 | 9876501003 | ACTIVE |



**5.5.2 insurance\_product**

| product\_id | product\_code | policy\_name | policy\_type | term\_months | premium\_frequency | premium\_amount | sum\_insured | age\_range | active |

|------------|--------------|-------------|-------------|-------------|-------------------|----------------|-------------|-----------|--------|

| 201 | HD-TERM-001 | HealthDekho Secure Term | TERM | 240 | MONTHLY | 1250.00 | 5000000.00 | 18-60 | TRUE |

| 202 | HD-HEALTH-001 | HealthDekho Family Health | HEALTH | 12 | YEARLY | 18000.00 | 1000000.00 | 18-65 | TRUE |

| 203 | HD-SAV-001 | HealthDekho Smart Savings | SAVINGS | 120 | QUARTERLY | 7500.00 | 1500000.00 | 21-55 | TRUE |



**5.5.3 customer\_policy**



| policy\_id | policy\_number | customer\_id | product\_id | start\_date | maturity\_date | frequency | premium\_amount | sum\_insured | total\_premium | status |

|-----------|---------------|-------------|------------|------------|---------------|-----------|----------------|-------------|---------------|--------|

| 3001 | HDP-2026-0001 | 1001 | 201 | 2026-11-20 | 2046-11-19 | MONTHLY | 1250.00 | 5000000.00 | 300000.00 | ACTIVE |

| 3002 | HDP-2026-0002 | 1002 | 202 | 2026-11-20 | 2027-11-19 | YEARLY | 18000.00 | 1000000.00 | 18000.00 | ACTIVE |



**5.5.4 premium\_installment**



| installment\_id | policy\_id | installment\_no | due\_date | grace\_end\_date | scheduled\_amount | paid\_amount | payment\_status |

|----------------|-----------|----------------|----------|----------------|------------------|-------------|----------------|

| 4001 | 3001 | 1 | 2026-11-20 | 2026-12-20 | 1250.00 | 1250.00 | PAID |

| 4002 | 3001 | 2 | 2026-12-20 | 2027-01-20 | 1250.00 | 500.00 | PARTIALLY\_PAID |

| 4003 | 3001 | 3 | 2027-01-20 | 2027-02-20 | 1250.00 | 0.00 | PENDING |

| 4004 | 3002 | 1 | 2026-11-20 | 2026-12-20 | 18000.00 | 18000.00 | PAID |



**5.5.5 premium\_payment**



| payment\_id | installment\_id | transaction\_reference | payment\_date | amount\_paid | penalty\_amount | payment\_method |

|------------|----------------|----------------------|--------------|-------------|----------------|----------------|

| 5001 | 4001 | TXN-HD-000001 | 2026-11-20 | 1250.00 | 0.00 | UPI |

| 5002 | 4002 | TXN-HD-000002 | 2026-12-20 | 500.00 | 0.00 | NET\_BANKING |

| 5003 | 4004 | TXN-HD-000003 | 2026-11-20 | 18000.00 | 0.00 | CARD |



**5.5.6 ai\_recommendation\_request**

| request\_id | customer\_id | insurance\_goals | preferred\_term | preferred\_type | calculated\_age | model\_name | model\_version | prompt\_version | requested\_at |

|------------|-------------|-----------------|----------------|----------------|----------------|------------|---------------|----------------|--------------|

| 6001 | 1001 | Family income protection | 20 years | TERM | 36 | configured-chat-model | deployment-v1 | rec-v1 | 2026-11-20 10:15:00+05:30 |

| 6002 | 1003 | Health coverage for self and parents | 1 year | HEALTH | 26 | configured-chat-model | deployment-v1 | rec-v1 | 2026-11-20 10:30:00+05:30 |

**5.5.7 ai\_recommendation\_item**

| item\_id | request\_id | product\_id | rank\_order | recommendation\_reason | suitability | selected |

|----------|------------|------------|------------|-----------------------|-------------|----------|

| 7001 | 6001 | 201 | 1 | Long-term protection aligned with income-replacement goal | HIGH | TRUE |

| 7002 | 6001 | 203 | 2 | Savings option supports a secondary wealth goal | MEDIUM | FALSE |

| 7003 | 6002 | 202 | 1 | Annual family health cover matches stated preference | HIGH | FALSE |

**5.5.8 Future-Enhancement Sample Data**

The following records are examples only for FE-1 and must not be loaded as part of the current assessment unless the future claim capability is implemented.

**insurance\_claim**

| claim\_id | claim\_number | policy\_id | customer\_id | claim\_date | claim\_amount | document\_status | claim\_status |

|----------|-------------|-----------|-------------|------------|--------------|-----------------|--------------|

| 8001 | CLM-2027-0001 | 3002 | 1002 | 2027-03-15 | 85000.00 | AVAILABLE | PENDING\_REVIEW |



**claim\_assessment**

| assessment\_id | claim\_id | premium\_compliance | policy\_validity | document\_verified | ai\_summary | model\_version |

|---------------|----------|-------------------|-----------------|------------------|------------|---------------|

| 8101 | 8001 | COMPLIANT | VALID | TRUE | Policy and payment checks passed; manual review required. | deployment-v1 |



**claim\_decision**

| decision\_id | claim\_id | final\_status | decision\_reasons | processing\_user\_id | decision\_timestamp |

|-------------|----------|--------------|------------------|-------------------|--------------------|

| 8201 | 8001 | APPROVED | All mandatory deterministic conditions satisfied. | OPS-USER-101 | 2027-03-16 14:30:00+05:30 |

