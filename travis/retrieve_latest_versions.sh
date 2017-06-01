#!/bin/bash
# ======================================================================================================================
# Updates the GitHub Pages website with the updated OJO SNAPSHOT and Bintray/JCenter version. Clones the GitHub Pages to
# the local filesystem, requests the new version information, then commits and pushes the changes.
#
# The version information is requested from the OJO and Bintray API's and saved to the GitHub Pages repository. The main
# site then consumes the information for displaying the latest information to visitors. The OJO and Bintray API's
# enforce the same origin policy which necessitates the server conducting the call rather than the page calling
# dynamically.
#
# Uses Git to update tags in the repo. Git commands using authentication are redirected to /dev/null to prevent leaking
# the access token into the log.
# ======================================================================================================================

set +x
set -e

# Assert the GitHub Personal Access Token exists
if [[ -z "$GITHUB_API_KEY" ]]; then
    echo "$0 error: You must export the GITHUB_API_KEY containing the personal access token for Travis\; the repo was not cloned."
    exit 1
fi

# Configure the user to be associated with commits to the GitHub pages
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"

# Clone the GitHub Pages repository to the local filesystem
GH_PAGES_DIR=${HOME}/gh_pages
git clone --quiet --depth=10 --branch=master https://${GITHUB_API_KEY}@github.com/NASAWorldWind/NASAWorldWind.github.io.git $GH_PAGES_DIR > /dev/null
cd $GH_PAGES_DIR

# Add jq for an initial evaluation of the API returns
sudo apt-get install -qq jq
jq --version

# Update the Bintray release log to reflect the latest version available
curl --silent -o ./assets/android/latestBintrayVersion.json "https://api.bintray.com/packages/nasaworldwind/maven/WorldWindAndroid/versions/_latest"

# Do a quick check to make sure there wasn't an issue with retrieval from the API
BINTRAY_CURRENT_VERSION=$(more ./assets/android/latestBintrayVersion.json | jq .name)

if [[ -z "$BINTRAY_CURRENT_VERSION" ]] || [[ "$BINTRAY_CURRENT_VERSION" == "null" ]]; then
    echo "The Bintray version API request failed, no changes made to the GH pages repository"
elif [[ `git status --porcelain` ]]; then
    # Update the Bintray version file
    echo "Updating the Bintray version file..."
    git add ./assets/android/latestBintrayVersion.json
    git commit -m "Updated the Bintray version from successful API call in build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"
    git push -fq origin master > /dev/null
else
    echo "There were no changes to the Bintray version"
fi

# Update the OJO release log to reflect the latest version available
curl --silent -o ./assets/android/latestOjoVersion.json "https://oss.jfrog.org/artifactory/api/search/versions?g=gov.nasa.worldwind.android&a=worldwind&repos=oss-snapshot-local"

# Do a quick check to make sure there wasn't an issue with the retrieval from the API
OJO_CURRENT_VERSION=$(more assets/android/latestOjoVersion.json | jq .results[0].version)

if [[ -z "$OJO_CURRENT_VERSION" ]] || [[ "$OJO_CURRENT_VERSION" == "null" ]]; then
    echo "The SNAPSHOT version API request failed, no changes made to the GH pages repository"
elif [[ `git status --porcelain` ]]; then
    # Update the OJO version file
    echo "Updating the OJO version file..."
    git add ./assets/android/latestOjoVersion.json
    git commit -m "Updated SNAPSHOT version from successful API call in build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"
    git push -fq origin master > /dev/null
else
    echo "There were no changes to the OJO version"
fi