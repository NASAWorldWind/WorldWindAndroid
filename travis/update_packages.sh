#!/bin/bash

# This script updates the GitHub Pages website with the updated APK packages and AAR libraries from the build.
# The packages are updated only if we're building the master or develop branch, not
# feature branches.
#
# Syntax: update_packages.sh

# Assert the GitHub Pages local folder is defined and exists.
if [[ -z "$GH_PAGES_DIR" ]]; then
    echo $0 error: You must export GH_PAGES_DIR containing the folder name for the cloned GitHub Pages\; the repo was not updated.
    exit 0
elif [[ ! -d ${HOME}/${GH_PAGES_DIR} ]]; then
    echo $0 error: The GitHub Pages folder does not exist\; the repo was not updated.
    exit 0
fi

# Assert the branch is master or develop.
if ! [[ "$TRAVIS_BRANCH" == "master" || "$TRAVIS_BRANCH" == "develop" ]]; then
    # Exit quietly when a feature branch is built
    exit 0
fi

# Packages are stored at ./assets/android/TRAVIS_BRANCH/*.[aar|apk]
echo Updating ${GH_PAGES_REPO}/assets/android/${TRAVIS_BRANCH}/*.[aar|apk]
cd ${HOME}/${GH_PAGES_DIR}

git rm -rfq --ignore-unmatch ./assets/android/${TRAVIS_BRANCH}/*.aar
git rm -rfq --ignore-unmatch ./assets/android/${TRAVIS_BRANCH}/*.apk

mkdir -p ./assets/android/${TRAVIS_BRANCH}/
cp -Rf ${TRAVIS_BUILD_DIR}/worldwind/build/outputs/aar/*.aar ./assets/android/${TRAVIS_BRANCH}
cp -Rf ${TRAVIS_BUILD_DIR}/worldwind-examples/build/outputs/apk/*.apk ./assets/android/${TRAVIS_BRANCH}
cp -Rf ${TRAVIS_BUILD_DIR}/worldwind-tutorials/build/outputs/apk/*.apk ./assets/android/${TRAVIS_BRANCH}

git add -f .
git commit -m "Updated Android packages from successful travis build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"

# Push the changes to GitHub
git push -fq origin master > /dev/null

cd $TRAVIS_BUILD_DIR
