# <img align="left" src="durian.png"> Durian: [Guava](https://github.com/google/guava)'s spikier (unofficial) cousin

[![JCenter artifact](https://img.shields.io/badge/jcenter-com.diffplug.durian%3Adurian-blue.svg)](https://bintray.com/diffplug/opensource/durian/view)
[![Branch master](http://img.shields.io/badge/master-1.0-lightgrey.svg)](https://github.com/diffplug/durian/releases/latest)
[![Branch develop](http://img.shields.io/badge/develop-1.1--SNAPSHOT-lightgrey.svg)](https://github.com/diffplug/durian/tree/develop)
[![Branch develop Travis CI](https://travis-ci.org/diffplug/durian.svg?branch=develop)](https://travis-ci.org/diffplug/durian)
[![License](https://img.shields.io/badge/license-Apache-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))

# NOT YET SUITABLE FOR USE - we're releasing a formerly internal library, bear with us as we clean it up for public release

Guava has become indispensable for many Java developers.  Because of its wide adoption, it must be conservative regarding its minimum requirements.

Durian complements Guava with some features which are too spiky for Guava, such as:
* One-liner exception handling for Java 8 functional interfaces.
* A simple replacement for the mess of `PrintStream`, `OutputStream`, `Writer`, etc. when all you want is to pipe some strings around.
* Given a node in a tree, and a `Function<Node, List<Node>>`, create a `Stream` for traversing this tree (breadth-first, depth-first, etc.).
* An enum for handling comparisons in a pattern-matchey way.
* Guava's `Suppliers`, `Predicates`, and `Functions` converted to Java 8, and a new `Consumers` class to round it out.

Durian's only requirement is Java 8 or greater, no other libraries are needed (not even Guava).

## Acknowledgements

* The API and tests for `Suppliers`, `Functions`, and `Predicates` were all ripped verbatim from [Guava](https://github.com/google/guava).
* `StringPrinter.toOutputStream()` borrows heavily from `WriterOutputStream`, inside Apache commons-io.
* `DurianPlugins` is inspired by RxJava's plugin mechanism.
* Formatted by [spotless](https://github.com/diffplug/spotless).
* Bugs found by [findbugs](http://findbugs.sourceforge.net/).
* Built by [gradle](http://gradle.org/).
* Tested by [junit](http://junit.org/).
* Artifacts hosted by [jcenter](https://bintray.com/bintray/jcenter) and uploaded by [gradle-bintray-plugin](https://github.com/bintray/gradle-bintray-plugin).
* Maintained by [DiffPlug](http://www.diffplug.com/).
