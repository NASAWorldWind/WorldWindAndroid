#!/bin/bash
# ======================================================================================================================
# Updates the GitHub Pages website with the updated javadoc from the build. Clones the GitHub Pages to the local
# filesystem, copies the new javadoc, then commits and pushes the changes.
#
# Uses Git to update tags in the repo. Git commands using authentication are redirected to /dev/null to prevent leaking
# the access token into the log.
# ======================================================================================================================

#!/bin/bash

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

# Assert the GitHub Pages repo is defined
if [[ -z "$GH_PAGES_REPO" ]]; then
    echo "$0 error: You must export the GH_PAGES_REPO containing GitHub Pages URL sans protocol\; the repo was not cloned."
    exit 1
fi

# Emit a log message for the javadoc update
echo "Updating JavaDoc at ${GH_PAGES_REPO}/assets/android/${TRAVIS_TAG}/javadoc"

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

# Initialize the FOLDER var predicated on the build configuration
RELEASES_URL="https://api.github.com/repos/nasaworldwind/worldwindandroid/releases"

# Update the release log to reflect all versions available
curl --silent -o ./assets/android/releases.json --header "Authorization: token ${GITHUB_API_KEY}" ${RELEASES_URL}

# Commit and push the changes (quietly)
git add -f .
git commit -m "Updated javadoc from successful travis build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"
git push -fq origin master > /dev/null