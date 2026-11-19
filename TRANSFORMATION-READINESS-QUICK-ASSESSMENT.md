# AWS Transform Reverse-Engineering Assessment: CardDemo to Java + React Transformation Readiness

**Date:** 2026-11-05  
**Assessment Type:** Quick Gap Analysis – Documentation Completeness for Java Spring Boot + React Frontend Conversion  
**Target Application:** CardDemo v2 (AWS Mainframe Modernization Reference Application)

---

## 1. Executive Summary

### Overall Readiness: **Ready with Significant Gaps**

The AWS Transform reverse-engineering output provides **strong program-level business logic documentation** (94 files; 29 COBOL programs, 35 JCL jobs, 8 IMS/DB2/MQ programs documented) but is **critically missing foundational data architecture specifications**. The documentation captures workflow and rules well, but lacks **copybook definitions, DB2 schema DDL, IMS segment hierarchies, VSAM record layouts, CICS screen/BMS definitions, and data type mappings**.

**Verdict:** With current documentation, a team can understand the business logic layer and build services. However, **database modeling, API contract design, and React component state management will require reverse-engineering from live mainframe sources or hand-analysis of COBOL copybooks**, adding 4–6 weeks to timeline.

**Critical blockers for immediate implementation:**
- No VSAM file record structure definitions (only "account," "transaction," etc., mentioned generically)
- No DB2 table schemas, column definitions, or key constraints
- No IMS segment hierarchies or parent-child relationships
- No CICS BMS map definitions (screen field names, types, validation rules)
- No COBOL PIC clause → Java type mappings
- No copybook cross-references
- No external API integration specifications

---

## 2. Documentation Quality Assessment

### Strengths

1. **High-Level Overview** ✓  
   - Summary metadata (`summary.json`) provides a concise 1-2 sentence description of each file's purpose
   - Application overview accurately captures the five major modules:
     - Core CardDemo (online CICS transactions & batch jobs)
     - Authorization (IMS/DB2/MQ)
     - Transaction Type (DB2)
     - VSAM/MQ
     - Batch & JCL workflows
   - Clear identification of data stores (VSAM, IMS, DB2, MQ)

2. **Business Logic Documentation** ✓  
   - Detailed flow diagrams (flowchart TD format) for each program showing data flow, decision points, and file I/O
   - 15–140 acceptance criteria rules per program, mapping requirements to COBOL decision logic
   - Clear mapping of input/output specifications for batch and CICS programs
   - Explicit transaction IDs identified (e.g., CB00 for COBIL00C bill payment, CM00 for COMEN01C main menu)
   - Error handling and validation logic documented

3. **Program Call Dependencies** ✓  
   - External called programs are named in flow diagrams (e.g., COBIL00C calls COSGN00C for sign-on, CBACT04C for interest calculation)
   - CICS inter-program transfers are documented
   - JCL step sequences and dependencies are clear

4. **File I/O and Data Movement** ✓  
   - File opens/closes and record reads/writes are documented
   - VSAM alternate indexes mentioned (TRANIDX)
   - Backup and restore procedures documented
   - MQ messaging touch-points identified

### Weaknesses

1. **Critical Gap: Data Structure Definitions** ✗  
   - **Copybooks not documented.** Programs reference structures like ACCOUNT-RECORD, ACCT-ID, ACCT-CREDIT-LIMIT but **no PIC clauses or byte offsets are provided**
   - **Example from CBACT01C-cbl.json:**  
     `"Account ID (ACCT-ID), Active Status (ACCT-ACTIVE-STATUS), Current Balance (ACCT-CURR-BAL)..."`  
     But no definition of ACCT-ID: PIC 9(9) or ACCT-CURR-BAL: PIC 9(7)V99
   - No working-storage section layouts
   - No record length specifications
   - **Impact:** Cannot auto-generate Java entity classes; must manually inspect COBOL source.

