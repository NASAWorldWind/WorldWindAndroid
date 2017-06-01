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

# Update the Bintray release log to reflect the most recent version available
curl --silent -o ./assets/android/latestBintrayVersion.json https://api.bintray.com/packages/nasaworldwind/maven/WorldWindAndroid/versions/_latest

# Configure the user to be associated with commits to the GitHub pages
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"

# Clone the GitHub Pages repository to the local filesystem
GH_PAGES_DIR=${HOME}/gh_pages
git clone --quiet --branch=master https://${GITHUB_API_KEY}@github.com/NASAWorldWind/NASAWorldWind.github.io.git $GH_PAGES_DIR > /dev/null
cd $GH_PAGES_DIR

curl --silent -o assets/android/latestOjoVersion.json "https://oss.jfrog.org/artifactory/api/search/versions?g=gov.nasa.worldwind.android&a=worldwind&repos=oss-snapshot-local"

# Do a quick check to make sure there wasn't an issue with the retrieval from the API
sudo apt-get install -qq jq
jq --version

OJO_CURRENT_VERSION=$(more assets/android/ojoVersionInformation.json | jq .results[0].version)

if [[ -z "$OJO_CURRENT_VERSION" ]] || [[ "$OJO_CURRENT_VERSION" == "null" ]]; then
    echo "The SNAPSHOT version request failed, no changes made to the GH pages repository"
else
    # Commit and push the changes (quietly)
    git add -f .
    git commit -m "Updated javadoc from successful travis build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"
    git push -fq origin master > /dev/null
fi