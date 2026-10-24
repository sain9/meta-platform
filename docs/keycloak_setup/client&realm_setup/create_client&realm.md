4#create a realm
Login to Keycloak.
In the upper-left corner, click the realm selector (it will show Master by default).
Click Create realm.
Enter:
Realm name:
platform
Click Create

#create a client
Create the client with:

Client ID: platform-gateway
OpenID Connect
Client authentication: ON
Standard Flow: ON
Direct Access Grants: ON
Service Accounts: ON
-----------------------------------------------------------------
Go to:

Clients

Click:

Create client
General Settings

Client Type:

OpenID Connect

Client ID:

platform-gateway

Name:

Platform Gateway

Description:

Gateway authentication client

Click Next.

-----------------------------------------------------------------
Login Settings

For now, since we don't have a frontend, set:

Root URL:

http://localhost:8080

Home URL:

http://localhost:8080

Valid Redirect URIs:

*

Valid Post Logout Redirect URIs:

*

Web Origins:

*

Note: Using * is acceptable only during development. Before production, we'll replace these with your actual frontend URLs.
------------------------------------------------------------------------
#create Realm Roles

Create these four realm roles:

ADMIN
DEVELOPER
USER
VIEWER
Meaning
ADMIN

Can do everything.

DEVELOPER

Can:

Create schemas
Modify schemas
Test APIs

Cannot manage users.

USER

Regular application user.

Can access allowed business functionality.

VIEWER

Read-only access.

Perfect for auditors or reporting.

Create the Roles
In the left menu, click Realm roles (or Roles, depending on your UI).
Click Create role.
Create these one by one:
ADMIN
DEVELOPER
USER
VIEWER

You don't need to add descriptions right now.

Verify

You should end up with something like:

Realm Roles

✓ ADMIN
✓ DEVELOPER
✓ USER
✓ VIEWER

note: A common pattern is:

Role = Who the user is (ADMIN, USER, VIEWER)
Permission = What the user can do (CREATE_SCHEMA, DELETE_SCHEMA)
----------------------------------------------------------------------------
# create user
Fill the form like this
Field	Value
Username	hussain
Email	hussain@example.com (or your email)
{note: dont forget to include email, let it be dummy email but include it , its ok not to verify but just add one email}
First name	Mohammad
Last name	Hussain
Email verified	Leave OFF
Required user actions	Leave empty
Groups	None

Then click Create.

After the user is created

Open the user and follow these steps:

1. Set a password

Go to:

Credentials

Click:

Set password

Use: hussain
(Username + Password = hussain)

Make sure Temporary = OFF, then save.

2. Assign the ADMIN role

Go to:

Role mapping

Click:

Assign role

Select Filter by realm roles

Select:

ADMIN (the real role we created above)

Click Assign.