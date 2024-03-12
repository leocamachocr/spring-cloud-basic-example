# Base Rest

- Register
````shell
curl -X POST \
-H "Content-Type: application/json" \
-d '{"name":"test","email":"test@email.com","password":"1234ABC"}' \
http://localhost:8080/api/public/auth/register
````

- Login
````shell
curl -X POST \
-H "Content-Type: application/json" \
-d '{"email":"test@email.com","password":"1234ABC"}' \
http://localhost:8080/api/public/auth/login
````