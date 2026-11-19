# CardDemo Extension - Pending Authorization

- [Description](#description)
- [Technologies used](#technologies-used-in-pending-authorization-extension)
- [Application Details](#application-details)
- [Application Diagram](#application-diagram)
  - [Business Logic](#business-logic)
- [Installation Guide on Mainframe](#installation-guide-on-mainframe)
  - [Pre-requisite](#pre-requisite)
  - [IMS Database](#ims-database)
  - [DB2 Table](#db2-table)
  - [Compile, Link and Bind Programs](#compile-link-and-bind-programs)
  - [CICS Resources](#cics-resources)
- [Application Components](#application-components)
  - [Programs](#programs)
  - [Copybooks](#copybooks)
  - [CICS Transactions](#cics-transactions)
  - [CICS Screens](#cics-screens)
  - [JCL](#jcl)
  - [IMS DB](#ims-db)
    - [DBD](#dbd)
    - [PSB](#psb)
  - [DB2](#db2)
  - [Sample MQ Name](#sample-mq-name)
- [Data Models](#data-models)
  - [Input Request MQ Message](#input-request-mq-message)
  - [Output Response MQ Message](#output-response-mq-message)
  - [IMS DB](#ims-db-data-model)
  - [DB2](#db2-data-model)
- [Application Screens](#application-screens)
  - [Authorization Summary](#authorization-summary)
  - [Authorization Details](#authorization-details)
  - [Mark Authorization Fraud](#mark-authorization-fraud)
- [License](#license)


## Description:
Pending Authorization is an extension of the original CardDemo functionality to add new integrations and complexities. This includes addition of new database and new Cobol data-types.


> __New Integrations added:__
> 1. COBOL CICS program with ***MQ*** trigger, ***IMS DB*** read and write and VSAM
> 2. ***Two-phase*** commit with ***IMS DB*** and ***DB2***
> 3. COBOL CICS green screen program with ***IMS DB*** and VSAM
> 3. COBOL Batch with ***IMS DB*** operations
>
> __Additional Complexities:__
> 1. Complex data-type like ***COMP***, ***COMP-3*** and ***REDEFINES***
> 2. COBOL Functions like ***NUMVAL***
> 3. ***Search ALL*** COBOL functionality
> 4. IMS DB key with packed-decimal fields
> 5. DB2 with data-types like Timestamp, Date, Decimal, SMALLINT, VARCHAR, CHAR

<br/>

## Technologies used in Pending Authorization extension:
1. COBOL
2. CICS
3. IMS DB
4. MQ
5. DB2
6. VSAM


## Application Details:
Pending Authorization of Credit Card is part of the payment processing system. When a Credit customer use the credit card to make a purchase either via point-of-sale, phone or online a request is sent to the Issuer bank to authorize the request. 

High-level the authorization request is initiated from merchant end which first reaches the Acquirer bank, then to Payment Network like VISA/MC and the forwarded to the Issuer back. Issuer bank responds with Approval or Decline.

In this section the Pending Authorization functionality is used to demonstrate the new Integrations of CardDemo.

<br/>

## Application Diagram:
![Alt text](./../diagrams/auth_flow.png?raw=true "Application Flow")

***Please note:*** *The Cloud client component is NOT included with the example code. We used API and Lambda to write/read the message to/from the MQ on mainframe. However, any client for your choice can be used. Please make sure the Input Request and Output Request MQ messages match the comma-seperated layout as mentioned below later, else modify the program COPAUA0C to match the layouts.*

### Business Logic:
    1. Authorization Driver [A.0]
        1. A Cloud based Credit Card Point-Of-Sale emulator sends authorization request to mainframe via MQ on mainframe
        2. Authorization message in MQ trigges the online application, format of message is comma seperated
        2. The online process retrieves the Account and Customer record for the card using the XREF file
        3. Run through the Business Rule to Approve or Decline the request
        4. Send the response back via reply MQ
        5. Write the details of the record in the IMS Authorization Database

    2. UI for viewing Account and Authorization Summary [S.0]
        1. UI for viewing the Account and Authorization details for an account
        2. Summary screen show the summary of the authorization details

    3. UI for viewing Authorization Details [S.1]
        1. UI for viewing the Authorization details for an account
        2. User can view the next authorization by pressing PF8

    4. Mark Authorization Fraud [S.2]
        1. Updates the IMS child segment to add Fraud Report Date
        2. Copy the Authorization details to a DB2 table to be used for further analytics

    5. Authorization Purge [B.0]
        1. Runs once a day and scans the IMS Authorization database to find records which have passed threshold date
        2. When an unmatched authorization is deleted the current available is adjusted accordingly

<br/>

# Installation Guide on Mainframe:

#### Pre-requisite

The Pending Authorization extension is built on top of the base version of the original CardDemo Application. Please follow the guide [here](./../README.md#installation-on-the-mainframe) to install the base version of CradDemo first.

### IMS database
Creation of IMS database is outside the scope of the documentation. Please consult your system administrator and database administrator on creating the IMS database.

Pending Authorization application at this time is a HIDAM IMS database. Please work with your DBA administrator to install the supplied DBDs and PSBs provided under apps directory to your system.

### DB2 Table
Below sample DB2 scripts can be executed to create the DB2 table and the Index.

*Please consult with your DB2 administrator for the creation of DB2 database, Storage Group and Schema.*

*The DB2 schema name has to be updated in the program COPAUS2C.cbl accordingly*
```shell
    CREATE TABLE <<db2-schema>>.AUTHFRDS                   
    (CARD_NUM              CHAR(16)    NOT NULL,
    AUTH_TS                TIMESTAMP   NOT NULL,
    AUTH_TYPE              CHAR(4)             ,
    CARD_EXPIRY_DATE       CHAR(4)             ,
    MESSAGE_TYPE           CHAR(6)             ,
    MESSAGE_SOURCE         CHAR(6)             ,
    AUTH_ID_CODE           CHAR(6)             ,
    AUTH_RESP_CODE         CHAR(2)             ,
    AUTH_RESP_REASON       CHAR(4)             ,
    PROCESSING_CODE        CHAR(6)             ,
    TRANSACTION_AMT        DECIMAL(12,2)       ,
    APPROVED_AMT           DECIMAL(12,2)       ,
    MERCHANT_CATAGORY_CODE CHAR(4)             ,
    ACQR_COUNTRY_CODE      CHAR(3)             ,
    POS_ENTRY_MODE         SMALLINT            ,
    MERCHANT_ID            CHAR(15)            ,
    MERCHANT_NAME          VARCHAR(22)         ,
    MERCHANT_CITY          CHAR(13)            ,
    MERCHANT_STATE         CHAR(02)            ,
    MERCHANT_ZIP           CHAR(09)            ,
    TRANSACTION_ID         CHAR(15)            ,
    MATCH_STATUS           CHAR(1)             ,
    AUTH_FRAUD             CHAR(1)             ,
    FRAUD_RPT_DATE         DATE                ,
    ACCT_ID                DECIMAL(11)         ,
    CUST_ID                DECIMAL(9)          ,
    PRIMARY KEY(CARD_NUM,AUTH_TS )             )
    IN <<db2-database>>.AWSTSFRD;
``` 
```shell
    CREATE UNIQUE INDEX <<db2-schema>>.XAUTHFRD           
    ON <<db2-schema>>.AUTHFRDS                         
    (CARD_NUM ASC, AUTH_TS DESC)             
    USING STOGROUP <<db2-storage-group>>                     
    COPY YES;
```


### Compile, Link and Bind Programs

The sample Compile JCLs provided can be used to compile the Cobol programs depending on whether is a BATCH or CICS program, and if it has IMS, DB2 or MQ.

### CICS resources

The base CICS resources from CardDemo have to be defined first

* Use the CEDA transaction to execute the below commands

    * Define Mapsets, Maps and Programs

      Sample CEDA commands. The same instructions have to be executed for all CICS transactions, maps and cobol programs.

       ```shell
       DEF PROGRAM(COPAUA0C) GROUP(CARDDEMO)
       DEF MAPSET(COPAU00) GROUP(CARDDEMO)
       DEFINE PROGRAM(COPAUA0C) GROUP(CARDDEMO) DA(ANY) TRANSID(CP00) DESCRIPTION(Authorization Main Module)
       DEFINE TRANSACTION(CP00) GROUP(CARDDEMO) PROGRAM(COPAUA0C) TASKDATAL(ANY)
       ```
    * Define DB2 PLAN and DB2 Transaction

       ```shell
       DEF DB2ENTRY(DB201PLN) GROUP(CARDDEMO)
       DEF DB2TRAN(CPVDTRAN) ENTRY(DB201PLN) TRANSID(CPVD) GROUP(CARDDEMO)
       ```

* Install the online resources to your CICS region

   ```shell
   CEDA INSTALL TRANS(CP00) GROUP(CARDDEMO)
   CEDA INSTALL TRANS(CPVS) GROUP(CARDDEMO)
   CEDA INSTALL TRANS(CPVD) GROUP(CARDDEMO)
   CEDA INSTALL DB2ENTRY(DB201PLN) GROUP(CARDDEMO)
   CEDA INSTALL DB2TRAN(CPVDTRAN) GROUP(CARDDEMO)
   ```

* Execute a NEWCOPY of mapsets and maps
   ```shell
   CEMT SET PROG(COPAUA0C) NEWCOPY
   CEMT SET PROG(COPAU0*) NEWCOPY  
   CEMT SET PROG(COPAUS*C) NEWCOPY 
   ```

# Application Components:

All the artifacts for Pending Authorization functionality is under folder [app/app-authorization-ims-db2-mq](./../app/app-authorization-ims-db2-mq)

#### Programs:
>1. COPAUA0C - [A.0] CICS program to process authorization request, TRAN CP00
>2. COPAUS0C - [S.0] Green Screen program to show Pending Authorization Summary, TRAN - CPVS
>3. COPAUS1C - [S.1] Green Screen program to show Pending Authorization Details, TRAN - CPVD
>4. COPAUS2C - [S.2] Called program to mark or reverse Authorization as Fraud. Update IMS DB and DB2
>5. CBPAUP0C - [B.0] Batch COBOL program to purge Expired Authorizations

#### Copybooks:

>1. CIPAUSMY - Pending Authorization Summary IMS Segment
>2. CIPAUDTY - Pending Authorization Details IMS Segment
>3. CCPAURQY - Copybook for Authorization Request
>4. CCPAURLY - Copybook to Authorization Response
>5. CCPAUERY - Authorization Programs Error Logs

#### CICS Transactions:
>1. CP00 - Process Authorization Requests
>2. CPVS - View Pending Authorization Summary
>3. CPVD - View Pending Authorization Details

#### CICS Screens:
> 1. COPAU00 - BMS screen for Authorization Summary
> 2. COPAU01 - BMS screen for Authorization Details

#### JCL:
>1. CBPAUM0J - Batch Job to purge Expired Authorizations

#### IMS DB:
1. ##### DBD:
    1. DBPAUTP0 - HIDAM              [Naming Convention: DB - DBD Prefix, PAUT - Pending Authorization, P - Primary, # - Sequence]
       1. Segments: PAUTSUM0 - Authorization Summary / PAUTDTL1 - Authorization Details
       2. DD: DDPAUTP0
    2. DBPAUTX0 - HIDAM Index   [Nameing Convention: DB - DBD Prefix, PAUT - Pending Authorization, X - Index, # - Sequence]
       1. Segment: PAUTINDX - Index
       2. DD: DDPAUTX0


2. ##### PSB:
   1. PSBPAUTB - BMP PSB
   2. PSBPAUTL - Load PSB

#### DB2:
1. AUTHFRDS - DCLGEN for FRAUD DB2 table

#### Sample MQ Name:
1. Response MQ - AWS.M2.CARDDEMO.PAUTH.REQUEST
2. Reply MQ        - AWS.M2.CARDDEMO.PAUTH.REPLY


<br/>

## Data Models:
### Input Request MQ Message
Comma separated fields in the following order -  
```
AUTH-DATE  
AUTH-TIME  
CARD-NUM              
AUTH-TYPE             
CARD-EXPIRY-DATE      
MESSAGE-TYPE          
MESSAGE-SOURCE        
PROCESSING-CODE       
TRANSACTION-AMT       
MERCHANT-CATAGORY-CODE  
ACQR-COUNTRY-CODE     
POS-ENTRY-MODE        
MERCHANT-ID           
MERCHANT-NAME         
MERCHANT-CITY         
MERCHANT-STATE        
MERCHANT-ZIP          
TRANSACTION-ID
```

### Output Response MQ Message
Comma separated field in following order -  
```aidl
CARD-NUM        
TRANSACTION-ID  
AUTH-ID-CODE    
AUTH-RESP-CODE  
AUTH-RESP-REASON
APPROVED-AMT  
```
### IMS DB data model
Pending Authorization Root (Summary) and Child (Details) Segment with relation to Account, Customer and Account-Customer XREF

![Alt text](./../diagrams/ims_model.png?raw=true "IMS Data Model")

### DB2 data model
Db2 Table to store authorizatoion records marked fraud.

![Alt text](./../diagrams/db2_model.png?raw=true "DB2 Data Model")

<br/>

## Application Screens:

#### Authorization Summary:
Green Screen to view the Pending Authorization Summary along with other account details. 

The account details are fetched from the account and customer VSAM files along with IMS root segment, and authorization details are fetch from the child IMS segments.
- *Press PF7 and PF8 to scroll through the pending authorization list*
- *Use S and then press Enter to see the details of authorization*

![Alt text](./../diagrams/auth_summary.png?raw=true "Authorization Summary")

#### Authorization Details:
Detail view of the Pending Authorization record.


![Alt text](./../diagrams/auth_details.png?raw=true "Authorization Details")

#### Mark Authorization Fraud:
*Authorization is marked FRAUD by pressing PF5 key.*

![Alt text](./../diagrams/auth_fraud.png?raw=true "Mark Authorization Fraud")


## License:

This is intended to be a community resource and it is released under the Apache 2.0 license.

<br/>