1- build data-api-proto
2- build data-api           (uses same above data-api-proto jar)
3- build data-api-client    (uses same above data-api-proto jar)

## check test/java/com.hussain.data_api_client

ClientSpringContextTest
ClientSmokeTest

set entity and columns that are actually present in db
in either of the above test files

then run:

> mvn clean test -Dtest=ClientSpringContextTest
                  or
> mvn test -Dtest=ClientSmokeTest

should see
Service/Execute OK in 212ms
AUTO-CONFIG OK: {id=d14cfb36-0bfe-4f1e-84d0-b32e37f4147d, entity_definition= ...
