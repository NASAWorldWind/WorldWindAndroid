#!/bin/bash

# This script updates the GitHub Pages website with the lint output and test reports from the build.
#
# Syntax: update_reports.sh

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
    # Exit quietly when a feature branch is built.
    exit 0
fi

# Reports are stored at ./assets/android/TRAVIS_BRANCH/reports
echo Updating ${GH_PAGES_REPO}/assets/android/${TRAVIS_BRANCH}/reports
cd ${HOME}/${GH_PAGES_DIR}

git rm -rfq --ignore-unmatch ./assets/android/${TRAVIS_BRANCH}/reports

mkdir -p ./assets/android/${TRAVIS_BRANCH}/reports
cp -Rf ${TRAVIS_BUILD_DIR}/worldwind/build/reports ./assets/android/${TRAVIS_BRANCH}
cp -f ${TRAVIS_BUILD_DIR}/worldwind/build/outputs/*.html ./assets/android/${TRAVIS_BRANCH}/reports

git add -f .
git commit -m "Updated Android lint output and test reports from travis build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"

# Push the changes to GitHub (use quiet and redirect used to prevent leaking the API token)
git push -fq origin master > /dev/null

cd $TRAVIS_BUILD_DIR
