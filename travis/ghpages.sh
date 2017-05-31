#!/bin/bash
# ======================================================================================================================
# Updates the GitHub Pages website with the updated javadoc from the build. Clones the GitHub Pages to the local
# filesystem, copies the new javadoc, then commits and pushes the changes.
#
# Uses Git to update tags in the repo. Git commands using authentication are redirected to /dev/null to prevent leaking
# the access token into the log.
# ======================================================================================================================

set +x
set -e

echo '[ghpages] TRAVIS_TAG='$TRAVIS_TAG
echo '[ghpages] TRAVIS_BRANCH='$TRAVIS_BRANCH
echo '[ghpages] TRAVIS_PULL_REQUEST='$TRAVIS_PULL_REQUEST

echo "Updating the GitHub pages repository and site with latest build documentation..."

# Assert the GitHub Personal Access Token exists
if [[ -z "$GITHUB_API_KEY" ]]; then
    echo "$0 error: You must export the GITHUB_API_KEY containing the personal access token for Travis\; the repo was not cloned."
    exit 1
fi

# Emit a log message for the javadoc update
echo "Updating JavaDoc at assets/android/${TRAVIS_TAG}/javadoc"

# Configure the user to be associated with commits to the GitHub pages
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"

# Clone the GitHub Pages repository to the local filesystem
GH_PAGES_DIR=${HOME}/gh_pages
git clone --quiet --branch=master https://${GITHUB_API_KEY}@github.com/NASAWorldWind/NASAWorldWind.github.io.git $GH_PAGES_DIR > /dev/null
cd $GH_PAGES_DIR

# Copy new javadocs to the repository
mkdir -p ./assets/android/${TRAVIS_TAG}/javadoc
cp -Rf ${TRAVIS_BUILD_DIR}/worldwind/build/outputs/doc/javadoc/* ./assets/android/${TRAVIS_TAG}/javadoc

# Release version information - builds JSON file for relaying documentation versioning to the website
JSON_DATA="{ \
      \"tag\": \"${TRAVIS_TAG}\" \
    }"
echo $JSON_DATA > ./assets/android/latestTag.json

# Update the Bintray release log to reflect the most recent version available
curl --silent -o ./assets/android/latestBintrayVersion.json https://api.bintray.com/packages/nasaworldwind/maven/WorldWindAndroid/versions/_latest

# Commit and push the changes (quietly)
git add -f .
git commit -m "Updated javadoc from successful travis build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"
git push -fq origin master > /dev/null