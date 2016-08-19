# Contributing to Durian-collect

Pull requests are welcome, preferably against `master`.

Every successful Travis CI build on branch `master` is automatically published to [`https://oss.sonatype.org/content/repositories/snapshots`](https://oss.sonatype.org/content/repositories/snapshots/com/diffplug/), and its javadoc are published [here](http://diffplug.github.io/durian-collect/javadoc/snapshot/).

## Build instructions

The easiest way to work on Spotless is to run `gradlew ide`.  It will download and setup an Eclipse IDE for you, using [goomph](https://github.com/diffplug/goomph).

It's a bog-standard gradle build.

`gradlew eclipse`
* creates an Eclipse project file for you.

`gradlew build`
* builds the jar
* runs FindBugs
* checks the formatting
* runs the tests

If you're getting style warnings, `gradlew spotlessApply` will apply anything necessary to fix formatting. For more info on the formatter, check out [spotless](https://github.com/diffplug/spotless).

## License

By contributing your code, you agree to license your contribution under the terms of the APLv2: https://github.com/diffplug/durian/blob/master/LICENSE

All files are released with the Apache 2.0 license as such:

```
Original Guava code is copyright (C) 2015 The Guava Authors.
Modifications from Guava are copyright (C) 2015 DiffPlug.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