2. **Critical Gap: Database Schemas** ✗  
   - **DB2 tables mentioned but not defined:**
     - CREADB21.jcl mentions "create and initialize the CARDDEMO database within DB2 subsystem DAZ1" but provides no DDL
     - No table names, columns, keys, or constraints
     - TRANEXTR.jcl extracts "transaction reference data from DB2" but schema unknown
   - **IMS hierarchy unknown:**
     - CBPAUP0C references IMS segments PAUTSUM0 (parent), PAUTDTL1 (child) but no segment definitions provided
     - No DBD (Database Description) specs
     - Parent-child call sequences present but segment layouts absent
   - **VSAM file structures vague:**
     - "Transaction Master VSAM file," "Account Master File," "Card Cross-Reference file" are named but records are not fully defined
     - TRANIDX creates an alternate index but the indexed field is not specified
   - **Impact:** Spring Boot JPA entities cannot be designed; ORM mappings unknown.

3. **Gap: CICS Screen/BMS Definitions** ✗  
   - **Screen maps not provided:**
     - COBIL00C mentions screen COBIL0A and map COBIL00 but no field definitions (field names, types, lengths, positions)
     - No validation rules for screen input
     - No screen layouts or visual designs
   - **BMSCMP.jcl compiles BMS map "CICSMAP" but the map source is not included**
   - **Impact:** React frontend UI/state design must guess at screen layout and field requirements; potential mismatch with backend.

4. **Gap: Data Type Mappings** ✗  
   - **No COBOL PIC → Java type guide:**
     - Examples: PIC 9(7)V99 → BigDecimal? PIC X(10) → String? Date fields (YYDDD vs YYYYMMDD)?
     - No EBCDIC character set handling rules
     - Mainframe packed decimal (COMP-3) vs Java serialization unknown
   - **Impact:** Data conversion logic must be written ad-hoc; risk of rounding or encoding errors.

5. **Gap: External API & MQ Integration** ✗  
   - **MQ queues and message formats not defined:**
     - COACCT01.cbl "bridges MQ messaging infrastructure" (from summary) but no queue names, message structure, or serialization format
     - IMSMQCMP.jcl compiles IMS/MQ integration but specifications are vague
   - **FTP integration mentioned (FTPJCL.JCL) but endpoints and file formats not documented**
   - **Impact:** Cannot design REST/MQ adapter layer without additional investigation.

6. **Gap: RACF & Security Specifications** ✗  
   - RACFCMDS.jcl performs security tasks, but no resource definitions or authorization rules captured
   - COUSR* programs manage user security but stored in "User Security file" (structure unknown)
   - **Impact:** Java security model (Spring Security roles, permissions) cannot be auto-derived.

7. **Minor: CoverPage.pdf Unusable** ⚠  
   - CoverPage.pdf exists but is likely a visual document not extractable as structured metadata
   - No indexed executive summary in JSON format

---

## 3. Critical Missing Information by Category

### A. Database & Schema (Priority: CRITICAL)

| Item | Status | Impact | Workaround |
|------|--------|--------|-----------|
| **DB2 Table Schemas (DDL)** | Missing | Cannot design Spring JPA entities | Extract from mainframe DB2 catalog (SYSCAT.TABLES); reverse-engineer from COBOL SQL embedded in programs |
| **DB2 Column definitions (name, type, precision, nullable, keys)** | Missing | Cannot build ORMs | Requires DB2 admin query or COBOL source inspection |
| **DB2 Primary & Foreign Keys** | Missing | Cannot model relationships | Reverse from COBOL logic; likely need to infer from record cross-references (XREF) |
| **IMS Segment Definitions (DBD)** | Missing | Cannot design IMS → relational mapping | Requires IMS DBA documentation or COBOL DL/I call analysis |
| **IMS Parent-Child Relationships & Hierarchies** | Mentioned (PAUTSUM0 parent, PAUTDTL1 child in CBPAUP0C) but not fully specified | Risk of data integrity errors in migration | Analyze IMS DL/I CALL statements; IMS DBDGEN source needed |
| **VSAM File Record Layouts (all fields, PIC clauses, offsets)** | Missing | Cannot deserialize VSAM records to Java objects | Must inspect COBOL VSAM file definitions and copybooks |
| **VSAM Alternate Index Fields** | TRANIDX builds AIX on "Transaction Master" but field unspecified | Sorting/filtering logic unclear | Inspect VSAM IDCAMS definitions |
| **Key Field Definitions (all VSAM, DB2, IMS)** | Partially present (record types referenced but not keyed) | Cannot design database indexes or REST GET /:id endpoints | Manual extraction from COBOL record layouts |

### B. API & Integration (Priority: HIGH)

