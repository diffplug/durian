# <img align="left" src="durian.png"> Durian: [Guava](https://github.com/google/guava)'s spikier (unofficial) cousin

<!---freshmark shields
output = [
	link(shield('Maven artifact', 'mavenCentral', '{{group}}:{{name}}', 'blue'), 'https://bintray.com/{{org}}/opensource/{{name}}/view'),
	link(shield('Latest version', 'latest', '{{stable}}', 'blue'), 'https://github.com/{{org}}/{{name}}/releases/latest'),
	link(shield('Javadoc', 'javadoc', 'OK', 'blue'), 'https://{{org}}.github.io/{{name}}/javadoc/{{stable}}/'),
	link(shield('License Apache', 'license', 'Apache', 'blue'), 'https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)'),
	'',
	link(shield('Changelog', 'changelog', '{{version}}', 'brightgreen'), 'CHANGES.md'),
	link(image('Travis CI', 'https://travis-ci.org/{{org}}/{{name}}.svg?branch=master'), 'https://travis-ci.org/{{org}}/{{name}}'),
	link(shield('Live chat', 'gitter', 'live chat', 'brightgreen'), 'https://gitter.im/diffplug/durian')
	].join('\n');
-->
[![Maven artifact](https://img.shields.io/badge/mavenCentral-com.diffplug.durian%3Adurian-blue.svg)](https://bintray.com/diffplug/opensource/durian/view)
[![Latest version](https://img.shields.io/badge/latest-3.4.0-blue.svg)](https://github.com/diffplug/durian/releases/latest)
[![Javadoc](https://img.shields.io/badge/javadoc-OK-blue.svg)](https://diffplug.github.io/durian/javadoc/3.4.0/)
[![License Apache](https://img.shields.io/badge/license-Apache-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))

[![Changelog](https://img.shields.io/badge/changelog-3.5.0--SNAPSHOT-brightgreen.svg)](CHANGES.md)
[![Travis CI](https://travis-ci.org/diffplug/durian.svg?branch=master)](https://travis-ci.org/diffplug/durian)
[![Live chat](https://img.shields.io/badge/gitter-live_chat-brightgreen.svg)](https://gitter.im/diffplug/durian)
<!---freshmark /shields -->

Guava has become indispensable for many Java developers.  Because of its wide adoption, it must be conservative regarding its minimum requirements.

<!---freshmark javadoc
output = prefixDelimiterReplace(input, 'https://{{org}}.github.io/{{name}}/javadoc/', '/', stable);
-->
Durian complements Guava with some features which are too spiky for Guava, such as:
* [One-liner exception handling](test/com/diffplug/common/base/ErrorsExample.java?ts=4) for Java 8 functional interfaces (even with checked exceptions).
* A [simple replacement](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/StringPrinter.html) for the mess of `PrintStream`, `OutputStream`, `Writer`, etc. when all you want is to pipe some strings around.
* Given a node in a tree, and a [`Function<Node, List<Node>>`](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/TreeDef.html), create a `Stream` for [traversing](test/com/diffplug/common/base/TreeStreamTest.java?ts=4) this tree (breadth-first, depth-first, etc.).
* An [enum for handling comparisons](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/Comparison.html) in a pattern-matchey way.
* Guava's [`Suppliers`](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/Suppliers.html),
[`Predicates`](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/Predicates.html),
and [`Functions`](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/Functions.html) converted to Java 8,
and a new [`Consumers`](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/Consumers.html) class to round it out.
* A few other carefully-curated Java 8 goodies:
	+ [Box and Box.Nullable](src/com/diffplug/common/base/Box.java?ts=4)
	+ [Either](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/Either.html)
	+ [StackDumper](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/StackDumper.html)
	+ [MoreCollectors](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/MoreCollectors.html)
	+ [FieldsAndGetters](https://diffplug.github.io/durian/javadoc/3.4.0/com/diffplug/common/base/FieldsAndGetters.html)

<!---freshmark /javadoc -->

Durian's only requirement is Java 8 or greater, no other libraries are needed (not even Guava).

Contributions are welcome, see [the contributing guide](CONTRIBUTING.md) for development info.

## Related

If you have a `Box`, but you'd like to subscribe to changes in its value, you should look at `RxBox` in [DurianRx](https://github.com/diffplug/durian-rx).

## Acknowledgements

* The API and tests for `Suppliers`, `Functions`, and `Predicates` are all verbatim from [Guava](https://github.com/google/guava).
* `StringPrinter.toOutputStream()` borrows heavily from `WriterOutputStream`, inside Apache commons-io.
* `DurianPlugins` is inspired by RxJava's plugin mechanism.
* Formatted by [spotless](https://github.com/diffplug/spotless), [as such](https://github.com/diffplug/durian/blob/v2.0/build.gradle?ts=4#L70-L90).
* Bugs found by [findbugs](http://findbugs.sourceforge.net/), [as such](https://github.com/diffplug/durian/blob/v2.0/build.gradle?ts=4#L92-L116).
* OSGi metadata generated by JRuyi's [osgibnd-gradle-plugin] (https://github.com/jruyi/osgibnd-gradle-plugin), which leverages Peter Kriens' [bnd](http://www.aqute.biz/Bnd/Bnd).
* Scripts in the `.ci` folder are inspired by [Ben Limmer's work](http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/).
* Built by [gradle](http://gradle.org/).
* Tested by [junit](http://junit.org/).
* Maintained by [DiffPlug](http://www.diffplug.com/).
