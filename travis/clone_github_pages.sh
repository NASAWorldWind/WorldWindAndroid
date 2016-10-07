#!/bin/bash

# This script clones the GitHub Pages website so it can be updated with the javadoc
# and test reports from the build. The GITHUB_API_KEY contains the GitHub personal
# access token for Travis CI. The git clone command is redirected to /dev/null to
# prevent leaking the access token into the log.

# Assert the GitHub Personal Access Token for the GitHub Pages
if [[ -z "$GITHUB_API_KEY" ]]; then
    echo $0 error: You must export the GITHUB_API_KEY containing the personal access token for Travis\; the repo was not cloned.
    exit 0
fi
# Assert the GitHub Pages repo to be updated.
if [[ -z "$GH_PAGES_REPO" ]]; then
    echo $0 error: You must export the GH_PAGES_REPO containing GitHub Pages URL sans protocol\; the repo was not cloned.
    exit 0
fi
# Assert the GitHub Pages local folder.
if [[ -z "$GH_PAGES_DIR" ]]; then
    echo $0 error: You must export GH_PAGES_DIR containing the folder name for the cloned GitHub Pages\; the repo was not cloned.
    exit 0
fi

echo Cloning $GH_PAGES_REPO to ${HOME}/${GH_PAGES_DIR}
cd $HOME

git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"

# Clone using --quiet and redirect to null to prevent leaking the API token
git clone --quiet --branch=master https://${GITHUB_API_KEY}@${GH_PAGES_REPO} $GH_PAGES_DIR > /dev/null

cd $TRAVIS_BUILD_DIR