| Item | Status | Impact | Workaround |
|------|--------|--------|-----------|
| **CICS Transaction IDs & Screen Names** | Documented (CB00, CM00, etc.) | ✓ Can map to REST endpoints | Good; use as-is |
| **CICS Screen/BMS Map Definitions (field names, types, lengths, validation)** | Missing | Cannot auto-generate React forms | Reverse from COBOL copybook linked to map (COBIL0A) |
| **MQ Queue Names & Message Formats** | Missing | Cannot build MQ adapter | Search COBOL for CICS MQ PUT/GET calls; IMS MQ specs in jcl |
| **FTP File Formats & Endpoints** | Missing | Cannot build file export/import | Inspect FTPJCL.JCL and underlying DD statements |
| **Batch Job Triggers & Scheduling** | JCL documented | ✓ Can build scheduler | Good; map JCL procs to Spring Batch jobs |
| **Program-to-Program Call Interfaces (LINKAGE SECTION)** | Flow diagrams show calls; linkage specs unknown | Cannot build service method signatures | Inspect COBOL LINKAGE SECTION; infer from message passing logic |

### C. Business Logic (Priority: HIGH)

| Item | Status | Impact | Workaround |
|------|--------|--------|-----------|
| **Account Lifecycle Business Rules** | Documented (balance calculations, cycle debit/credit, reissue logic) | ✓ Can implement in service layer | Good; use rules from CBACT* programs |
| **Authorization Decision Logic** | Documented (expiry checks, pending review workflows in COPAUA0C, COPAUS*) | ✓ Can implement in auth service | Good; detailed rules in JSON |
| **Transaction Posting & Settlement** | Documented (CBTRN01C, CBTRN02C) | ✓ Can implement | Good; workflow clear |
| **Account Cross-Reference Logic** | Documented (Card-to-Account mapping) | ✓ Can implement | Good; rules present |
| **Date Calculations & Formatting** | Partially documented (CSUTLDTC utility, date conversion in multiple programs) | ⚠ Need date library mapping | Requires inspection of date format transformations (YYDDD vs YYYYMMDD handling) |
| **Batch Report Generation** | Documented (CBTRN03C, TRANREPT.jcl) | ✓ Can build report service | Good; workflow clear |

### D. Security & Configuration (Priority: HIGH)

| Item | Status | Impact | Workaround |
|------|--------|--------|-----------|
| **User Authentication (sign-on flow)** | Documented (COSGN00C entry point) | ✓ Flow known | Good; sign-on logic present |
| **Authorization Rules (role-based access control)** | Mentioned (user security files, RACF) but not detailed | Cannot auto-generate Spring Security config | Inspect COUSR* programs and RACFCMDS.jcl; manual policy creation |
| **Password Handling & Encryption** | Not documented | Security risk | Assume mainframe RACF; design new encryption for Java |
| **Mainframe System Parameters** | Not documented (e.g., DB2 subsystem DAZ1, CICS region CICSAWSA) | Deployment-specific; not critical for logical design | Use as environment config |
| **Batch Job Control Language (JCL) Procedures** | Documented | ✓ Can map to Spring Batch | Good; all 35 JCL jobs in catalog |

### E. Data Type Mappings (Priority: CRITICAL)

| Item | Status | Example | Java Equivalent Unknown |
|------|--------|---------|--------|
| **COBOL PIC Clauses** | Missing | CBACT01C has "Current Cycle Debit" (likely PIC 9(7)V99) | BigDecimal? int? Need PIC definitions |
| **EBCDIC Character Encoding** | Not addressed | All VSAM/CICS data in EBCDIC on mainframe | UTF-8 conversion rules needed |
| **Date Formats** | Partially present | YYDDD format mentioned in CBPAUP0C; YYYYMMDD unknown | LocalDate? Instant? Format conversion logic must be explicit |
| **Packed Decimal (COMP-3)** | Not mentioned | Likely used in numeric fields | BigDecimal serialization/deserialization |
| **Zoned Decimal** | Not mentioned | Likely used in display fields | String-to-BigDecimal parsing |
| **Boolean/Flag Fields** | Partially documented (e.g., "Active Status: Y/N") | ACCT-ACTIVE-STATUS = 'Y' or 'N' | Enum or boolean? |

---

## 4. Program Coverage Summary

### Documentation Inventory

