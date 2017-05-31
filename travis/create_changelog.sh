#!/bin/bash
# ======================================================================================================================
# Creates a changelog using the GitHub API and the milestone associated with the tag.
# ======================================================================================================================

set +x

# Assemble Markdown headers for the Changelog file
echo "#Changelog" >> CHANGELOG.md
echo "##${TRAVIS_TAG}" >> CHANGELOG.md
FORMATTED_DATE=$(date '+%Y-%b-%d')
echo "###${FORMATTED_DATE}" >> CHANGELOG.md

# Install jq to filter GitHub API results
sudo apt-get install -qq jq
jq --version

# GitHub RESTful API URLs
GITHUB_API_URL="https://api.github.com/repos/nasaworldwind/worldwindandroid"

# Query GitHub for a milestone matching the Git tag
MILESTONE_ARRAY=( \
    $(curl --silent "${GITHUB_API_URL}/milestones?state=all" \
    | jq --arg title "${TRAVIS_TAG}" '.[] | select(.title == $title) | .number') \
    ) > /dev/null

# Write the milestone description into the Changelog
MILESTONE_DESCRIPTION=( \
    $(curl --silent "${GITHUB_API_URL}/milestones/${MILESTONE_ARRAY[0]}" \
    | jq .description) \
    ) > /dev/null
MILESTONE_DESCRIPTION=${MILESTONE_DESCRIPTION[*]#\"}
echo ${MILESTONE_DESCRIPTION[*]%\"} >> CHANGELOG.md

# Write instruction for how to get the library from JCenter
echo "###Use via JCenter and Gradle" >> CHANGELOG.md
echo "\`\`\`groovy" >> CHANGELOG.md
echo "compile: \"gov.nasa.worldwind.andriod:worldwind:${TRAVIS_TAG}@aar\"" >> CHANGELOG.md
echo "\`\`\`" >> CHANGELOG.md
echo "The JCenter repository must be specified in your project (Android Studio does this as a default)." >> CHANGELOG.md

# When GitHub has milestones associated with the release, assemble a Changelog file and post it to the release assets
if [[ "${#MILESTONE_ARRAY[@]}" -ne 0 ]]; then
    # Assemble Markdown list items for each milestone's associated issues
    ISSUE_ARRAY=$(curl --silent "${GITHUB_API_URL}/issues?state=all&milestone=${MILESTONE_ARRAY[0]}" | jq -c '.[] | [.title, .number, .html_url]') >> /dev/null
    while read line
    do
        echo ${line} | sed 's#\["\(.*\)",\(.*\),"\(.*\)"\]#- \1 ([\#\2](\3))#' >> CHANGELOG.md
    done <<< "${ISSUE_ARRAY[*]}"
fi