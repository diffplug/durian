# Durian: [Guava](https://github.com/google/guava)'s spikier (unofficial) cousin

![durian-logo](durian.png)
[![Release](http://img.shields.io/badge/master-0.1-lightgrey.svg)](https://github.com/diffplug/durian/releases/latest)
[![Build Status](https://travis-ci.org/diffplug/durian.svg?branch=master)](https://travis-ci.org/diffplug/durian)
[![Snapshot](http://img.shields.io/badge/develop-0.2--SNAPSHOT-lightgrey.svg)](https://github.com/diffplug/durian/tree/develop)
[![Build Status](https://travis-ci.org/diffplug/durian.svg?branch=develop)](https://travis-ci.org/diffplug/durian)
[![License](https://img.shields.io/badge/license-Apache-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))

# NOT YET SUITABLE FOR USE - we're releasing a formerly internal library, bear with us as we clean it up for public release

Guava has become indispensable for many Java developers.  Because of its wide adoption, it must be conservative regarding its minimum requirements.

Durian complements Guava with some features which are too spiky for Guava, such as:
* One-liner exception handling for Java 8 functional interfaces.
* A simple replacement for the mess of `PrintStream`, `OutputStream`, `Writer`, etc. when all you want is to pipe some strings around.
* TODO: Given a node in a tree, and a `Function<Node, List<Node>>`, create a `Stream` for traversing this tree (breadth-first, depth-first, etc.).
* An enum for handling comparisons in a pattern-matchey way.
* TODO: Guava's functional interface utilities (`Suppliers`, `Predicates`, etc.) converted to Java 8.

Durian's only requirement is Java 8 or greater, no other libraries needed (not even Guava).  It is published to JCenter at the maven coordinates `com.diffplug.durian:durian`.

## Known problems

* The `MANIFEST.MF` has a bunch of unneeded `VER_suchandsuch` properties.  They don't do any harm, but it would be nice to fix. [Github issue](https://github.com/TomDmitriev/gradle-bundle-plugin/issues/33)
* `gradlew format` is supposed to format the source code. For now, it doesn't work. [Github issue](https://github.com/youribonnaffe/gradle-format-plugin/issues/8)

## Acknowledgements

* Built by [gradle](http://gradle.org/).
* Tested by [junit](http://junit.org/).
* Bugs found by [findbugs](http://findbugs.sourceforge.net/).
* Bundled for OSGI by [gradle-bundle-plugin](https://github.com/TomDmitriev/gradle-bundle-plugin).
* Formatted by [gradle-format-plugin](https://github.com/youribonnaffe/gradle-format-plugin).
* License headered by [license-gradle-plugin](https://github.com/hierynomus/license-gradle-plugin).
* Artifacts hosted by [jcenter](https://bintray.com/bintray/jcenter) and uploaded by [gradle-bintray-plugin](https://github.com/bintray/gradle-bintray-plugin).
* `StringPrinter.toOutputStream()` borrows heavily from `WriterOutputStream`, inside Apache commons-io.
* `DurianPlugins` is inspired by RxJava's plugin mechanism.
