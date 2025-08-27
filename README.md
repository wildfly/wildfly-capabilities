---
layout: base
permalink: /project.html
---
WildFly Capabilities
====================


This project provides the central registry of descriptive information about capabilities accessible via the management layer of a [WildFly Core](http://github.com/wildfly/wildfly-core) based process. The intent is to provide a central location where capability developers can go to learn about other capabilities available in the WildFly ecosystem and to advertise their capability. Most importantly, registering capabilities helps ensure that different capabilities won't use the same name.

Building
------------------

This project does not produce a Java artifact. It is simply a registry of information. It does, however, produce a site using
[Roq](https://iamroq.com), a Quarkus-based static site generator. To build the site locally, run

```
$ cd roq
$ ./mvnw quarkus:dev
```

To generate a publishable site, run

```
$ cd roq
$ export QUARKUS_ROQ_GENERATOR_BATCH=true
$ ./mvnw package quarkus:run
```

You will find the build site in `target/roq`.

Structure of the Registry
-------------------------

The registry consists of two primary elements:

* a single text file in the root of the repository, 'registry.txt' which is a simple alphabetically ordered list of capability names. This file provides a single location where users can quickly learn the names of available capabilities
* a filesystem tree where the tree structure corresponds to the names of the capabilities. Each leaf in the tree is a 'capability.adoc' file which includes standard information describing a particular capability. The 'template.adoc' file in the root of the repository shows the standard format of a capability.adoc file.

Capabilities
-------------------

A capability is a piece of functionality used in a WildFly Core based process that is exposed via the WildFly Core management layer. Capabilities may depend on other capabilities, and this interaction between capabilities is mediated by the WildFly Core management layer.

Some capabilities are automatically part of a WildFly Core based process, but in most cases the configuration provided by the end user (i.e. in standalone.xml, domain.xml and host.xml) determines what capabilities are present at runtime. It is the responsibility of the handlers for management operations to register capabilities and to register any requirements those capabilities may have for the presence of other capabilities.

* Capabilities vs modules: A JBoss Modules module is the means of making resources available to the classloading system of a WildFly Core based process. To make a capability available, you must package its resources in one or more modules and make them available to the classloading system. But a module is not a capability in and of itself, and simply copying a module to a WildFly installation does not mean a capability is available. Modules can include resources completely unrelated to management capabilities.
* Capabilities vs Extensions: An extension is the means by which the WildFly Core management layer is made aware of manageable functionality that is not part of the WildFly Core kernel. The extension registers with the kernel new management resource types and handlers for operations on those resources. One of the things a handler can do is register or unregister a capability and its requirements. An extension may register a single capability, multiple capabilities, or possibly none at all. Further, not all capabilities are registered by extensions; the WildFly Core kernel itself may register a number of different capabilities.

Capability Names
-------------------

Capability names are simple strings, with the dot character serving as a separator to allow namespacing.

The 'org.wildfly' namespace is reserved for projects associated with the [WildFly organization on github] (https://github.com/wildfly)

The structure of this repository is based on the structure of capability names. The name is split based on the dot character and then each section becomes a directory in the repository. Each directory whose path from the repository root represents a complete capability name then includes a capability.adoc file describing that capability.

Statically vs Dynamically Named Capabilities
--------------------------------------------

The full name of a capability is either statically known, or it may include a statically known base element and then a dynamic element. The dynamic part of the name is determined at runtime based on the address of the management resource that registers the capability. For example, the management resource at the address '/socket-binding-group=standard-sockets/socket-binding=web' will register a dynamically named capability named 'org.wildlfy.network.socket-binding.web'. The 'org.wildlfy.network.socket-binding' portion is the static part of the name.

All dynamically named capabilities that have the same static portion of their name should provide a consistent feature set and set of requirements. The entry in this registry should be for the static part of the capability name. So, for the socket-binding example above, the registry.txt file would include a line for 'org.wildlfy.network.socket-binding' and the repository would include a capability.adoc file at location org/wildfly/network/socket-binding/capability.adoc.

Services provided by a capability
---------------------------------

Typically, a capability functions by registering a service with the WildFly process' MSC ServiceContainer, and then dependent capabilities depend on that service. The WildFly Core management layer orchestrates registration of those services and service dependencies by providing a means to discover service names.

The capability.adoc entry for a capability should provide information about the service made available by the capability, if there is one.

Custom integration APIs provided by a capability
------------------------------------------------

Instead of or in addition to providing MSC services, a capability may expose some other API to dependent capabilities. This API must be encapsulated in a single class (although that class can use other non-JRE classes as method parameters or return types).

The capability.adoc entry for a capability should provide information about the custom integration API made available by the capability, if there is one.

Capability Requirements
------------------------

A capability may rely on other capabilities in order to provide its functionality at runtime. The management operation handlers that register capabilities are also required to register their requirements.

There are three basic types of requirements a capability may have:

* Hard requirements. The required capability must always be present for the dependent capability to function.
* Optional requirements. Some aspect of the configuration of the dependent capability controls whether the depended on capability is actually necessary. So the requirement cannot be known until the running configuration is analyzed.
* Runtime-only requirements. The dependent capability will check for the presence of the depended upon capability at runtime, and if present it will utilize it, but if it is not present it will function properly without the capability. There is nothing in the dependent capability's configuration that controls whether the depended on capability must be present. Only capabilities that declare themselves as being suitable for use as a runtime-only requirement should be depended upon in this manner.

Hard and optional requirements may be for either statically named or dynamically named capabilities. Runtime-only requirements can only be for statically named capabilities, as such a requirement cannot be specified via configuration, and without configuration the dynamic part of the required capability name is unknown.

The capability.adoc entry for a capability should provide information about capability requirements, if there are any.

Supporting runtime-only requirements
------------------------------------

Not all capabilities are usable as a runtime-only requirement, and the capability.adoc entry for a capability should express this.

Any dynamically named capability is not usable as a runtime-only requirement.

For a capability to support use as a runtime-only requirement, it must guarantee that a configuration change to a running process the removes the capability will not impact currently running capabilities that have a runtime-only requirement for it. This means:

* A capability that supports runtime-only usage must ensure that it never removes its runtime service except via a full process reload.
* A capability that exposes a custom integration API generally is not usable as a runtime-only requirement. If such a capability does support use as a runtime-only requirement, it must ensure that any functionality provided via its integration API remains available as long as a full process reload has not occurred.

Note that declaring support for use as a runtime-only requirement is part of the permanent contract of a capability, so it should not be done lightly. For example, the current implementation of a particular provider of a capability may require a full process reload in order to remove it, but other providers may not. Or, the particular provider requiring reload may be a current implementation detail but one that could be changed in the future. Declaring the capability supports runtime-only use precludes these possibilities.

Private capabilities
--------------------

A capability can be private if the project that registers it does not wish its services or custom integration API to be depended upon by others. Any service or custom integration API exposed by the capability can be changed at any time, and it is not necessary to record information in this registry about such services or integration API. However, the capability itself should be recorded, including descriptive information about what it does and what requirements it may have.

Private capabilities are useful because only a capability can depend upon another capability and use its services and integration API.  "Anonymous" code cannot require capabilities because that would result in users not being able to determine what functionality would be lost if a required capability were removed. However, just because code wishes to use another capability and is willing to register itself as doing so doesn't mean it is willing to publish a contract that other, unknown capabilities can rely upon.

Private capabilities are allowed to provide services or a custom integration API for use by other capabilities controlled by the maintainer of the private capability. However the private capability implementation is free to change those at any time. This kind of usage scenario is analogous to typical use of package-protected Java methods.

Contents of capability.adoc {#contents}
--------------------------

The following is information describing the contents of a capability.adoc file. See the template.adoc file in the repository root for context.

NAME: The name of the capability. See above.

DESCRIPTION: A free form description of the capability

REGISTERED BY: Information about the project that is responsible for the integration contract exposed by the capability. There may be multiple projects that provide an implementation the capability, but there should be a single project responsible for the capability's contract. The contract is defined by the capability.adoc file and by the Java API of any classes referenced in the "SERVICE PROVIDED" and "CUSTOM INTEGRATION API" sections below. The following information should be provided:

* NAME: The name of the project
* URL: A useful URL. Perhaps a general project URL, perhaps one for relevant source code (e.g. a github repo)

DYNAMIC: True if the capability is dynamically named; false otherwise.

PRIVATE: True if the capability should not be used outside of its respective codebase; false otherwise. If true, any SERVICE PROVIDED or CUSTOM INTEGRATION API information provided is purely informational and is subject to change at any time.

SUPPORTS RUNTIME ONLY: True if the capability can be depended upon by another capability as a runtime-only requirement. Must be 'false' if DYNAMIC is 'true'.

SERVICE PROVIDED: Information about an MSC service registered by the capability which can be depended upon or injected by other capabilities, if there are any.

* CLASS: The fully qualified class name of the value type of the MSC Service
* MODULE: The name of the jboss-modules module that typically provides the class

CUSTOM INTEGRATION API: Information about the custom integration API provided by the capability, if there is one.

* CLASS: The fully qualified class name of class that provides a custom API for interacting with the capability
* MODULE: The name of the jboss-modules module that typically provides the class

HARD REQUIREMENTS: Information about other capabilities that must be present if this capability is registered, if there are any. For each such capability the following should be provided:

* NAME: The static portion of the name of the required capability
* DYNAMIC: True if the required capability is dynamically named

OPTIONAL REQUIREMENTS: Information about other capabilities that must be present if some aspect of this capability's configuration is present, if there are any. For each such capability the following should be provided:

* NAME: The static portion of the name of the required capability
* DYNAMIC: True if the required capability is dynamically named
* DESCRIPTION: Free form text description of what makes this requirement optional

RUNTIME ONLY REQUIREMENTS: Information about other capabilities that cannot be required by this capability's configuration, but which will be detected and used if present.

* NAME: The static portion of the name of the required capability
* DESCRIPTION: Free form text description of what functionality is enabled if this capability is present