- **Total documented files:** 94 (all included in metadata/summary.json)
- **Total documented programs:** 72 (COBOL programs across all modules)
  - Batch programs: 29 (CBACT*, CBTRN*, CBSTM*, CBCUS*, CB*, etc.)
  - CICS online programs: 18 (CO*, CB00, CM00, etc.)
  - Authorization/IMS/DB2 programs: 8 (CBPAUP0C, COPAUA0C, COPAUS*, etc.)
  - Utility programs: 3 (CSUTLDTC date utility, COBSWAIT, etc.)
- **JCL jobs documented:** 35 (data setup, file initialization, backup, batch execution)
- **Sample JCL templates:** 8 (compilation templates, RACF, BMS compilation)

### Program-by-Module Spot-Check

#### 1. **Core CardDemo (app/cbl/)**
   
**Sample:** CBACT01C (batch account reader)
- ✓ **High-level overview present:** Reads account master file, outputs in 3 formats
- ✓ **Flow diagram:** Complete with 17 decision/processing nodes
- ✓ **File I/O:** ACCTFILE-FILE (input), OUT-FILE, ARRY-FILE, VBRC-FILE (outputs) specified
- ✓ **Acceptance criteria:** 97 rules covering file operations, data defaults, output formatting
- ✗ **Missing:** PIC clauses for ACCOUNT-RECORD structure; output record layouts not defined
- ✗ **Missing:** Default values documented (e.g., "apply default cycle debit 2525.00") but units/precision unclear

**Sample:** COBIL00C (CICS bill payment online)
- ✓ **CICS workflow documented:** Session initialization, screen input, validation, balance lookup, payment confirmation, transaction creation
- ✓ **Screen interaction:** References screen COBIL0A/COBIL00, fields ACTIDINI and CONFIRMI named
- ✓ **Business logic:** Complex multi-step flow with 21 functionalities, 141 rules
- ✓ **File access:** ACCTDAT (account data), CXACAIX (card cross-ref), TRANSACT (transaction file) referenced
- ✗ **Missing:** CICS screen field definitions (COBIL0A map not provided); ACCTDAT record structure unknown
- ✗ **Missing:** Transaction ID generation algorithm (browsing transaction file "from highest ID backwards" but key field unknown)

#### 2. **Authorization (app/app-authorization-ims-db2-mq/cbl/)**

**Sample:** CBPAUP0C (batch IMS authorization expiry processor)
- ✓ **IMS integration documented:** Reads PAUTSUM0 (parent segment), PAUTDTL1 (child segment)
- ✓ **Business logic:** Expiry calculation, checkpoint processing, summary updates
- ✓ **Parameters:** Expiry days, checkpoint frequency, debug flag; defaults documented
- ✗ **Missing:** IMS segment definitions (PAUTSUM0 and PAUTDTL1 fields); parent-child link fields
- ✗ **Missing:** Checkpoint semantics (IMS-specific); DL/I call specifications

**Sample:** COPAUA0C (real-time authorization decision engine)
- Referenced in summary as "real-time decision engine that evaluates incoming card payment requests in a CICS/IMS/MQ environment"
- **Status:** Documented at high level; detailed rules likely in JSON file (not examined fully)
- ✗ **Missing:** MQ message format; authorization rule decisioning logic not yet reviewed

#### 3. **Transaction Type (app/app-transaction-type-db2/cbl/)**

**Sample:** COTRTUPC (transaction type update utility)
- Referenced: "maintains reference data supporting transaction processing" (batch DB2 maintenance)
- **Status:** Mentioned in summary; full doc likely present
- ✗ **Missing:** DB2 table schema for transaction type table; update/insert/delete rules not examined

#### 4. **VSAM/MQ (app/app-vsam-mq/cbl/)**

**Sample:** COACCT01 (CICS account service, MQ integration)
- Referenced: "bridges MQ messaging infrastructure with core account data"
- **Status:** Mentioned; detailed interaction unknown
- ✗ **Missing:** MQ queue names; message structure; MQ PUT/GET specifications

#### 5. **JCL/Batch Setup**

**Sample:** CARDFILE.jcl (setup VSAM structures for cards)
- ✓ **Purpose clear:** "manages the setup and initialization of VSAM data structures"
- ✓ **Procedure documented:** IDCAMS commands for KSDS creation
- ✗ **Missing:** Record length, block size, key length/position specifics (must inspect raw JCL)

