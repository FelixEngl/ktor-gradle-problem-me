# About

This is a minimal example for a problem of ktor with gradle in a MPP project.


## TL:DR
The JVM-ktor-dependencies do not only load the normal library versions like 
`ktor-server-core:2.0.0` but also the `ktor-server-core: jvmAndNixMain:2.0.0`.
Ktor is the only library causing this issue, so I think I either found a bug or I made a mistake in my gradle buildscript.


## Software Configuration

````
IntelliJ IDEA 2022.1 (Ultimate Edition)
Build #IU-221.5080.210, built on April 12, 2022
Runtime version: 11.0.14.1+1-b2043.25 amd64
VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
Windows 11 10.0
GC: G1 Young Generation, G1 Old Generation
Memory: 4096M
Cores: 16

Kotlin: 221-1.6.20-release-285-IJ5080.210
````

````
java --version 
openjdk 11.0.14.1 2022-02-08
OpenJDK Runtime Environment Temurin-11.0.14.1+1 (build 11.0.14.1+1)
OpenJDK 64-Bit Server VM Temurin-11.0.14.1+1 (build 11.0.14.1+1, mixed mode)
````




## Original Description From Slack
The problem these nix-libs cause show themselves in various ways, here are some
of the errors caused by it:

1. Looking at your code in IntelliJ, the IDE shows shows an error, that it can
   not resolve the classes of ktor.
2. When gradle tries to build the application with this nixAndJvmMain it either
   fails to resolve the classes or I get a "Out of heapspace"-exception.
3. When I run unit tests they start to run just fine, but when it tries to
   call some code from from "io.ktor:ktor-server-test-host:jvmAndNixMain:2.0.0"
   I get an exception saying "Function in module not found".

As far as I can say, all these errors are somehow related to ktor and its
nix-libraries. I'll go into further details with the following examples:


===== Example 1 =====
My Code:
````kotlin
fun initializeServer(applicationConfig: ApplicationConfig){
  require(!initialized) {
    "The server is already initialized!"
  }
  val config = applicationConfig.getFSConfig()

  try {
    config.config("fs")
  } catch (e: ApplicationConfigurationException) {
    logger.error(e) { "Tried to load a not-fs config!" }
    exitProcess(WRONG_CONFIG)
  }

  //<...>
}
````

What IntelliJ does:
IntelliJ underlines the `e: ApplicationConfigurationException` and shows the
following errors and documentations:

Error:
````
Type mismatch.
Required: Throwable
Found:    ApplicationConfigurationException
````

Documentation:
````
public final class ApplicationConfigurationException
Thrown when an application is misconfigured
  io.ktor.server.config   0_config.knm 
  Gradle: io.ktor:ktor-server-core:jvmAndNixMain:2.0.0 (io.ktor-ktor-server-core-jvmAndNixMain.klib)
````
The path to the source file, when I jump to the declaration of
ApplicationConfigException via IntelliJ is `C:\Users\felix\.gradle\caches\modules-2\files-2.1\io.ktor\ktor-server-core\2.0.0\7064a323594c9b548ea52254325d1013f02da942\ktor-server-core-2.0.0-sources.jar!\jvmAndNixMain\io\ktor\server\config\ApplicationConfig.kt`.
(This path does not exist in the filesystem.)

===== Example 2 =====
My Code:
````kotlin
val testApplication = TestApplication {
  environment {
    config = loadTestConfig("test_application.conf")
  }
  application {
    environment.monitor.subscribe(FSInitialized) {
      if (rootUser !in it.userManager)
        it.userManager.put(rootUser)
      if (adminUser !in it.userManager)
        it.userManager.put(adminUser)
      if (normalUser !in it.userManager)
        it.userManager.put(normalUser)
    }
    fsServer.apply { main() }
  }
}
````

The IDE marks `environment.monitor.<...>` as error with the message `Cannot
access 'environment': it is internal in 'TestApplicationBuilder'`.

