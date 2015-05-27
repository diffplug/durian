#!/bin/bash
# This script initiates publishing the proper Javadoc page whenever pushes to master occur.
# NOTE: Travis-CI can only publish SNAPSHOT versions.
if [ "$TRAVIS_REPO_SLUG" == "diffplug/durian" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then

	echo "Publishing javadoc..."

	# save the version
	VERSION=$(./gradlew -q printVersion)

	# copy javadoc from the build to ~/javadoc-temp
	cp -R build/docs/javadoc $HOME/javadoc-temp

	# clone a new copy durian to gh-pages
	cd $HOME
	git config --global user.email "travis@travis-ci.org"
	git config --global user.name "travis-ci"
	rm -rf $HOME/gh-pages
	git clone --quiet --branch=gh-pages https://${gh_token}@github.com/diffplug/durian gh-pages > /dev/null

	# copy the javadoc into the build
	cd gh-pages
	if [[ "$VERSION" != *SNAPSHOT* ]]; then
		git rm -rf javadoc/${VERSION}
		cp -Rf $HOME/javadoc-temp/ ./javadoc/${VERSION}/
	else
		git rm -rf javadoc/latest
		cp -Rf $HOME/javadoc-temp/ ./javadoc/latest/
	fi

	# add all of the stuff and commit it
	git add -f -A
	git commit -m "Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
	if ! git push -fq origin gh-pages &> /dev/null; then
		echo "Error pushing gh-pages to origin. Bad gh_token? GitHub down?"
	else
		echo "Published javadoc to gh-pages."
	fi
fi