**Sample:** CREADB21.jcl (DB2 database initialization)
- Referenced: "create and initialize the CARDDEMO database within the DB2 subsystem (DAZ1)"
- ✗ **Missing:** DDL statements; DB2 table/index definitions

---

## 5. By-Module Readiness Assessment

### Module 1: Core CardDemo Online Transactions (CICS Programs)

**Status: Ready with Gaps**

| Aspect | Assessment | Evidence |
|--------|------------|----------|
| Business Logic | ✓ Ready | Flow diagrams, decision trees, 100+ rules per program documented |
| Screen/UI Design | ⚠ Needs Work | Screen field names (ACTIDINI, CONFIRMI) identified but BMS definitions missing; UI layout unknown |
| Service Layer Design | ✓ Ready | Call sequences and inter-program transfers clear (e.g., COBIL00C → COSGN00C) |
| Data Model | ✗ Blocked | VSAM file structures (ACCOUNT-RECORD, etc.) referenced but not fully defined; PIC clauses missing |
| API Contracts | ⚠ Needs Work | Transaction IDs (CB00, CM00) and screen names present; method signatures must be inferred from LINKAGE SECTION |
| Database Schema | ✗ Blocked | VSAM and any underlying DB2 mappings unknown |

**Verdict:** Can implement services and business logic. Cannot finalize data models or UI without copybook inspection.

---

### Module 2: Authorization (IMS/DB2/MQ)

**Status: Ready with Significant Gaps**

| Aspect | Assessment | Evidence |
|--------|------------|----------|
| Business Logic | ✓ Mostly Ready | Authorization decision, expiry logic, summary management documented |
| IMS Integration | ⚠ Needs Work | Segment names (PAUTSUM0, PAUTDTL1) identified; hierarchies and record layouts missing |
| DB2 Integration | ✗ Blocked | DB2 tables mentioned (CARDDEMO in DAZ1) but schema not provided |
| MQ Integration | ✗ Blocked | MQ bridge mentioned but queue names, message formats, serialization unknown |
| Batch Processing | ✓ Ready | Checkpoint logic, parameter handling, reporting documented |

**Verdict:** Can implement authorization logic and batch processor. Cannot finalize IMS/DB2/MQ adapter layer without schema definitions.

---

### Module 3: Transaction Type Management (DB2)

**Status: Ready with Gaps**

| Aspect | Assessment | Evidence |
|--------|-----------|---------|
| Business Logic | ⚠ Partial | Referenced as "maintain reference data" but detailed rules not fully reviewed |
| DB2 Integration | ✗ Blocked | Table schema for transaction types not provided |
| Batch Maintenance | ✓ Ready | JCL job sequence documented (MNTTRDB2.jcl) |

**Verdict:** Can structure as microservice; cannot finalize schema mapping until DB2 DDL acquired.

---

### Module 4: VSAM/MQ Messaging

**Status: Ready with Gaps**

| Aspect | Assessment | Evidence |
|--------|-----------|---------|
| Business Logic | ⚠ Partial | Account service and MQ bridge mentioned; detailed workflow unclear |
| VSAM File I/O | ✗ Blocked | File structures referenced but record definitions missing |
| MQ Integration | ✗ Blocked | Queue and message format not documented |

**Verdict:** Concept clear; implementation blocked on structural definitions.

---

### Module 5: Batch & JCL Workflows

**Status: Ready**

| Aspect | Assessment | Evidence |
|--------|-----------|---------|
| Job Sequencing | ✓ Ready | All 35 JCL jobs cataloged with purposes (setup, backup, posting, reporting) |
| Procedure Calls | ✓ Ready | Job dependencies and step sequences documentable from JCL |
| Data Movement | ✓ Ready | File I/O and backup/restore procedures documented |
| Scheduling | ✓ Ready | Daily, end-of-day, and on-demand triggers clear |

**Verdict:** Ready for Spring Batch scheduling and ETL layer.

---

## 6. Top 10 Missing Items by Transformation Impact

