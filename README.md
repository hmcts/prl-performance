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
---------------------------------------------------
** Before running any datagen scenarios, ensure the following code has been uncommented from within the protocol defintion:
.inferHtmlResources(AllowList(), DenyList()) *** ONLY TO BE USED FOR DATA PREP ****

To generate data for CUI as a respondent/applicant cases need to be created and progressed to a specific state. The process for this has been broken down into two different scenarios:
1. PRLFL401CreateProgressCase - Creates FL401 cases as a Solicitor within Manage Case XUI, Progresses this case as a caseworker and a court admin, requests and lists 2 x hearings --> Writes to output files: FL401caseNumberAndCodeApplicant.csv, FL401caseNumberAndCodeRespondent.csv
2. PRLC100CreateProgressCase - Creates C100 cases cases as a Solicitor within Manage Case XUI, Progresses this case as a caseworker and a court admin, requests and lists 2 x hearings --> Writes to output files: C100caseNumberAndCodeApplicant.csv, C100caseNumberAndCodeRespondent.csv

The datagen scripts progress the cases to service of application and then lists hearings to ensure maximum coverage within the citizen user interface. These scenarios have been setup to create enough data for three peak load performance tests and will take 1.5-2.5 hours to run. 

The generated case data can then be copied into the correct CSV files to be fed into the relevant scripts:

FL401caseNumberAndCodeApplicant --> FL401ApplicantDashData.csv For the FL401ApplicantDashboard script/scenario
FL401caseNumberAndCodeRespondent --> FL401RespondentData.csv For the FL401Respondent script/scenario
C100caseNumberAndCodeApplicant --> C100ApplicantDashData.csv For the C100ApplicantDashboard script/scenario
C100caseNumberAndCodeRespondent --> C100RespondentData.csv For the FL401Respondent script/scenario


Data Preparation for CUIRA Journeys
----------------------------------------------------
For the CUIRA (add) journey cases need to be responded to within the CUI. The above data prep needs to be completed then the cases responded to via the Citizen_PRL_C100_Respondent and Citizen_PRL_FL401_Respondent journeys. These two scripts output to the following files:
AddRAData.csv 

To generate data for the modifyRA journey the data needs to be run through the PRLReasonableAdjustmentsAdd scenario. This writes data to:
ModifyRAData.csv

Test User Cleanup
----------------------------------------------------
Before or after any performance test scenario, run the userCleaner scenario to clear all created test users from previous tests 