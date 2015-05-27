#!/bin/bash

# Do the Gradle build
./gradlew build

if [ "$TRAVIS_REPO_SLUG" == "diffplug/durian" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
	# Publish the artifacts
	./.ci/publish-artifacts.sh
	# Push the javadoc
	./.ci/push-javadoc.sh
fi
