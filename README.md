# Look aside using Spring Data Geode/GemFire
In this example the look aside client is implemented using purely  [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) API's.

The web service (REST) is exposed using [Spring Boot Web](). With the focus on the use of the [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) API's 
rather than exposing a REST endpoint, the use of [Spring Boot Web]() is not a problem. In addition to this, the application's Spring context
will be bootstrapped using [Spring Boot](). 

In this example, the underlying infrastructure is still a concern. That means that processing of the `VCAP` properties from the `pccService` service
is a concern that still needs to handled. In a non-PCC environment, this is not a concern, but this is something that a developer is now concerned with.

In order to better understand all Spring framework and projects used, it is best to reference their documentation:
* [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire)
* [Spring Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html)

## How to run

Build this application with maven using `mvn clean package`.

Login into the relevant PCF environment: 

(usually with ` cf login [-a API_URL] [-u USERNAME] [-p PASSWORD] [-o ORG] [-s SPACE] [--sso | --sso-passcode PASSCODE]`)

Once logged in, deploy the build application with: `cf push -f manifest.yml --no-start`. 

This will deploy the webapp,named `sdgApp`, into PCF without starting it. It will also bound to the `pccService`

Once pushed it can be started with :

`cf rs sdgApp`

With the `sdgApp` running, it is time to test it out.

Open a browser or run a `curl` command against the url `https:sdgApp.{API_URL}/books/{isbn}` where the ISBN number is any random string. 
eg. `https://sdgApp.apps.pcfone.io/books/123TNGSD`

This command should return a result stating something similar the following:

`It took: 1575 millis to execute getBook: Book{isbn='123TNGSD', title='Tirra Lirra by the River', author='Melony Bednar', genre='Realistic fiction'} for ISBN: 123TNGSD`

Here one can see it hit the synthetic service invocation delay that one would like to improve. In this case this request took 1575ms to execute.
The next time this call is made, it should be able to use the cached value and the time taken should drop significantly to sub-10ms responses.

`It took: 3 millis to execute getBook: Book{isbn='123TNGSD', title='Tirra Lirra by the River', author='Melony Bednar', genre='Realistic fiction'} for ISBN: 123TNGSD`

## How it works
The [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) application is started in the `io.pivotal.pcc.demo.client.ClientApp` class. The `main` method starts a Spring application using the `@SpringBootApplication`.
With the same constraints as the [GemFire API](https://github.com/kohlmu-pivotal/PCCDemo/tree/using-GemFireAPI) application, which needs to be concerned with the underlying infrastructure, 
this application also needs to be concerned with parsing the `VCAP` properties in order to find its relevant properties.

The parsing of the `VCAP` properties is done in the `io.pivotal.pcc.demo.client.configuration.PcfPccEnvironmentApplicationContextInitializer` class. This class uses Spring's `org.springframework.core.env.ConfigurableEnvironment`
to process the `VCAP` properties into a more usable schema. The ContextInitializer class will process the properties, extract out 
the security url, credentials and gfsh Url and export them as Properties to be consumed by the [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) factory beans.

The main configuration of the application is done in the `io.pivotal.pcc.demo.client.configuration.ClientAppConfiguration` class.

The class is annotated with the following:
```
@ClientCacheApplication
@EnableClusterConfiguration
@EnableEntityDefinedRegions(basePackageClasses = Book.class)
@EnableGemfireCaching
@EnableGemfireRepositories(basePackageClasses = BookRepository.class)
@EnablePdx
@EnableSecurity
```

`@Configuration` indicates to the Spring framework that this class is a configuration class and needs to be processed

`@EnableClusterConfiguration` is a [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) annotation, which tells the [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) to
"push" the configuration to the server and create the necessary components on the server if they don't exist.

`@EnableEntityDefinedRegions` is a [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) annotation, which tell the [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) to
create a region for all defined `javax.persistence.Entity` or domain objects.

`@EnableGemfireCaching` is a [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) annotation, which will enable the [Spring Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html)
and enable GemFire as a caching provider

`@EnableGemFireRepositories` is a [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) annotation, which will enable GemFire specific [Repositories](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/stereotype/Repository.html) to be used.

`@EnablePdx` is a [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) annotation, which will turn on [PDX Serialization](https://gemfire.docs.pivotal.io/98/geode/developing/data_serialization/gemfire_pdx_serialization.html)

`@EnableSecurity` is a [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) annotation, which turns on Security and implements a [AuthInitialize](https://geode.apache.org/releases/latest/javadoc/org/apache/geode/security/AuthInitialize.html).
This replaces the AuthInitialize implementation in the [GemFire API](https://github.com/kohlmu-pivotal/PCCDemo/tree/using-GemFireAPI) implementation.  

The class `io.pivotal.pcc.demo.client.controller.BookController` will expose the endpoints that are used to test with. In the `BookController` the 
`getBook` method will take the `isbn` parameter and pass it to the `io.pivotal.pcc.demo.client.service.BookService`.

Inside the `BookService` the `getBook` method is annotated with `@Cacheable("Books)` and in conjunction with the `@EnableGemFireCache` annotation, will signal the 
[Spring Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html) to start caching the results for this method.
All results shall be stored within the [Region](https://gemfire.docs.pivotal.io/98/geode/basic_config/data_regions/chapter_overview.html), named `Books`. 
In the case of a "cache miss" a synthetic delay is introduced and after that, a new `Book` cached, in the `Books` region, and returned.



