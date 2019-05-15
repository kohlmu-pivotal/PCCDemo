# Look aside using GemFire API
In this example the look aside client is implemented using purely GemFire client API's.

The web service (REST) is exposed using JAX-RS.

In this example the `PCCConfigurer` is tasked with parsing the `VCAP` properties from the `pccService` which bound to this app.

## How to run

Build this application with maven using `mvn clean package`.

Login into the relevant PCF environment: 

(usually with ` cf login [-a API_URL] [-u USERNAME] [-p PASSWORD] [-o ORG] [-s SPACE] [--sso | --sso-passcode PASSCODE]`)

Once logged in, deploy the build application with: `cf push -f manifest.yml --no-start`. 

This will deploy the webapp,named `gfApp`, into PCF without starting it. It will be automatically bound to the `pccService` using the command.

Once pushed, it can be started with :

`cf rs gfApp`

With the `gfApp` running the next task is to create the `Books` region within the [Pivotal Cloud Cache](https://pivotal.io/pivotal-cloud-cache) service.
The way to do this, is to run `cf env gfApp`. Which should return a JSON document, containing the `VCAP` properties. Look for a property, under the `p-cloudcache` tree, called `gfsh`. This is the url that `gfsh` requires to login into the cluster.

Also, look up a user under the heading of `users`, which has the `cluster_operator` role. Copy the `username` and `password`, as these to be used to login into the cluster using gfsh.

Open a terminal and navigate to your [Pivotal GemFire](https://pivotal.io/pivotal-gemfire) installation. In this case `9.7.1` is the required version to use.
Go to the `bin` directory and run the `gfsh` command.

Once `gfsh` has started log into the [Pivotal Cloud Cache](https://pivotal.io/pivotal-cloud-cache) cluster using the `gfhs url`, `username` and `password` from the `VCAP` properties using the following command:

`connect --url={gfshURl} --username={username} --password={password} --skip-ssl-validation=true --use-http --use-ssl` 

Once logged in, create the `Books` region using the command: `create region --name=Books --type=PARTITION_REDUNDANT`

With the `Books` region created on the server and the `gfApp` deployed in [Pivotal Cloud Foundry](https://pivotal.io/platform) , it is time to test it out.

Open a browser or run a `curl` command against the url `https:gfApp.{API_URL}/books/{isbn}` where the ISBN number is any random string. 
eg. `https://gfApp.apps.pcfone.io/books/123TNGSD`

This command should return a result stating something similar the following:

`It took: 1575 millis to execute getBook: Book{isbn='123TNGSD', title='Tirra Lirra by the River', author='Melony Bednar', genre='Realistic fiction'} for ISBN: 123TNGSD`

Here one can see it hit the synthetic service invocation delay that one would like to improve. In this case this request took 1575ms to execute.
The next time this call is made, it should be able to use the cached value and the time taken should drop significantly to sub-10ms responses.

`It took: 3 millis to execute getBook: Book{isbn='123TNGSD', title='Tirra Lirra by the River', author='Melony Bednar', genre='Realistic fiction'} for ISBN: 123TNGSD`

## How it works
The deployed web app is bound to an internal Tomcat Server, this is all auto configured using [Pivotal Cloud Cache](https://pivotal.io/pivotal-cloud-cache).

The class `io.pivotal.pcc.demo.client.ClientApp` implements the JAX-RS `javax.ws.rs.core.Application` and uses the `@ApplicationPath("/")` annotation to define the webcontext to bind to.

The JAX-RS defined by the dependency 
```
<dependency>
	<groupId>org.glassfish.jersey.bundles</groupId>
	<artifactId>jaxrs-ri</artifactId>
	<version>2.13</version>
</dependency>
```

will cause a web app to be created (without having to define a `web.xml` file as legacy webapps).

The class `io.pivotal.pcc.demo.client.web.BookController` will expose the endpoints that are used to test with. In the `BookController` the 
`getBook` method will take the `isbn` parameter and pass it to the `io.pivotal.pcc.demo.client.services.BookService`.

Inside the `BookService` the `getBook` method will check if the `isbn` exists within the `bookRegion` and if not, run a 
synthetic delay, create a new `io.pivotal.pcc.demo.client.domain.Book` and put it into the `Books` region.

Inside the `BookController` it can be seen that a `io.pivotal.pcc.demo.client.services.CacheService` is created. If one looks into the `CacheService` it can be
seen that "standard" [Pivotal GemFire](https://pivotal.io/pivotal-gemfire) API's are used to create a `ClientCache`.

This example is really simple as one Region is created and no other features are used. In addition, the `ClientCache` uses a `io.pivotal.pcc.demo.client.PCCConfigurer` 
which is responsible for extracting the necessary properties from the [Pivotal Cloud Foundry](https://pivotal.io/platform) `VCAP` environmental properties for the `gfApp` application and `pccService`.



