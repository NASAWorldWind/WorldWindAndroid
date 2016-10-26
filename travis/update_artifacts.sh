#!/bin/bash

# This script uploads build artifacts the GitHub Releases via the GitHub REST API.
# The script uses jq to process the REST API JSON results. jq should be installed
# before this script is executed, e.g., before_script: sudo apt-get install -qq jq
#
# Syntax: update_artifacts.sh

# GitHub RESTful API URLs
RELEASES_URL="https://api.github.com/repos/nasaworldwind/worldwindandroid/releases"
UPLOADS_URL="https://uploads.github.com/repos/nasaworldwind/worldwindandroid/releases"

# Get the GitHub release id used for the master or develop branch; skip feature branches.
# Also create the description that will be used to amend the release
if [ "$TRAVIS_BRANCH" == "master" ]; then
    RELEASE_TAG="stable"
    # Get the id of the Stable Artifacts release which is associated with the 'stable' tag
    RELEASE_ID=$(curl ${RELEASES_URL} | jq '.[] | select(.tag_name == "stable") | .id')
    # Generate the release description (don't allow preceding spaces on new lines)
    RELEASE_NOTES="## Build artifacts from the master branch.\r\n\r\n\
The World Wind Development Team uploads the latest build artifacts \
from the *master* branch here--prior to generating an 'official' tagged release. \
Feel free to download these artifacts and preview the next release.\r\n\r\n"
elif [ "$TRAVIS_BRANCH" == "develop" ]; then
    RELEASE_TAG="development"
    # Get the id of the Development Artifacts release which is associated with the 'development' tag
    RELEASE_ID=$(curl ${RELEASES_URL} | jq '.[] | select(.tag_name == "development") | .id')
    # Generate the release description (Note: don't allow preceeding spaces on new lines)
    RELEASE_NOTES="## Build artifacts from the develop branch.\r\n\r\n\
The World Wind Development Team uploads the latest build artifacts \
from the *develop* branch here--to download, test and install on devices. \
These artifacts are in a 'beta' state and should not be used for production.\r\n\r\n"
else
    # Exit quietly when a feature branch is built
    exit 0
fi

# Assert that we found a GitHub release id for the current branch
if [[ -z "$RELEASE_ID" ]]; then
    echo $0 error: A stable or development tag was not found\; no artifacts were uploaded to GitHub releases.
    exit 0
fi

# Define the assets that we'll be processing
LIBRARY_FILES=( \
    worldwind-debug.aar \
    worldwind-release.aar)
EXAMPLE_FILES=( \
    worldwind-examples-debug.apk \
    worldwind-examples-release.apk )
TUTORIAL_FILES=( \
    worldwind-tutorials-debug.apk \
    worldwind-tutorials-release.apk)
ALL_FILES=( "${LIBRARY_FILES[@]}"  "${EXAMPLE_FILES[@]}" "${TUTORIAL_FILES[@]}" )

# Remove the old assets if they exist (asset id length > 0)
for FILENAME in ${ALL_FILES[*]}
do
    # Note, we're using the jq "--arg name value" commandline option to create a predefined filename variable
    ASSET_ID=$(curl ${RELEASES_URL}/${RELEASE_ID}/assets | jq --arg filename $FILENAME '.[] | select(.name == $filename) | .id')
    if [ ${#ASSET_ID} -gt 0 ]; then
        echo DELETE ${RELEASES_URL}/assets/${ASSET_ID}
        curl -include --header "Authorization: token ${GITHUB_API_KEY}" --request DELETE ${RELEASES_URL}/assets/${ASSET_ID}
    fi
done

# Upload the new assets
LIBRARIES_PATH="worldwind/build/outputs/aar"
EXAMPLES_PATH="worldwind-examples/build/outputs/apk"
TUTORIALS_PATH="worldwind-tutorials/build/outputs/apk"

for FILENAME in ${LIBRARY_FILES[*]}
do
    curl -include \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/vnd.android.package-archive" \
    --header "Accept: application/json" \
    --data-binary @${TRAVIS_BUILD_DIR}/${LIBRARIES_PATH}/${FILENAME} \
    --request POST ${UPLOADS_URL}/${RELEASE_ID}/assets?name=${FILENAME}
done

for FILENAME in ${EXAMPLE_FILES[*]}
do
    curl -include \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/vnd.android.package-archive" \
    --header "Accept: application/json" \
    --data-binary @${TRAVIS_BUILD_DIR}/${EXAMPLES_PATH}/${FILENAME} \
    --request POST ${UPLOADS_URL}/${RELEASE_ID}/assets?name=${FILENAME}
done

for FILENAME in ${TUTORIAL_FILES[*]}
do
    curl -include \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/vnd.android.package-archive" \
    --header "Accept: application/json" \
    --data-binary @${TRAVIS_BUILD_DIR}/${TUTORIALS_PATH}/${FILENAME} \
    --request POST ${UPLOADS_URL}/${RELEASE_ID}/assets?name=${FILENAME}
done


# Build up the release notes with download links and metadata. Check the source file for existence
if [ -s ${TRAVIS_BUILD_DIR}/${EXAMPLES_PATH}/worldwind-examples-release.apk ]; then
    RELEASE_NOTES+="#### [Download the Examples App](https://github.com/nasaworldwind/WorldWindAndroid/releases/download/${RELEASE_TAG}/worldwind-examples-release.apk)\r\n"
fi
if [ -s ${TRAVIS_BUILD_DIR}/${TUTORIALS_PATH}/worldwind-tutorials-release.apk ]; then
    RELEASE_NOTES+="#### [Download the Tutorials App](https://github.com/nasaworldwind/WorldWindAndroid/releases/download/${RELEASE_TAG}/worldwind-tutorials-release.apk)\r\n"
fi
if [ -s ${TRAVIS_BUILD_DIR}/${LIBRARIES_PATH}/worldwind-release.aar ]; then
    RELEASE_NOTES+="#### [Download the Library](https://github.com/nasaworldwind/WorldWindAndroid/releases/download/${RELEASE_TAG}/worldwind-release.aar)\r\n"
fi
RELEASE_DATE=$(date)
RELEASE_NOTES+="#### [Download the Source](https://github.com/nasaworldwind/WorldWindAndroid/archive/${TRAVIS_COMMIT}.zip)\r\n\
_Note: the 'Source code' links in the Downloads section below are empty, use the link above._\r\n\r\n\
### Built ${RELEASE_DATE} from commit ${TRAVIS_COMMIT}"

# Update the release description (Note: in --data, single quotes inhibit variable substitution, must use double quotes)
curl -include \
--header "Authorization: token ${GITHUB_API_KEY}" \
--data "{\"body\": \"${RELEASE_NOTES}\"}" \
--request PATCH ${RELEASES_URL}/${RELEASE_ID}
