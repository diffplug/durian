#!/bin/bash

# Do the Gradle build
./gradlew build || exit 1

if [ "$TRAVIS_REPO_SLUG" == "diffplug/durian" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master-guava" ]; then
	# Publish the artifacts
	./gradlew publish || exit 1
	# Push the javadoc
	./gradlew publishGhPages || exit 1
fi
