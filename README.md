# Gradle Degraph Plugin [![Build Status](https://travis-ci.org/obecker/gradle-degraph.svg?branch=master)](https://travis-ci.org/obecker/gradle-degraph)

This plugin adds a verification task to the build that executes [Degraph](https://github.com/schauder/degraph) 
dependency checks on the sources of the gradle project.
It will detect package cycles without any additional configuration. 
Using custom configuration settings you can define which package dependencies are allowed and which should be rejected. 

The latest version uses Degraph 0.1.4

## Installation

There is no release of this plugin yet.

However, you can clone this repository and install the plugin locally like this:

```
gradlew publishToMavenLocal
```

Then add the following configuration to your project's `build.gradle`:
```groovy
buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath 'de.obqo.gradle:gradle-degraph:0.1.4.0-SNAPSHOT'
    }
}

apply plugin: 'de.obqo.gradle.degraph'
```

## Running

The plugin creates for each source set a corresponding <code>degraph<i>SourceSetName</i></code> task. 
Additionally there is one `degraph` task that runs all these source set specific tasks. 

You can run a single degraph check (for example for the `main` source set) with

```
gradle degraphMain
```

You can run all degraph checks with

```
gradle degraph
```

The `degraph` task has a dependency on the `check` task, so it will be executed together with other verification tasks if you run

```
gradle check
```

The result of each source set specific degraph task will be written to a corresponding file <code>$buildDir/reports/degraph/<i>name</i>.graphml</code> 
(where <i>name</i> is the name of the source set) in the
[GraphML format](https://en.wikipedia.org/wiki/GraphML).

## Configuration

The plugin adds a `degraph` configuration object to the build, that offers corresponding settings to those described in
the [Degraph Manual](http://blog.schauderhaft.de/degraph/documentation.html) (see in particular the [Testing](http://blog.schauderhaft.de/degraph/documentation.html#testing-of-dependencies)) section.

All configuration settings are optional.

<pre>
<b>degraph</b> {
    <b>sourceSets</b> sourceSets.main, sourceSets.test, ...
    <b>including</b> 'org.example.includes.**', ...
    <b>excluding</b> 'org.example.excludes.**', ...
    <b>slicings</b> {
        <i>name1</i> {
            <b>patterns</b> 'org.example.(*).**', ...
            <b>allow</b> 'a', 'b', ...
            <b>allowDirect</b> 'x', 'y', ...
        }
        <i>name2</i> {
            ...
        }
    }
}
</pre>

(_Note_: technically all configuration settings are method calls and no property assignments. 
So you have to use `sourceSets ...` or even `sourceSets(...)` instead of `sourceSets = ...`)

* `sourceSets`
  defines the source sets that should be analyzed. 
  By default all source sets defined in the gradle build file are considered.
  That means this option is most useful if you only want a subset of the source sets to be checked.

* `including`
  defines ant style string patterns for the classes that should be included (default: all) 

* `excluding`
  defines ant style string patterns for the classes that should be excluded (default: none)

* `slicings`
  starts the slicings block, each [slicing](http://blog.schauderhaft.de/degraph/documentation.html#adding-slicings) 
  is defined by its name (also known as slicing type). A slicing configuration contains:
  * `patterns`
     a list containing unnamed patterns (String) or named patterns. 
     A named pattern is defined using <code>namedPattern(<i>name</i>, <i>pattern</i>)</code>.<br>
     _Note_: in a named pattern the name comes first, like in the Scala Constraint DSL, but unlike in the Java Constraint DSL 
  * `allow`
    defines a [simple constraint](http://blog.schauderhaft.de/degraph/documentation.html#simple-constraints-on-slicings) on the defined slices
  * `allowDirect`
    defines a [strict constraint](http://blog.schauderhaft.de/degraph/documentation.html#strict-constraints) on the defined slices. As constraints (both simple and strict) you can use
    * a string (referencing the name of the pattern/slice)
    * <code>anyOf(<i>slice, ...</i>)</code>, see [Unspecified order of slices](http://blog.schauderhaft.de/degraph/documentation.html#unspecified-order-of-slices)
    * <code>oneOf(<i>slice, ...</i>)</code>, see [One of many slices](http://blog.schauderhaft.de/degraph/documentation.html#one-of-many-slices)

* _Note_: there is no `noJars` option. 
  The `degraph` task will use only the outputs of the given source sets, which don't include jars by default.

## Examples

Here are some examples from the degraph manual and their corresponding gradle configuration:

Slicing configuration in the Scala Constraint DSL:
```scala
classpath.including("de.schauderhaft.**") //
    .withSlicing("module", "de.schauderhaft.(*).**") // use the third part of the package name as the module name
    .withSlicing("layer", 
        ("persistence","de.schauderhaft.legacy.db.**"), // consider everything in the package de.schauderhaft.legacy.db and subpackages as as part of the layer "persistence"
        "de.schauderhaft.*.(*).**") // for everything else use the fourth part of the package name as 
    ) 
```
Gradle:
```groovy
degraph {
    including 'de.schauderhaft.**'
    slicings {
        module {
            patterns 'de.schauderhaft.(*).**'
        }
        layer {
            patterns(namedPattern('persistence', 'de.schauderhaft.legacy.db.**'), 
                     'de.schauderhaft.*.(*).**')
        }
    }
}
```

Slice constraints in the Scala Constraint DSL:
```scala
classpath
    .withSlicing("module", "de.schauderhaft.degraph.(*).**")
        .allow("check", anyOf("configuration", "graph"), "model")
```
Gradle:
```groovy
degraph {
    slicings {
        module {
            patterns 'de.schauderhaft.degraph.(*).**'
            allow 'check', anyOf('configuration', 'graph'), 'model'
        }
    }
}
```

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
