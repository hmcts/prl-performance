### PRL Gatling Performance Tests

This script runs a suite of services against PRL scenario:
- Citizen Case Creation 
- Case Worker Case Creation

To run locally:
- Performance test against the perftest environment: `./gradlew gatlingRun`

Flags:
- Debug (single-user mode): `-Ddebug=on e.g. ./gradlew gatlingRun -Ddebug=on`
- Run against AAT: `Denv=aat e.g. ./gradlew gatlingRun -Denv=aat`

Before running locally, update the client secret in src/gatling/resources/application.conf then run `git update-index --assume-unchanged src/gatling/resources/application.conf` to ensure the changes aren't pushed to github.

To make other configuration changes to the file, first run `git update-index --no-assume-unchanged src/gatling/resources/application.conf`, ensuring to remove the client secret before pushing to origin


Data Preparation for Citizen/Respondent UI Journeys
----------------------------------------------------
To generate data for CUI as a respondent cases need to be created and progressed to a specific state. The process for this has been broken down into different scenarios:
1. PrlFL401Create - Creates FL401 cases as a Solicitor within Manage Case XUI --> Writes to output file: FL401Cases.csv
2. PRLC100CitizenScenario - Creates C100 cases as a Citizen within PRL CUI --> Writes to output file: C100Cases.csv
3. PrlDataPrep - Creates C100 cases as a Solicitor within Manage Case XUI --> Write to output file: cases.csv *16/10/24 - requires rework*

----- Once the cases are generated they they need to be progressed by case worker and case admins within XUI ---------- 
FL401
4. PRLFL401CaseworkerScenario - Progress created FL401 cases as a caseworker within Manage Case XUI --> Reads from FL401CourtAdminData.csv --> Writes to output file(s): FL401caseNumberAndCodeApplicant.csv & FL401caseNumberAndCodeApplicant.csv 
5. PRLFL401CaseManagerScenario - Progress the FL401 cases (which have been run through PRLFL401CaseworkerScenario) to the desired state by a case manager within Manage Case XUI --> Reads from Reads from FL401CourtAdminData.csv --> Writes to output file: FL401caseNumberProgressed

C100
6. PRLC100CaseworkerScenario - Progress created C100 cases as a caseworker within Manage Case XUI --> Reads from C100CourtAdminData.csv --> Writes to output file: C100caseNumberAndCode.csv


Data Preparation for CUIRA Journeys
----------------------------------------------------
For the CUIRA (add) journey cases need to be responded to within the CUI. The above data prep needs to be completed then the cases responded to via the Citizen_PRL_C100_Respondent and Citizen_PRL_FL401_Respondent journeys. These two scripts output to the following files:
AddRAData.csv 

To generate data for the modifyRA journey the data needs to be run through the PRLReasonableAdjustmentsAdd scenario. This writes data to:
ModifyRAData.csv
