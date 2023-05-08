# Logging-lib

Library to handle rate limit logic

**Rate limiting** refers to the number of API calls the client (API consumer) is able to make

### Types
- **Key-level rate limiting**
  is more focused on controlling traffic from individual sources and making sure that users are staying within their prescribed limits
- **API-level rate limiting**
  assesses all traffic coming into an API from all sources and ensures that the overall rate limit is not exceeded.

### Algorithms
- **Token Bucket**: is a simple and widely used algorithm for rate limiting. It works by maintaining a bucket with a certain number of tokens. Each incoming request to the system removes one token from the bucket.
- **Leaky Bucket**: This is similar to Token Bucket, but instead of adding tokens to the bucket at a fixed rate, incoming requests are accumulated in the bucket and released at a fixed rate.
- **Fixed Window**: In this algorithm, the number of requests is limited over a fixed time window. Requests are rejected once the limit is reached.
- **Sliding Window**: In this algorithm, the rate of incoming requests is limited over a sliding time window. Requests are rejected if the rate exceeds the specified limit over any part of the sliding window.
- **Smooth Bursting**: This algorithm combines the benefits of Fixed Window and Token Bucket algorithms, allowing for a certain number of bursts of incoming requests, beyond which requests are rejected.

## Technologies
1. Java 11+
2. Spring
3. Redis
4. [Lombok](https://projectlombok.org/) is an annotation-based helper that saves you (or me, really) from creating many getters, setters, and constructors.
5. Sonarqube (`InProgress`)
6. [Spotless](https://github.com/diffplug/spotless) to enforce and maintain consistence standard format in the project


## Deployment

To publish package as private repo on GitHub

- Generate token in git hub side
- Define the user-name and token in mvn setting
``` xml
<servers>
    <server>
      <id>github</id>
      <username>user-name</username>
      <password>github-token</password>
    </server>

  </servers>

```
- Run mvn deploy command to publish the lib


## USAGE

- Add lib dependency to your POM or gradle file  
  **Maven**
``` xml
  <dependency>
        <groupId>com.gotrah</groupId>
        <artifactId>rate-limit-lib</artifactId>
        <version>1.0.0</version>
  </dependency>
```
**Gradle**
``` kotlin
implementation 'com.gotrah:rate-limit-lib:1.0.0'
```

- Make sure blow props config added to application.yml file

``` yaml
application:
  service-name: COUNTER_SERVICE
spring:
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT} 
    password: test
    timeout: 6000
```
- Implement UserIdentifier  interface to return the user unique identifier that can be used in Key-level rate limiting implementation

## Description

In this lib provide implementation for **Fixed Window**

    @RateLimit( perUser= true, FixedWindow = @FixedWindow( limit = 5 , per = 60 ))

    @RateLimit( perUser= true, FixedWindow = @FixedWindow( limitExp = "l.r" , perExp = "per.exp" ))

    @RateLimit( perApi= true, FixedWindow = @FixedWindow( limit = 5 , per = 60 ))

    @RateLimit( perApi= true, FixedWindow = @FixedWindow( limitExp = "l.r" , perExp = "per.exp" ))



**perUser** implement Key-level rate limiting , key user identifier

**perApi** implement API-level rate limiting

Only one of them per method can be used , method annotated by @RateLimit  with perApi= true and perUser= true
will throw runtime exception

**FixedWindow** implement Fixed Window algorithm
- per: configure window time
- limit: configure number of accepted request per window time 