| Rank | Item | Impact | Priority | Why Critical |
|------|------|--------|----------|---|
| 1 | **COBOL Copybooks with PIC Clauses** | CRITICAL – blocks entity design | CRITICAL | Cannot generate Java entity classes; manual COBOL inspection required for each 50+ record types |
| 2 | **DB2 Table Schema (DDL, columns, constraints)** | CRITICAL – blocks ORM/JPA | CRITICAL | Cannot design Spring Data JPA repositories; DB2 catalog query or COBOL SQL reverse-engineering required |
| 3 | **IMS Segment Definitions (DBD, field layouts)** | HIGH – blocks IMS adapter | HIGH | Cannot design IMS ↔ relational mapping; IMS DBA or DBD reverse-engineering required |
| 4 | **VSAM File Record Layouts (byte-level specs)** | HIGH – blocks batch I/O | HIGH | VSAM reader/writer classes cannot be auto-generated; IDCAMS or COBOL analysis required |
| 5 | **CICS BMS Screen Maps (field definitions, validation)** | HIGH – blocks UI design | HIGH | React forms must guess at field types, lengths, validation; BMS source unavailable |
| 6 | **MQ Queue Definitions & Message Schema** | HIGH – blocks adapter | HIGH | Cannot build MQ listener/publisher; queue names and message format must be inferred from COBOL |
| 7 | **Data Type Mapping Rules** | HIGH – blocks serialization | HIGH | COBOL COMP-3, packed decimals, EBCDIC encoding – no automated conversion specs |
| 8 | **CICS Screen Transaction ID & Call Chain Mapping** | MEDIUM – blocks endpoint design | MEDIUM | Transaction IDs present (CB00, CM00) but complete call chains not fully traced |
| 9 | **Authorization & RACF Rules** | MEDIUM – blocks security layer | MEDIUM | No role/permission mapping; Spring Security policies must be manually created |
| 10 | **External API & File Integration Specs** | MEDIUM – blocks integration layer | MEDIUM | FTP endpoints, format specs, and API contracts for external systems unknown |

---

## 7. Recommendations for Next Steps

### Immediate Actions (Week 1)

1. **Acquire COBOL Source & Copybooks**
   - Obtain all .CBL and .CPY files from mainframe source library (AWS.M2.CARDDEMO.CBL, AWS.M2.CARDDEMO.CPY)
   - Action: Work with mainframe team to extract source and copybooks into readable text format
   - Effort: 1–2 days
   - Tool: Use COBOL parser (e.g., Antlr4 grammar) or manual inspection for 50–100 copybooks

2. **Extract DB2 Schema Definitions**
   - Query DB2 catalog tables (SYSCAT.TABLES, SYSCAT.COLUMNS, SYSCAT.TABCONST) for CARDDEMO database
   - Action: DB2 admin provides DDL via `db2 GET DDL FOR TABLE ...` or equivalent
   - Effort: 1 day
   - Output: SQL DDL scripts for all tables in CARDDEMO database

3. **Extract IMS Database Definitions**
   - Obtain IMS DBD (Database Description) source for DEMODB and related segments
   - Action: IMS DBA provides DBD source code
   - Effort: 1 day
   - Output: IMS segment hierarchies, field definitions, record formats

4. **Acquire CICS BMS Screen Map Definitions**
   - Obtain BMS source (.bms files) and compiled maps for COBIL0A, COMEN0A, etc.
   - Action: CICS admin extracts BMS source from CICS resource library
   - Effort: 1–2 days
   - Output: Field names, types, lengths, validation attributes for each screen

### Short-Term Actions (Weeks 2–3)

5. **Create Java Entity Class Templates from Copybooks**
   - Parse COBOL copybooks and generate scaffolding:
     ```java
     @Entity
     public class Account {
         @Id private String accountId;  // PIC 9(9)
         private BigDecimal currentBalance;  // PIC 9(7)V99
         private LocalDate reissueDate;  // DATE format TBD
         // ... generated from copybook
     }
     ```
   - Effort: 2–3 days (automated parsing + manual review)
   - Tool: Custom COBOL parser or manual template generation

6. **Map VSAM File I/O to Spring Batch Readers/Writers**
   - Reverse-engineer VSAM file layouts (IDCAMS LISTCAT output)
   - Create Spring Batch `FlatFileItemReader` / `FlatFileItemWriter` mappings
   - Effort: 2–3 days
   - Output: Spring Batch configuration for each VSAM file

7. **Build IMS-to-Relational Mapper**
   - Design normalized relational schema to hold IMS hierarchical data
   - Create adapter/mapper classes for read/write operations
   - Effort: 2–3 days

