## step 1 (up keycloak container)
> cd docs/keycloak_setup/docker

> docker compose up -d

## step 2 (generate client id and client_secret)
> cd docs/keycloak_setup
> ./setup-keycloak.sh

Now copy the client_secret and paste it in the application.properties of user-access-api
------------------------------------------------


else,
if you want to setup manually then follow the setup and test steps provided

