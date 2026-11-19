## CardDemo -- Mainframe CardDemo Application

- [CardDemo -- Mainframe CardDemo Application](#carddemo----mainframe-card-demo-application)
- [Description](#description)
- [Technologies used](#technologies-used)
- [Installation on the mainframe](#installation-on-the-mainframe)
- [Application Details](#application-details)
  - [User Functions](#user-functions)
  - [Admin Functions](#admin-functions)
  - [Application Inventory](#application-inventory)
    - [**Online**](#online)
    - [**Batch**](#batch)
  - [Application Screens](#application-screens)
    - [**Signon Screen**](#signon-screen)
    - [**Main Menu**](#main-menu)
    - [**Admin Menu**](#admin-menu)
- [Support](#support)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Project status](#project-status)

<br/>

## Description
CardDemo is a Mainframe application designed and developed to test and showcase AWS and partner technology for mainframe migration and modernization use-cases such as discovery, migration, modernization, performance test, augmentation, service enablement, service extraction, test creation, test harness, etc.

Note that the intent of this application is to provide mainframe coding scenarios to excercise analysis, transformation and migration tooling. So, the coding style is not uniform across the application


#### ***<span style="color:blue">New Optional Features:</span>***

* We have added the below optional extensions to the base version of CardDemo.  More details can be found in the links below -*

1. Transaction Type management - Optional module demonstrates the integration using DB2
2. [Pending Authorization Extension](./docs/authorization.md) - Optional module demonstrates  IMS, DB2 and integration using MQ

<br/>

## Technologies used

Core Functionality

1. COBOL
2. CICS
3. VSAM
4. JCL
5. RACF
6. ASSEMBLER

Optional Features
1. Db2
2. IMS
3. MQ

<br/>

## Installation on the mainframe 

To install this repository on the mainframe please follow the following steps

1. Clone this repository to your local development environment

2. Create datasets on the mainframe  hold the code
   * It is recommended to group them under a High Level Qualifier (HLQ)for all your datasets. 
   * Upload the following application source folders from the main branch of git repository on to your mainframe
      using $INDFILE or your preferred upload tool.
   * If you have used AWS.M2 as your HLQ, you should end up with the below code structure on the mainframe
   
      | HLQ    | Name          | Format | Length |
      | :----- | :------------ | :----- | -----: |
      | AWS.M2 | CARDDEMO.JCL  | FB     |     80 |
      | AWS.M2 | CARDDEMO.PROC | FB     |     80 |
      | AWS.M2 | CARDDEMO.CBL  | FB     |     80 |
      | AWS.M2 | CARDDEMO.CPY  | FB     |     80 |
      | AWS.M2 | CARDDEMO.BMS  | FB     |     80 |
      | AWS.M2 | CARDDEMO.ASM  | FB     |     80 |
      | AWS.M2 | CARDDEMO.MACLIB| FB     |    80 |
      
3. Use data for testing using either of the below approaches

   ** Use the supplied sample data**
   
      * Upload the sample data provided in the main/-/data/EBCDIC/ folder to the mainframe. Ensure that you use transfer mode binary

         | Dataset name                      | Name                                             | Copybook (Layout) | Format | Length | Name of equivalent ascii file |
         | :---------------------------------| :----------------------------------------------- | :-----            | :----- | -----: | :---------------------------- |
         | AWS.M2.CARDDEMO.USRSEC.PS         | User Security file                               | CSUSR01Y          | FB     |     80 | See DEFUSR01.jcl (inline)     |
         | AWS.M2.CARDDEMO.ACCTDATA.PS       | Account Data                                     | CVACT01Y          | FB     |    300 | acctdata.txt                  |
         | AWS.M2.CARDDEMO.CARDDATA.PS       | Card Data                                        | CVACT02Y          | FB     |    150 | carddata.txt                  |
         | AWS.M2.CARDDEMO.CUSTDATA.PS       | Customer Data                                    | CVCUS01Y          | FB     |    500 | custdata.txt                  |
         | AWS.M2.CARDDEMO.CARDXREF.PS       | Customer Account Card Cross reference            | CVACT03Y          | FB     |     50 | cardxref.txt                  |
         | AWS.M2.CARDDEMO.DALYTRAN.PS.INIT  | Transaction database initialization record       | CVTRA06Y          | FB     |    350 | 1 record (low-values ending with 00000100)|
         | AWS.M2.CARDDEMO.DALYTRAN.PS       | Transaction data which has to go through posting | CVTRA06Y          | FB     |    350 | dailytran.txt                 |
         | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS| Transaction data entered online                  | CVTRA05Y          | FB     |    350 | not applicable                |
         | AWS.M2.CARDDEMO.DISCGRP.PS        | Disclosure Groups                                | CVTRA02Y          | FB     |     50 | discgrp.txt                   |
         | AWS.M2.CARDDEMO.TRANCATG.PS       | Transaction Category Types                       | CVTRA04Y          | FB     |     60 | trancatg.txt                  |
         | AWS.M2.CARDDEMO.TRANTYPE.PS       | Transaction Types                                | CVTRA03Y          | FB     |     60 | trantype.txt                  |
         | AWS.M2.CARDDEMO.TCATBALF.PS       | Transaction Category Balance                     | CVTRA01Y          | FB     |     50 | tcatbal.txt                   |

      * Execute the following JCLs in order
        -  Run optional jobs only if the corresponding optional module is installed (e.g. run DB2 jobs only if DB2 optional module is installed).
            | Jobname  | What it does                                        | Optional module |
            | :------- | :-------------------------------------------------- |:----|
            | DUSRSECJ | Sets up user security vsam file                     |     |
            | CLOSEFIL | Closes files opened by CICS                         |     |
            | ACCTFILE | Loads Account database using sample data            |     |
            | CARDFILE | Loads Card database with credit card sample data    |     |
            | CUSTFILE | Creates customer database                           |     |
            | XREFFILE | Loads Customer Card account cross reference to VSAM |     |
            | CREADB21 | Creates CardDemo Db2 database and loads tables      |  Db2: Transaction Type Mgmt  |
            | TRANFILE | Copies initial Trasaction file  to VSAM             |     |
            | TRANEXTR | Extracts TRAN type and category tables from Db2     |  Db2: Transaction Type Mgmt  |
            | DISCGRP  | Copies initial Disclosure Group file  to VSAM       |     |
            | TCATBALF | Copies initial TCATBALF file  to VSAM               |     |
            | TRANCATG | Copies initial transaction category file  to VSAM   |     |
            | TRANTYPE | Copies initial transaction type file to VSAM        |     |
            | OPENFIL  | Makes files available to CICS                       |     |
            | DEFGDGB  | Defines GDG Base                                    |     |
            | DEFGDGD  | Defines GDG Bases added for Db2                     |     |

      


4. Compile the Programs. 
   
   You should use the compile process followed by your mainframe shopfloor
   
   We have however provided some sample JCLs in the samples folder in git to help you craft the JCL   

5. Create resources in the CARDDEMO group in CICS
   
   You have 2 options
   
   Be sure to edit the HLQs in the below documents as required before you do the definition
   
   * (Preferred) . Use the DFHCSDUP JCL that the resources required by the application

      The resources required are in the CSD file provided in the CSD folder
       
      * Group CARDDEMO
      * Mapsets
      * Transactions
      * Maps
      * Files
      
   * Use the CEDA transaction to execute the commands in the above listing
   
      * Define group 
         ```shell
         DEFINE LIBRARY(COM2DOLL) GROUP(CARDDEMO) DSNAME01(&HLQ..LOADLIB)
         ```
      * Define Mapsets, Maps , Programs and Files
      
         Sample CEDA commands
         
         ```shell
         DEF PROGRAM(COCRDLIC) GROUP(CARDDEMO)
         DEF MAPSET(COCRDLI) GROUP(CARDDEMO)
         DEFINE PROGRAM(COSGN00C) GROUP(CARDDEMO) DA(ANY) TRANSID(CC00) DESCRIPTION(LOGIN)
         DEFINE TRANSACTION(CC00) GROUP(CARDDEMO) PROGRAM(COSGN00C) TASKDATAL(ANY)
         ```

   * Install /Load the online resources to your CICS region

      ```shell
      CEDA INSTALL TRANS(CCLI) GROUP(CARDDEMO)
      CEDA INSTALL FILE(CARDDAT) GROUP(CARDDEMO)
      CECI LOAD PROG(COCRDUP)
      CECI LOAD PROG(COCRDUPC)
      ```

   * Execute a NEWCOPY of mapsets and maps
      ```shell
      CEMT SET PROG(COCRDUP) NEWCOPY
      CEMT SET PROG(COCRDUPC) NEWCOPY  
      ```
6. Enjoy the demo

   * For online functions : Start the CardDemo application using the CC00 transaction
     - Enter userid ADMIN001 and the initially configured password PASSWORD to manage users
     - Enter userid USER0001 and the initially configured password PASSWORD to access back office functions
   * For batch            : See the instructions for running full batch below.

## Running full batch 
   
   * Execute the following JCLs in order
    -  Run jobs having Y in column Db2 only if you are running Db2 version of CardDemo
       | Jobname  | What it does                                                |Optional module?|
       | :------- | :---------------------------------------------------------- |:----|
       | CLOSEFIL | Closes files opened by CICS                                 |     | 
       | ACCTFILE | Loads Account database using sample data                    |     | 
       | CARDFILE | Loads Card database with credit card sample data            |     | 
       | XREFFILE | Loads Customer Card account cross reference to VSAM         |     | 
       | CUSTFILE | Creates customer database                                   |     | 
       | TRANBKP  | Creates Transaction database                                |     | 
       | TRANEXTR | Extracts lastest Db2 data for Transaction type and category |  Db2: Transaction Type Mgmt  | 
       | TRANCATG | Copies latest transaction category file  to VSAM            |     |     
       | TRANTYPE | Copies latest transaction type file to VSAM                 |     | 
       | DISCGRP  | Copies initial disclosure Group file  to VSAM               |     | 
       | TCATBALF | Copies initial TCATBALF file  to VSAM                       |     | 
       | DUSRSECJ | Sets up user security vsam file                             |     | 
       | POSTTRAN | Core processing job                                         |     | 
       | INTCALC  | Run interest calculations                                   |     | 
       | TRANBKP  | Backup Transaction database                                 |     | 
       | COMBTRAN | Combine system transactions with daily ones                 |     | 
       | CREASTMT | Produce transaction statement                               |     |  	
       | TRANIDX  | Define alternate index on transaction file                  |     | 
       | OPENFIL  | Makes files available to CICS                               |     | 
       | WAITSTEP | Defines a step to wait job for given time                   |     |
<br/>

## Application Details 
The CardDemo is a Credit Card management application, built primarily using COBOL programming language. The application has various functions that allows users to manage Account, Credit card, Transaction and Bill payment. 

There are 2 types of users:
* Regular User
* Admin User

The Regular user can perform the user functions and the Admin users can only perform Admin functions.

<br/>

### User Functions

![Alt text](./diagrams/Application-Flow-User.png?raw=true "User Flow")

<br/>

### Admin Functions

![Alt text](./diagrams/Application-Flow-Admin.png?raw=true "Admin Flow")

<br/>

### Application Inventory

#### **Online**

| Transaction |      | BMS Map | Program  | Function                        | Optional Module                    | Remark                                                     |
|:------------|:-----|:--------| :------- |:--------------------------------|:-----------------------------------|:-----------------------------------------------------------|
| CC00        |      | COSGN00 | COSGN00C | Signon Screen                   |                                    |                                                            |
| CM00        |      | COMEN01 | COMEN01C | Main Menu                       |                                    |                                                            |
|             | CAVW | COACTVW | COACTVWC | Account View                    |                                    |                                                            |
|             | CAUP | COACTUP | COACTUPC | Account Update                  |                                    |                                                            |
|             | CCLI | COCRDLI | COCRDLIC | Credit Card List                |                                    |                                                            |
|             | CCDL | COCRDSL | COCRDSLC | Credit Card View                |                                    |                                                            |
|             | CCUP | COCRDUP | COCRDUPC | Credit Card Update              |                                    |                                                            |
|             | CT00 | COTRN00 | COTRN00C | Transaction List                |                                    |                                                            |
|             | CT01 | COTRN01 | COTRN01C | Transaction View                |                                    |                                                            |
|             | CT02 | COTRN02 | COTRN02C | Transaction Add                 |                                    |                                                            |
|             | CR00 | CORPT00 | CORPT00C | Transaction Reports             |                                    |                                                            |
|             | CB00 | COBIL00 | COBIL00C | Bill Payment                    |                                    |                                                            |
|             | CPVS | COPAU00 | COPAUS0C | Pending Authorization Summary   | IMS-DB2-MQ: Pending Authorizations | Read IMS and VSAM                                          |
|             | CPVD | COPAU01 | COPAUS1C | Pending Authorization Details   | IMS-DB2-MQ: Pending Authorizations | Update IMS and Insert DB2                                  |
| CP00        |      |         | COPAUA0C | Process Authorization Requests  | IMS-DB2-MQ: Pending Authorizations | MQ trigger, request and response; Insert and Update to IMS |
| CA00        |      | COADM01 | COADM01C | Admin Menu                      | Db2: Transaction Type Mgmt         |                                                            |
|             | CU00 | COUSR00 | COUSR00C | List Users                      |                                    |                                                            |
|             | CU01 | COUSR01 | COUSR01C | Add User                        |                                    |                                                            |
|             | CU02 | COUSR02 | COUSR02C | Update User                     |                                    |                                                            |
|             | CU03 | COUSR03 | COUSR03C | Delete User                     |                                    |                                                            |
|             | CTTU | COTRTUP | COTRTUPC | Tran Type add/edit              | Db2: Transaction Type Mgmt         | Update and insert on Db2                                   |
|             | CTLI | COTRTLI | COTRTLIC | Tran Type list/update/delete    | Db2: Transaction Type Mgmt         | Demonstrates cursor and delete in Db2                      |


#### **Batch**

| Job      | Program  | Function                                             |Optional Module|
|:---------|:---------|:-----------------------------------------------------|:----|
| DUSRSECJ | IEBGENER | Initial Load of User security file                   |     |
| DEFGDGB  | IDCAMS   | Setup GDG Bases                                      |     |
| DEFGDGD  | IDCAMS   | Setup more GDG Bases for Db2                         |     |
| ACCTFILE | IDCAMS   | Refresh Account Master                               |     |
| CARDFILE | IDCAMS   | Refresh Card Master                                  |     |
| CUSTFILE | IDCAMS   | Refresh Customer Master                              |     |
| CREADB21 | DSNTEP4  | Creates CardDemo Db2 database and loads tables       |  Db2: Transaction Type Mgmt  |
| TRANEXTR | DSNTIAUL | Extracts lastest Db2 data for Transaction type and category |  Db2: Transaction Type Mgmt  | 
| DISCGRP  | IDCAMS   | Load Disclosure Group File                           |     |
| TRANFILE | IDCAMS   | Load Transaction Master file                         |     |
| TRANCATG | IDCAMS   | Load Transaction category types                      |     |
| TRANTYPE | IDCAMS   | Load Transaction type file                           |     |
| XREFFILE | IDCAMS   | Account, Card and Customer cross reference           |     |
| CLOSEFIL | IEFBR14  | Close VSAM files in CICS                             |     |
| TCATBALF | IDCAMS   | Refresh Transaction Category Balance                 |     |
| TRANBKP  | IDCAMS   | Refresh Transaction Master                           |     |
| POSTTRAN | CBTRN02C | Transaction processing job                           |     |
| TRANIDX  | IDCAMS   | Define AIX for transaction file                      |     |
| OPENFIL  | IEFBR14  | Open files in CICS                                   |     |
| INTCALC  | CBACT04C | Run interest calculations                            |     |
| COMBTRAN | SORT     | Combine transaction files                            |     |
| CREASTMT | CBSTM03A | Produce transaction statement                        |     |
| TRANREPT | CBTRN03C | Transaction Report - Submitted from CICS             |     |
| ESDSRRDS | IDCAMS   | To creare ESDS and RRDS VSAM files                   |     |
| CBPAUP0J | CBPAUP0C | Batch COBOL program to purge Expired Authorizations  | IMS-DB2-MQ: Pending Authorizations    |
| MNTTRDB2 | COBTUPDT | Batch COBOL program to maintain Transaction type table  | Db2: Transaction Type Mgmt    |
| WAITSTEP | COBSWAIT | Batch COBOL prorgam to wait the job for given time   |     |



<br/>

### Application Screens

#### **Signon Screen**

![Alt text](./diagrams/Signon-Screen.png?raw=true "Signon Screen")


#### **Main Menu**

![Alt text](./diagrams/Main-Menu.png?raw=true "Main Menu")

***Please Note: Option 11 is only available with version 2 of CardDemo. Please refer [here](./docs/authorization.md)*
#### **Admin Menu**

![Alt text](./diagrams/Admin-Menu.png?raw=true "Admin Menu")

* Options 5 and 6 will be enabled only if you install transactions CTTU and CTLI which are specific to DB2 Transaction Type Mgmt

<br/>

## Support

If you have questions or requests for improvement please raise an issue in the repository.

<br/>

## Roadmap

The following features are planned for upcoming releases

1. Additional database syntax usage scenarios

   1. DB2 Rewards : Will calculate rewards for transactions based on transaction types, categories and rules. Will include stored procedures, functions and dynamic SQL
   
   2. Hierachical database : IMS DC

2. Integration

   * ftp, sftp
   
   * Web Service
   
   * Exposure of transactions for distributed application integration

<br/>

## Contributing

We are looking forward to receiving contributions and enhancements to this initial codebase from the mainframe code base.

Feel free to raise issues, create code and raise merge requests for enhancements so that we can build out this application as a resource for programmers wanting to understand and modernize their mainframes.

<br/>

## License

This is intended to be a community resource and it is released under the Apache 2.0 license.

<br/>

## Project status

We are planning a v2 of this application in Q2 2023.

Watch this space for updates

<br/>