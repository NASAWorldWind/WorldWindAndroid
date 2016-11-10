#!/bin/bash

# This script updates the GitHub Pages website with the updated JavaDoc from the build.
# The JavaDocs are updated only if we're building the master or develop branch, not
# feature branches. JavaDocs from the develop branch are built daily via a cron job,
# not on push events.
#
# Syntax: update_javadoc.sh

# Assert the GitHub Pages local folder is defined and exists.
if [[ -z "$GH_PAGES_DIR" ]]; then
    echo $0 error: You must export GH_PAGES_DIR containing the folder name for the cloned GitHub Pages\; the repo was not updated.
    exit 0
elif [[ ! -d ${HOME}/${GH_PAGES_DIR} ]]; then
    echo $0 error: The GitHub Pages folder does not exist\; the repo was not updated.
    exit 0
fi

# Assert the branch is master or develop, and that develop is updated only in a cron job.
if ! [[ "$TRAVIS_BRANCH" == "master" || "$TRAVIS_BRANCH" == "develop" ]]; then
    # Exit quietly when a feature branch is built
    exit 0
elif [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_EVENT_TYPE" != "cron" ]]; then
    # Exit quietly when the develop branch is built via a "push" event
    exit 0
fi

# JavaDocs are stored at ./assets/android/TRAVIS_BRANCH/javadoc
echo Updating ${GH_PAGES_REPO}/assets/android/${TRAVIS_BRANCH}/javadoc
cd ${HOME}/${GH_PAGES_DIR}

git rm -rfq --ignore-unmatch ./assets/android/${TRAVIS_BRANCH}/javadoc

mkdir -p ./assets/android/${TRAVIS_BRANCH}/javadoc
cp -Rf ${TRAVIS_BUILD_DIR}/worldwind/build/outputs/doc/javadoc ./assets/android/${TRAVIS_BRANCH}

git add -f .
git commit -m "Updated Android javadoc from successful travis build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"

# Push the changes to GitHub
git push -fq origin master > /dev/null

cd $TRAVIS_BUILD_DIR
