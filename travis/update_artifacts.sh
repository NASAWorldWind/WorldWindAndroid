#!/bin/bash

# This script uploads build artifacts the GitHub Releases via the GitHub REST API.
# The script performs the following CRUD operations: POST, GET, PATCH, and DELETE.
# The script uses jq to process the REST API JSON results. jq should be installed
# before this script is executed, e.g., before_script: sudo apt-get install -qq jq
#
# Syntax: update_artifacts.sh

# GitHub RESTful API URLs
RELEASES_URL="https://api.github.com/repos/nasaworldwind/worldwindandroid/releases"
UPLOADS_URL="https://uploads.github.com/repos/nasaworldwind/worldwindandroid/releases"
DOWNLOADS_URL="https://github.com/nasaworldwind/WorldWindAndroid/releases/download"

# Initialize the release variables predicated on the tag and branch.
# Note: In the release notes markdown, don't allow preceding spaces
# on new lines else GitHub doesn't format the text correctly.
if [[ -n $TRAVIS_TAG ]]; then
    echo Tagged Release $TRAVIS_TAG
    PRERELEASE="false"
    RELEASE_TAG=$TRAVIS_TAG
    RELEASE_BRANCH="master"
    RELEASE_NOTES="World Wind Android ${RELEASE_TAG} adds new features, enhancements and bug fixes."
elif [[ "$TRAVIS_BRANCH" == "master" ]]; then
    PRERELEASE="true"
    RELEASE_TAG="production"
    RELEASE_BRANCH="master"
    RELEASE_NOTES="World Wind Android builds from the [master](https://github.com/NASAWorldWind/WorldWindAndroid/tree/master) branch."
    RELEASE_NOTES+="<br/>Production builds with new features, enhancements and bug fixes ready for official release."
elif [[ "$TRAVIS_BRANCH" == "develop" ]]; then
    PRERELEASE="true"
    RELEASE_TAG="development"
    RELEASE_BRANCH="develop"
    RELEASE_NOTES="World Wind Android builds from the [develop](https://github.com/NASAWorldWind/WorldWindAndroid/tree/develop) branch."
    RELEASE_NOTES+="Development builds with the newest, bleeding-edge World Wind Android features. Intended for developers and early adopters."
else
    echo Skipping $TRAVIS_BRANCH branch
    exit 0  # Exit quietly when a feature branch is built
fi

# Query the release id for the given tag
echo Extracting release id for $RELEASE_TAG
RELEASE_ID=$(curl ${RELEASES_URL} | jq --arg tagname $RELEASE_TAG '.[] | select(.tag_name == $tagname) | .id')

# Create the release if it doesn't exist (if zero-length release id)
if [[ ${#RELEASE_ID} -eq 0 ]]; then
    echo Creating $TRAVIS_TAG Release
    IS_NEW_RELEASE=0 # true
    # Build the JSON (Note: single quotes inhibit variable substitution, must use double quotes)
    JSON_DATA="{ \
      \"tag_name\": \"${RELEASE_TAG}\", \
      \"target_commitish\": \"${RELEASE_BRANCH}\", \
      \"name\": \"${RELEASE_TAG}\", \
      \"body\": \"${RELEASE_NOTES}\", \
      \"draft\": false, \
      \"prerelease\": ${PRERELEASE} \
    }"

    # Create the release
    RELEASE=$(curl \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/vnd.android.package-archive" \
    --header "Accept: application/json" \
    --data "${JSON_DATA}" \
    --request POST ${RELEASES_URL})

    # Extract the release id from the JSON result
    RELEASE_ID=$(echo $RELEASE | jq '.id')
fi

# Assert that we found a GitHub release id for the current branch (release id length > 0)
if [[ ${#RELEASE_ID} -eq 0 ]]; then
    echo $0 error: The $RELEASE_TAG tag was not found\; no artifacts were uploaded to GitHub releases.
    exit 0
fi

# Define locations for assets
LIBRARIES_PATH="worldwind/build/outputs/aar"
EXAMPLES_PATH="worldwind-examples/build/outputs/apk"
TUTORIALS_PATH="worldwind-tutorials/build/outputs/apk"
JAVADOC_PATH="worldwind/build/outputs/doc"

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
JAVADOC_FILES=( worldwind-javadoc.zip )
ALL_FILES=( "${LIBRARY_FILES[@]}"  "${EXAMPLE_FILES[@]}" "${TUTORIAL_FILES[@]}" "${JAVADOC_FILES[@]}" )

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

for FILENAME in ${JAVADOC_FILES[*]}
do
    curl -include \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/zip" \
    --header "Accept: application/json" \
    --data-binary @${TRAVIS_BUILD_DIR}/${JAVADOC_PATH}/${FILENAME} \
    --request POST ${UPLOADS_URL}/${RELEASE_ID}/assets?name=${FILENAME}
done