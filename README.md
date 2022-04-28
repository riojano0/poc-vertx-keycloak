# POC Vertx & Keycloak

POC using vertx, keycloak and event buss service with service proxy

## Docker-Compose how to run

```docker-compose --env-file keycloak/local.env up```

The docker will create a realm called poc-realm with the client poc-vertx-client

## Create a user on called vertx-demo 

You can follow the documentation about how to create the user

```https://www.keycloak.org/docs/latest/server_admin/#proc-creating-user_server_administration_guide```

## Configure Api to work with keycloak

Check that exist a src/main/resources/config.yaml

```
api:
  port: 3001

oauth2:
  keycloak:
    site: http://localhost:8085/auth/realms/poc-realm
    clientId: poc-vertx-client
    clientSecret: {your-secret}
    tokenPath: /protocol/openid-connect/token
    jwkPath: /protocol/openid-connect/certs
    logoutPath: /protocol/openid-connect/logout
    userInfoPath: /protocol/openid-connect/userinfo
    introspectionPath: /protocol/openid-connect/token/introspect
    callbackPath: http://localhost:3000/callback
    scopes: openid
```

Here you need to insert you clientSecret replace the placeholder **{your-secret}**

## Get Token from keycloak

```bash
curl --location --request POST 'http://localhost:8085/auth/realms/poc-realm/protocol/openid-connect/token'\
 --header 'Content-Type: application/x-www-form-urlencoded'\
 --data-urlencode 'grant_type=password'\
 --data-urlencode 'username=vertx-demo'\
 --data-urlencode 'password=vertx-demo'\
 --data-urlencode 'client_id=poc-vertx-client'\
 --data-urlencode 'client_secret={secret}'
```

## Check is working

```bash
curl --location --request GET 'http://localhost:3000/secure/hello'\
 --header 'Authorization: Bearer {token}'
```

## Book Endpoints

The poc have a little ABM for books that work with the following endpoints:

``GET  /book/all``  Get All books

``GET  /book/:id``  Find a book by ID

``POST /book``  Create a new book, this endpoints need to have the authorization token for authorize the operation

## How to run with -conf from the IDE?


Create a new application with main class

```io.github.riojano0.pocvertxkeycloak.AppLauncher```

and in cli arguments fill with

```run io.github.riojano0.pocvertxkeycloak.MainVerticle -conf {your-file}```


## Consideration about override properties from fat-jar using the flag "-conf"

Currently, vertx only support properties of type .json Was extended the RunCommand following this work-around https://github.com/eclipse-vertx/vert.x/pull/4340 
but for you to remember if you want to override some property injection that configuration on the fat-jar, only is supported json files and not other yaml files (without the work-around)

Eg:

conf.json
```json
{
  "api": {
    "port": 8080
  },
  "oauth2": {
    "keycloak": {
      "clientSecret": "my-secret"
    }
  }
}
```

Command
```sh
java -jar my-fat.jar -conf conf.json
```