# <img align="left" src="durian.png"> Durian: [Guava](https://github.com/google/guava)'s spikier (unofficial) cousin

[![Maven artifact](https://img.shields.io/badge/mavenCentral-com.diffplug.durian%3Adurian-blue.svg)](https://bintray.com/diffplug/opensource/durian/view)
[![Latest release](http://img.shields.io/badge/last release-2.0-blue.svg)](https://github.com/diffplug/durian/releases/latest)
[![Changelog](http://img.shields.io/badge/master-3.0--SNAPSHOT-lightgrey.svg)](CHANGES.md)
[![Travis CI](https://travis-ci.org/diffplug/durian.svg?branch=master)](https://travis-ci.org/diffplug/durian)
[![License](https://img.shields.io/badge/license-Apache-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))

Guava has become indispensable for many Java developers.  Because of its wide adoption, it must be conservative regarding its minimum requirements.

Durian complements Guava with some features which are too spiky for Guava, such as:
* [One-liner exception handling](test/com/diffplug/common/base/ErrorsExample.java?ts=4) for Java 8 functional interfaces (even with checked exceptions).
* A [simple replacement](src/com/diffplug/common/base/StringPrinter.java?ts=4) for the mess of `PrintStream`, `OutputStream`, `Writer`, etc. when all you want is to pipe some strings around.
* Given a node in a tree, and a [`Function<Node, List<Node>>`](src/com/diffplug/common/base/TreeDef.java?ts=4), create a `Stream` for [traversing](test/com/diffplug/common/base/TreeStreamTest.java?ts=4) this tree (breadth-first, depth-first, etc.).
* An [enum for handling comparisons](src/com/diffplug/common/base/Comparison.java?ts=4) in a pattern-matchey way.
* Guava's [`Suppliers`](src/com/diffplug/common/base/Suppliers.java?ts=4), [`Predicates`](src/com/diffplug/common/base/Predicates.java?ts=4), and [`Functions`](src/com/diffplug/common/base/Functions.java?ts=4) converted to Java 8, and a new [`Consumers`](src/com/diffplug/common/base/Consumers.java?ts=4) class to round it out.
* A few other carefully-curated Java 8 goodies:
	+ [Box and Box.Nullable](src/com/diffplug/common/base/Box.java?ts=4)
	+ [StackDumper](src/com/diffplug/common/base/StackDumper.java?ts=4)
	+ [MoreCollectors](src/com/diffplug/common/base/MoreCollectors.java?ts=4)

Durian's only requirement is Java 8 or greater, no other libraries are needed (not even Guava).

Contributions are welcome, see [the contributing guide](CONTRIBUTING.md) for development info.

## Acknowledgements

* The API and tests for `Suppliers`, `Functions`, and `Predicates` are all verbatim from [Guava](https://github.com/google/guava).
* `StringPrinter.toOutputStream()` borrows heavily from `WriterOutputStream`, inside Apache commons-io.
* `DurianPlugins` is inspired by RxJava's plugin mechanism.
* Formatted by [spotless](https://github.com/diffplug/spotless), [as such](https://github.com/diffplug/durian/blob/v2.0/build.gradle?ts=4#L70-L90).
* Bugs found by [findbugs](http://findbugs.sourceforge.net/), [as such](https://github.com/diffplug/durian/blob/v2.0/build.gradle?ts=4#L92-L116).
* Scripts in the `.ci` folder are inspired by [Ben Limmer's work](http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/).
* Built by [gradle](http://gradle.org/).
* Tested by [junit](http://junit.org/).
* Maintained by [DiffPlug](http://www.diffplug.com/).
