# API-GATEWAY

### 1 Glossary

* **Route**: It is defined by an ID, a destination URI, a collection of predicates, and a collection of filters.

* **Predicate**: Match on anything from the HTTP request, such as headers or parameters.

* **Filter**: Can modify requests and responses before or after sending the downstream request.

![Gateway Component](https://cloud.spring.io/spring-cloud-gateway/reference/html/images/spring_cloud_gateway_diagram.png)





### 1. Route

```
management.endpoint.gateway.enabled=true # default value
management.endpoints.web.exposure.include= gateway
```


```
curl --location --request GET 'http://localhost:8024/api-gateway/configuration/config/customer/jdbc' --header 'Authorization: Basic QU5ITlQ6QU5ITlQtUEFTUw=='
```

### 1. Actuator

```
curl --location --request GET 'http://localhost:8024/actuator/gateway/routes'
```

```
curl --location --request GET 'http://localhost:8024/actuator/gateway/globalfilters'
```

```
curl --location --request GET 'http://localhost:8024/actuator/gateway/routefilters'
```

### 2.Creating and Deleting a Particular Route

```
POST: /gateway/routes/{id_route_to_create}
DELETE: /gateway/routes/{id_route_to_create}
```


[https://www.baeldung.com/java-digital-signature](https://www.baeldung.com/java-digital-signature)