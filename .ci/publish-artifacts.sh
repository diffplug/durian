#!/bin/bash

if [[ $(./gradlew -q printVersion) != *SNAPSHOT* ]]; then
	echo "Publishing snapshot artifacts..."
else
	echo "Publishing release artifacts..."
fi

./gradlew publish
RETVAL=$?

if [ $RETVAL -eq 0 ]; then
	echo "Publish artifacts successful."
else
	echo "Publish artifacts failed."
	exit 1
fi
