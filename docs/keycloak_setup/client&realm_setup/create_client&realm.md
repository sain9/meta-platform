## create a realm

## then create a client as below

✅ Client authentication → On

✅ Authorization → Off ← correct choice

✅ Standard flow → On (default, harmless)

✅ Direct access grants → On (lets you test with password grant if needed)

✅ Service accounts roles → On ← the critical one, and it's already checked



## copy client secret


## assign service acount roles
	 client > 	service account roles >  assign

> Assign the following roles
query-users
query-groups
view-users
view-realm
create-client
manage-users
manage-realm
S
-- Then you are done for user-access api --