The documentation of `TestApplication` says:
````
@[ERROR : io.ktor.util.KtorDsl]
public fun TestApplication(
  block: TestApplicationBuilder.() -> Unit
): TestApplication
Creates an instance of TestApplication configured with the builder block. Make sure to call TestApplication.stop after your tests.
See Also:
testApplication
  io.ktor.server.testing   1_testing.knm 
  Gradle: io.ktor:ktor-server-test-host:jvmAndNixMain:2.0.0 (io.ktor-ktor-server-test-host-jvmAndNixMain.klib)
````

The documentation of `application` says:
````
@[ERROR : io.ktor.util.KtorDsl]
public final fun application(
  block: [ERROR : io.ktor.server.application.Application].() -> Unit
): Unit
Adds a module to TestApplication.
See Also:
testApplication
  io.ktor.server.testing.TestApplicationBuilder 
  Gradle: io.ktor:ktor-server-test-host:jvmAndNixMain:2.0.0 (io.ktor-ktor-server-test-host-jvmAndNixMain.klib)
````
The declarations in the IDE are also pointing to some invalid paths.

===== Example 3 =====
Regardless to the errors in "Example 2" I can compile and run the Unit-Tests.
But then the code throws the following exception, when it reaches code from any
ktor package with a jvmAndNixMain-twin it throws a massive amount of exceptions.
(see example3_stacktrace.txt)

````
Module function cannot be found for the fully qualified name 'de.fs.web.PluginsKt.plugins'
io.ktor.server.engine.internal.ReloadingException: Module function cannot be found for the fully qualified name 'de.fs.web.PluginsKt.plugins'
	at app//io.ktor.server.engine.internal.CallableUtilsKt.executeModuleFunction(CallableUtils.kt:27)
	...
	at io.ktor.client.statement.HttpStatement.execute(HttpStatement.kt:46)
	at de.fs.web.BasicTests$testBasicConfigDownload$1$invoke$lambda-1$$inlined$withConfiguredTestApplication$1.invokeSuspend(BasicTests.kt:681)
Caused by: io.ktor.server.engine.internal.ReloadingException: Module function cannot be found for the fully qualified name 'de.fs.web.PluginsKt.plugins'
	at app//io.ktor.server.engine.internal.CallableUtilsKt.executeModuleFunction(CallableUtils.kt:27)
	...
	at app//kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:664)
````

===== Example 4 =====
When I check "Project -> External Libraries", the nixAndJvmMain-libs are loaded
from a different path than the other, normal working libs:

Normal:
````
Gradle: io.ktor:ktor-server-core-jvm:2.0.0
Classes:
    C:\Users\felix\.gradle\caches\modules-2\files-2.1\io.ktor\ktor-server-core-jvm\2.0.0\2bf374bfde9bd8e96f7b3ed3ff85e3ccf11aa8b0\ktor-server-core-jvm-2.0.0.jar
Sources:
    C:\Users\felix\.gradle\caches\modules-2\files-2.1\io.ktor\ktor-server-core-jvm\2.0.0\24cf9fccb8a1b7cff506364391b7263607880689\ktor-server-core-jvm-2.0.0.jar
````

Nix:
````
Gradle: io.ktor:ktor-server-core:jvmAndNixMain:2.0.0
Classes:
    D:\git\firmensuche\.gradle\kotlin\sourceSetMetadata\search.firmensuche\jvmMain\implementation\io.ktor-ktor-server-core

Sources:
    C:\Users\felix\.gradle\caches\modules-2\files-2.1\io.ktor\ktor-server-core-jvm\2.0.0\7064a323594c9b548ea52254325d1013f02da942\ktor-server-core-2.0.0-sources.jar
````

Like in "Example 1", the sources-path of the io.ktor:ktor-server-core:jvmAndNixMain:2.0.0
doesn't exist.


So my guesses right now are:
A) The library is not correctly rersolved in IntelliJ/gradle and erroneously
loaded as dependency.
B) The jvmAndNixMain should not be loaded as library at all and the MPP-Plugin
has some serious issues with either modules and (only) ktor.
