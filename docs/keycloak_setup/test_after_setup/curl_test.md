## once the keycloak container is up in the docker check

curl -X POST "http://localhost:9001/realms/platform/protocol/openid-connect/token" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=client_credentials" \
-d "client_id=platform-gateway" \
-d "client_secret=xyzabc"




## after registering the user

curl --location 'http://localhost:9001/realms/platform/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_id=platform-gateway' \
--data-urlencode 'client_secret=xyzabc' \
--data-urlencode 'username=hussain' \
--data-urlencode 'password=hussain'


