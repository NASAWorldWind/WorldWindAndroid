#!/bin/bash
# ======================================================================================================================
# Creates a changelog using the GitHub API and the milestone associated with the tag.
# ======================================================================================================================

set +x

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

# When GitHub has milestones associated with the release, assemble a Changelog file and post it to the release assets
if [[ "${#MILESTONE_ARRAY[@]}" -ne 0 ]]; then
    # Assemble Markdown headers for the Changelog file
    echo "#Changelog" >> CHANGELOG.md
    echo "##${TRAVIS_TAG}" >> CHANGELOG.md

    # Assemble Markdown list items for each milestone's associated issues
    for MILESTONE in "${MILESTONE_ARRAY[*]}"
    do
        ISSUE_ARRAY=$(curl --silent "${GITHUB_API_URL}/issues?state=all&milestone=${MILESTONE}" | jq -c '.[] | [.title, .number, .html_url]') >> /dev/null
        while read line
        do
            echo ${line} | sed 's#\["\(.*\)",\(.*\),"\(.*\)"\]#- \1 ([\#\2](\3))#' >> CHANGELOG.md
        done <<< "${ISSUE_ARRAY[*]}"
    done
fi