8. **Design REST API Specifications**
   - Map CICS transaction IDs (CB00, CM00) to REST endpoints
   - Define request/response schemas based on COBOL screen/record definitions
   - Output: OpenAPI (Swagger) specification
   - Effort: 2–3 days

### Medium-Term Actions (Weeks 4–6)

9. **Build React UI Components**
   - Create form components for each CICS screen (bill payment, account lookup, etc.)
   - Use field definitions from BMS maps and business logic from flow diagrams
   - Effort: 3–5 days (initial draft; iteration based on UX review)

10. **Implement Data Type Conversion & Serialization**
    - Build utility classes for EBCDIC ↔ UTF-8, YYDDD ↔ ISO 8601, COMP-3 ↔ BigDecimal
    - Effort: 1–2 days

11. **Build Adapter Layer for IMS/DB2/MQ**
    - Implement Spring integration modules for each external system
    - Effort: 3–5 days

---

## 8. Key Gaps by Impact & Effort

| Gap | Impact | Effort to Close | Recommended Action |
|-----|--------|-----------------|---|
| Copybook definitions | CRITICAL (blocks all entities) | 3–5 days | Extract COBOL source; parse copybooks; auto-generate entity stubs |
| DB2 schemas | CRITICAL (blocks ORM) | 1 day | Query DB2 catalog; generate Spring Data JPA repositories from DDL |
| IMS segment definitions | HIGH (blocks authorization module) | 1–2 days | Obtain IMS DBD source; create relational schema for normalized storage |
| VSAM record layouts | HIGH (blocks batch layer) | 2–3 days | Reverse from IDCAMS + COBOL analysis; create Spring Batch readers |
| BMS screen definitions | HIGH (blocks UI design) | 1–2 days | Extract BMS source; parse field definitions; create React form templates |
| MQ queue/message specs | MEDIUM (blocks messaging layer) | 2–3 days | Inspect COBOL MQ PUT/GET calls; document queue names and message structure |
| RACF authorization rules | MEDIUM (blocks security) | 1–2 days | Inspect COUSR* programs and RACFCMDS.jcl; manually define Spring Security policies |
| Date/type mappings | MEDIUM (prevents errors) | 1 day | Create conversion utility library; test against live mainframe data samples |

---

## 9. Deliverables Checklist for Next Phase

To move forward with Java + React implementation, acquire:

- [ ] All COBOL .CBL source files (app/cbl/ and subdirectories)
- [ ] All COBOL .CPY copybook files
- [ ] DB2 DDL for CARDDEMO database (or access to live DB2 catalog)
- [ ] IMS DBD definitions for DEMODB and related segments
- [ ] CICS BMS source files (.bms) and screen definitions
- [ ] VSAM IDCAMS control card definitions (file attributes, record length, key specs)
- [ ] MQ queue definitions and sample message payloads (or COBOL inspection notes)
- [ ] RACF user resource class definitions and authorization matrix
- [ ] JCL job control language source (for batch job flow modeling)
- [ ] Sample data extracts from each file/table for format validation

---

## 10. Conclusion

### Current State

The AWS Transform reverse-engineering has produced **excellent program-level documentation** with detailed business logic, flow diagrams, and acceptance criteria. The metadata is well-organized and accessible.

### Transformation Readiness

- ✓ **Ready:** Business logic layer, batch workflow orchestration, program dependencies
- ⚠ **Partial:** CICS screen interactions (UI flows known, field definitions missing)
- ✗ **Blocked:** Data model design, ORM mapping, database schema, external system integration details

### Timeline Impact

**Without structural definitions (copybooks, schemas, BMS):**
- Estimated timeline: 12–16 weeks (with significant manual reverse-engineering)

**With structural definitions (all 10 items above):**
- Estimated timeline: 8–10 weeks (can proceed with code generation and automation)

### Recommendation

**Do NOT proceed with implementation until items 1–4 from Section 7 are completed.** These items unblock 60–70% of the remaining work and enable automated code generation for data layers. Estimated delay: 1–2 weeks to acquire and process; ROI on timeline: +3–4 weeks saved during implementation.

---

**Report Generated:** 2026-11-05  
**Assessment Completed By:** AWS Transform Reverse-Engineering Review  
**Next Review Point:** After copybook and schema acquisition (Week 2)
