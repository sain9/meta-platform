User Access API Developer Guide
Base URL: http://localhost:8083/api/v1/users


POST /register
Register a new user
{"username":"mike","email":"mike@test.com","firstName":"Mike","lastName":"Ross","password":"Password@123","role":"DEVELOPER"}

POST /login
Login
{"username":"mike","password":"Password@123"}

POST /refresh-token
Refresh access token
{"refreshToken":"<refresh-token>"}

POST /logout
Logout
{"refreshToken":"<refresh-token>"}

POST /change-password
Change password
{"username":"mike","currentPassword":"Password@123","newPassword":"Password@456"}

GET /{username}
Get user
e.g: GET /mike

PUT /{username}
Update user
{"email":"mike@test.com","firstName":"Michael","lastName":"Ross"}

DELETE /{username}
Delete user
e.g: DELETE /mike

PATCH /{username}/enable
Enable user
e.g: PATCH /mike/enable

PATCH /{username}/disable
Disable user
PATCH /mike/disable

POST /{username}/roles
Assign role
{"role":"ADMIN"}

DELETE /{username}/roles
Remove role
{"role":"ADMIN"}

Typical Flow
Register -> Login -> Refresh Token -> Logout -> Login -> Change Password -> Get User -> Update User -> Enable/Disable -> Assign/Remove Role -> Delete User