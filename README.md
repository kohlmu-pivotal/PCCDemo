# Look aside using Spring Boot Data for Pivotal GemFire
In this example the look aside client is implemented using purely  [Spring Boot Data For Pivotal GemFire](https://github.com/spring-projects/spring-boot-data-geode) API's.

The web service (REST) is exposed using [Spring Boot Web](). With the focus on the use of the [Spring Boot Data For Pivotal GemFire](https://github.com/spring-projects/spring-boot-data-geode) API's 
rather than exposing a REST endpoint, the use of [Spring Boot Web]() is not a problem. In addition to this, the application's Spring context
will be bootstrapped using [Spring Boot](). 

In this example, the underlying infrastructure is NOT a concern. The Spring Boot application itself will recognise if it 
has been deployed to a Platform environment (PCF) and configure itself accordingly.

In order to better understand all Spring framework and projects used, it is best to reference their documentation:
* [Spring Bood Data For Pivotal GemFire](https://docs.spring.io/autorepo/docs/spring-boot-data-geode-build/1.1.0.BUILD-SNAPSHOT/reference/html5/)
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
The [Spring Boot Data For Pivotal GemFire](https://docs.spring.io/autorepo/docs/spring-boot-data-geode-build/1.1.0.BUILD-SNAPSHOT/reference/html5/) 
application is started in the `io.pivotal.pcc.demo.client.ClientApp` class. The `main` method starts a Spring application using the `@SpringBootApplication`.

With the use of:
```
<dependency>
	<groupId>org.springframework.geode</groupId>
	<artifactId>spring-geode-starter</artifactId>
	<version>${spring-boot-data-gemfire.version}</version>
</dependency>
```
The Spring Boot application will now start the [Spring Boot Data For Pivotal GemFire](https://github.com/spring-projects/spring-boot-data-geode) autoconfigurations.

The most notable change is that a [Spring Boot Data For Pivotal GemFire](https://github.com/spring-projects/spring-boot-data-geode) 
application is by default a ClientCache application and security, pdx are enabled and configured by default.

Unlike the [GemFire API](https://github.com/kohlmu-pivotal/PCCDemo/tree/using-GemFireAPI) or [Spring Data for Pivotal Gemfire](https://github.com/kohlmu-pivotal/PCCDemo/tree/using-SDG) applications, 
which needed to be concerned with the underlying infrastructure, the this application does not! 

The parsing of the `VCAP` properties is done automatically and will populate the required fields to enable security and configurtion "push".

The `io.pivotal.pcc.demo.client.ClientApp` class is annotated with the following:
```
@SpringBootApplication
@EnableClusterConfiguration(useHttp = true)
@EnableEntityDefinedRegions(basePackageClasses = Book.class)
```

`@Configuration` indicates to the Spring framework that this class is a configuration class and needs to be processed

`@EnableClusterConfiguration` is a [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) annotation, which tells the [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) to
"push" the configuration to the server and create the necessary components on the server if they don't exist.

`@EnableEntityDefinedRegions` is a [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) annotation, which tell the [Spring Data For Pivotal GemFire](https://spring.io/projects/spring-data-gemfire) to
create a region for all defined `javax.persistence.Entity` or domain objects.

The class `io.pivotal.pcc.demo.client.controller.BookController` will expose the endpoints that are used to test with. In the `BookController` the 
`getBook` method will take the `isbn` parameter and pass it to the `io.pivotal.pcc.demo.client.service.BookService`.

Inside the `BookService` the `getBook` method is annotated with `@Cacheable("Books)` and in conjunction with the `@EnableGemFireCache` annotation, will signal the 
[Spring Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html) to start caching the results for this method.
All results shall be stored within the [Region](https://gemfire.docs.pivotal.io/98/geode/basic_config/data_regions/chapter_overview.html), named `Books`. 
In the case of a "cache miss" a synthetic delay is introduced and after that, a new `Book` cached, in the `Books` region, and returned.


