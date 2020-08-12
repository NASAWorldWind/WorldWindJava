# Contributing to WorldWind Java

#### Table of Contents

[Asking Questions](#asking-questions)

[Design and Coding Guidelines](#design-and-coding-guidelines)

[Contributing](#contributing)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting New Features](#suggesting-new-features)
- [Pull Requests](#pull-requests)

[Resources](#resources)

## Asking Questions

**Please do not file an issue to ask a question.** You will get faster results by using the following resources:

- Email the [Administrative Contact](mailto:worldwind-info@lists.nasa.gov)

## Design and Coding Guidelines

These design and coding guidelines are for **WorldWind Java** and do not necessarily reflect the expectations for other
WorldWind projects.

### General

* The project's development IDE is Apache NetBeans. The NetBeans configuration files for this project are checked in to
the code repository. They define within them global and local library links, formatting rules, etc.
* Our required target platforms are OS X, Windows, and the most popular versions of Linux. All code
and all products must run on all those systems.
* Read the WorldWind API's Overview section for a description of WorldWind architecture, design and usage. Read 
the overview pages of each WorldWind package for descriptions of those. These descriptions contain critical 
information that is not repeated elsewhere. To avoid making mistakes, everyone working on WorldWind must read
them before using or modifying the code.
* Most major classes need a no-argument constructor so that the declarative instantiation mechanism can work. WW
objects should avoid constructors with arguments so that they can be created generically by name. This means
they should self-configure if at all possible, drawing their parameterized info from Configuration. They should
also contain an interface to set the configuration details programmatically.
* Make field and variable names clear and easy to read. Don't label them with "my" or "m_" or some other goofy
notation. Within a class refer to all member fields with "this", e.g., this.tileCount.
* The buffers one must use to pass arrays of info to JOGL must have their byte order set to that of the machine
they're used on. Call nativeByteOrder() on NIO buffers when you deal with them, use the methods in
com.jogamp.common.nio.Buffers.
* Favor immutability (all fields final) in classes representing some small entity like a Point or Vector.
Immutable classes are fully thread safe and generally less error prone.
* Don't worry too much about frequent memory allocations. Java is now so optimized for this that allocating an
object on the heap has similar performance to allocating it on the stack, and this includes the cost of garbage
collection. There is still a cost to executing any code, however, so be smart about allocation frequency.
* Configuration items typically have two values and thus two attribute names: a DEFAULT value that is used if not
overridden, and a non-default value that can be set programmatically (in Configuration) to a current value
without losing the ability to recover the default value.
* Classes such as BasicDataFileStore and the logger are effectively singletons but they are not defined in their
class definition as such. Their singleton nature comes from their 1:1 association with the truly singleton
WorldWind class, which provides access to instances of these "singleton" classes.
* Do not use classes that are not available in the standard JRE. Don't incur additional or external
dependencies. The only 3rd-party library we rely on is JOGL.
* Constants are defined as String and namespace qualified. This enables easy and non-conflicting extension.
* Do not use GUI builders to generate interfaces for examples or applications. They prevent others from being able
to maintain the code.
* Protect OpenGL state within a rendering unit, such as a layer, by bracketing state changes within try/finally
clauses. The util.OpenGLStackHandler utility class makes this easy. It manages both attribute state and matrices when it fails.
* WorldWind never crashes. Always catch exceptions at least at the highest entry point from the runtime, e.g., UI
listeners, thread run() methods.
* Within a rendering pass WorldWind does not touch the disk or the network. Always fork off a thread to do that.
Use the TaskManager and Retriever systems to start threads during rendering. These are set up to govern thread
usage to avoid swamping the local machine and the server.
* Too much micromanagement of state makes the code brittle. Try to design so that the right thing just happens
once things are set up, and the effect of something going wrong is benign. For example, Layers fork off
Retriever objects to retrieve data from the network. But they don't try to keep track of these. If the retriever
succeeds then the data will be available the next time the layer looks for it. The fact that it's not there
because of a timeout or something tells the layer to ask for it again if it needs it.
* DOMs are expensive in memory and time. Use an event for any documents that might be large. Use the parser in
gov.nasa.worldwind.util.xml when appropriate.

### Exceptions
          
* WW objects running in the Main thread pass exceptions through to the application unless there's good
reactive/corrective behavior that can be applied within WW.

## Contributing

* Log any exceptions prior to throwing them. Use the same message for the log as for the exception.
* Ensure all exception messages are generated using the i18n method details below.
* Public methods validate their arguments and throw the appropriate exception, typically InvalidArgumentException,
and identify the exception message the parameter name and the problem -- null, out of range, etc. See the
message resource file, util.MessageStrings.properties, for common messages and their identifiers.
* In Retriever threads, do not catch Throwable. Catch and react to the Exception if there's a good reactive/corrective
behavior to apply, otherwise allow them to pass up the stack. Retriever threads should have an uncaught
Exception handler specified for the thread. The method should log the Exception or Throwable and then return.
* Private and protected methods whose calling client can't be trusted validate their arguments and throw an
appropriate exception.
* The audience for exceptions is not primarily the user of the client program, but the application or WorldWind
developer. Throw exceptions that would let them know immediately that they're using faulty logic or data.

### Logging

* Logging using java.util.logging has the nice feature of capturing the class and method name at the site of the
logging call. That's why there is the common idiom of create message, log message, throw exception. Wrapping
these three actions in some utility method would lose the class and method-name feature, so don't do that. Don't
use any logging system other than that in the JRE. 
* Log all exceptional conditions before rethrowing or throwing a new exception.
* Ensure all logging uses i18n messages as detailed below.
* Use level SEVERE for things that prevent the intended action,e.g., file can't be written. Use level WARN for
things that don't stop the action but seem exceptional, e.g., a file was retrieved or written redundantly. Use
level FINE for simple notifications. Use FINER for method traces. Using the "FINE"series prevents screen display
of these when the default Java logging settings are used. Since we're a component, we don't communicate such
things directly to the application's user; the application does.

### Concurrency

* Use collection classes from the java.util.concurrent package if there's any chance at all that the collection 
may be accessed from multiple threads.
* Except for simple atomic variables (but not long or double) the safest way to manage multi-thread access is
through the blocking queue classes of java.util.concurrent.
* Making a class' fields final avoids concurrency problems, but it makes the class much less extensible. If using
private, make sure there are overridable set/get accessors.

### Offline Mode 

* WorldWind's use of the network can be disabled by calling {@link gov.nasa.WorldWind.setOfflineMode}. Prior to
attempting retrieval of a network resource -- anything addressed by a URL -- WorldWind checks the offline-mode
setting and does not attempt retrieval if the value is true. To honor this contract, all code must check network
status prior to attempting retrieval of a resource on the network.

### Documentation

* Use the appropriate Ant target to generate worldwind.jar and javadoc API documentation. Do not use the NetBeans
Tools command because it's not configured appropriately, only the Ant targets are.
* All public and protected classes, methods and fields should be commented for javadoc documentation generation.
* Descriptions of classes, methods, etc. should start with a capital letter. Parameter descriptions and
return-value description should start with a lower-case letter. All descriptions end with a period.
* If a class overrides methods from {@link Object} such as <code>toString()</code> and <code>equals()</code>,
their behavior for the specific class should be described. For <code>equals()</code> that would be the fields   
compared. For <code>toString()</code> that would be the representation returned.
* Use links liberally, e.g., {@link WorldWind}. They help the reader get to information fast.
   
### Code Formatting

* Use the code formatting and style that's in the NetBeans project file. It makes it possible to review previous code
modifications.
* We generally use the traditional Java coding conventions. Constants are in all upper case with words separated by
underscores. Everything else is in camel case. Class names start with an upper case character, variables start
in lower case.
* White space is preferred over packing code into a small space. Please use white space liberally.
* Resolve all NetBeans warnings before checking in a file. If the warning refers to an intentional case, add an
exception statement.

### Internationalization (i18n)

* String "constants" are stored in separate resource files (e.g. MessageStrings.properties). These files must end 
in ".properties" and must be stored in the src directory. Strings are stored with the format:&nbsp;
packageOfClass.className.nameOfString=value of the string
* Access the string constants by using the following pattern:&nbsp; (e.g. Logging.getMessage("myPackage.myClass.targetStringName");).

### Books

The books we go back to again and again are the following:
* *Core Java*, Horstmann & Cornell, Volumes 1 and 2, Prentice Hall. Be sure to get the editions covering at 
least J2SE 5. Get the newest edition (currently 8).
* *The Java Programming Language*, Arnold & Gosling, Addison Wesley. Be sure to get the most recent edition
covering at least Java 6.
* *Concurrent Programming in Java*, Lea, Addison Wesley
* *Java Threads*, Oaks & Wong, O'Reilly
* *Java Cookbook*, Darwin, O'Reilly
* *OpenGL Programming Guide*, Shreiner & Woo & et al, Addison Wesley. Be sure to get the version covering
OpenGL 2.0, which is currently the Fifth Edition.
* *Mathematics for 3D Game Programming & Computer Graphics*, Lengyel, Charles River Media. Be sure to get the
Second (or later if there is one) edition.

## Contributing
        
### Reporting Bugs

This section guides you through submitting a bug report to WorldWind Java. Following these guidelines helps both the
WorldWind team and community understand your report, reproduce the behavior, and find related reports.

#### Before Submitting a Bug Report

* Check this repository's [**issues**](https://github.com/NASAWorldWind/WorldWindJava/issues) to see if the problem has
already been reported. If it has and the issue is **still open**, add a comment to the existing issue instead of opening
a new one.

> **Note:** If you find a **Closed** issue that seems like it is similar to what you are experiencing, open a new issue
and include a link to the original issue in the body of your new one.

#### Submitting a Good Bug Report

Bugs are tracked as [GitHub issues](https://guides.github.com/features/issues/). After you've complete the prerequisites
for submitting a bug, create an issue in the appropriate repository and providing the following information by filling out
the [template](ISSUE_TEMPLATE.md).

Explain the problem and include additional details to help the WorldWind team reproduce the problem:

* **Use a clear, descriptive title.**
* **Describe the exact steps which reproduce the problem.** Please be as detailed as possible. When listing steps, don't
just say what you did, but explain how you did it.
* **Provide specific examples.** Include the appropriate files, links, or code snippets which will help the WorldWind
team and community better understand the issue.
* **Describe the behavior.** Detail what behavior you observed and point out what is wrong with that behavior. Explain
which behavior you expected to see and why.

Provide more context by answering these questions:

* **Did the problem start happening recently?**
* **Can you reliably reproduce the issue?** If not, provide details about how often the problem happens and under which
conditions it normally happens.
* **Does the problem happen for all files, or only some?**
* **What is the name and version of the OS you're running?**

### Suggesting New Features

This section guides you through submitting and enhancement for WorldWind Java, including completely new features and minor
improvements to existing functionalities. Following these guidelines helps the WorldWind team and community understand
your suggestion and find related suggestions.

Before creating new feature suggestions, check this repository's [issues](https://github.com/NASAWorldWind/WorldWindJava/issues)
as you may find out that you don't need to create one. When you are creating an enhancement suggestion, please provide as many details as possible. Fill in the [template](ISSUE_TEMPLATE.md), including the steps that you imagine you would take if the feature you're requesting existed.

#### Submitting a Good New Feature Suggestion

New feature suggestions are tracked as [GitHub Issues](https://guides.github.com/features/issues/). After you've checked for existing issues that might relate to your suggestion, create an issue
in the appropriate repository and provide the following information:

* **Use a clear and descriptive title.**
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps.**
* **Explain why this enhancement would be beneficial** to most WorldWind users.
* **Specify the name and version of the OS you're using.**

### Pull Requests

* Fill in the [PULL_REQUEST template](PULL_REQUEST_TEMPLATE.md).
* Do not include issue numbers in the PR title.
* Provide a description of the change.
* Explain why this code should be in the core.
* Describe possible benefits and drawbacks from merging this pull request.
* Specify some issues to which this pull request is applicable.

## Resources

For WorldWind Java tutorials and examples, please check out our website: https://worldwind.arc.nasa.gov/.

To reach our Administrative Contact, please email: [Administrative Contact](mailto:worldwind-info@lists.nasa.gov)

