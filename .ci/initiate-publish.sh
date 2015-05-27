#!/bin/bash
# This script initiates the Gradle publishing task when pushes to master occur.
# NOTE: Travis-CI can only publish SNAPSHOT versions.
if [ "$TRAVIS_REPO_SLUG" == "diffplug/durian" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
	if [[ $(./gradlew -q printVersion) != *SNAPSHOT* ]]; then
		echo -e "Publishing snapshot\n"
	else
		echo -e "Publishing release\n"
	fi

	./gradlew publish
	RETVAL=$?

	if [ $RETVAL -eq 0 ]; then
		echo 'Completed publish!'
	else
		echo 'Publish failed.'
		return 1
	fi
fi
