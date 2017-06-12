#!/bin/bash

# Writes a change log to stdout using the GitHub API and the milestone associated with the specified Git tag
function changelog() {

    # Give function arguments meaningful names
    GIT_TAG="$1"
    GITHUB_API_URL="$2"

    # Write Markdown headers for the Git tag and release date
    echo "##${GIT_TAG}"
    echo "######Released on $(date '+%Y-%m-%d')"

    # Query GitHub API for a milestone matching the Git tag
    MILESTONE_ARRAY=( \
        $(curl --silent "${GITHUB_API_URL}/milestones?state=all" \
        | jq --arg title "${GIT_TAG}" '.[] | select(.title == $title) | .number') \
        ) > /dev/null

    # When GitHub has a milestone matching the Git tag, write milestone related information to the change log
    if [[ "${#MILESTONE_ARRAY[@]}" -ne 0 ]]; then
        # Write the milestone description to the change log
        MILESTONE_DESCRIPTION=( \
            $(curl --silent "${GITHUB_API_URL}/milestones/${MILESTONE_ARRAY[0]}" \
            | jq .description) \
            ) > /dev/null
        MILESTONE_DESCRIPTION=${MILESTONE_DESCRIPTION[*]#\"}
        echo ${MILESTONE_DESCRIPTION[*]%\"}

        # Write the milestone's associated issues to the change log with each issue on a separate line with the issue
        # title, issue number, and a link to the issue on GitHub.com, all in markdown format: .title ([#.number](.html_url))
        ISSUE_ARRAY=$(curl --silent "${GITHUB_API_URL}/issues?state=all&milestone=${MILESTONE_ARRAY[0]}" | jq -c '.[] | [.title, .number, .html_url]') >> /dev/null
        while read line
        do
            echo ${line} | sed 's#\["\(.*\)",\(.*\),"\(.*\)"\]#- \1 ([\#\2](\3))#'
        done <<< "${ISSUE_ARRAY[*]}"
    fi
}
