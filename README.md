### XUI Gatling Performance Tests

This script runs a suite of services against XUI in a cohabitation scenario:
- General caseworker flows
- Probate solicitor journey
- Divorce solicitor journey
- Financial Remedy solicitor journey
- Family Public Law solicitor journey
- Immigration and Asylum solicitor journey
- No Fault Divorce solicitor journey

To run locally:
- Performance test against the perftest environment: `./gradlew gatlingRun`

Flags:
- Debug (single-user mode): `-Ddebug=on e.g. ./gradlew gatlingRun -Ddebug=on`
- Run against AAT: `Denv=aat e.g. ./gradlew gatlingRun -Denv=aat`

Before running locally, update the client secret in src/gatling/resources/application.conf then run `git update-index --assume-unchanged src/gatling/resources/application.conf` to ensure the changes aren't pushed to github.

To make other configuration changes to the file, first run `git update-index --no-assume-unchanged src/gatling/resources/application.conf`, ensuring to remove the client secret before pushing to origin