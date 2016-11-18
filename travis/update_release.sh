#!/bin/bash
# ======================================================================================================================
# For Travis builds triggered by a "tag", this script creates or updates a GitHub release and uploads release artifacts.
#
# The script uses "curl" to performs CRUD operations on the GitHub Repos/Releases REST API.
# The script uses "jq" to process the REST API JSON results.
#   jq should be installed before this script is executed, e.g., in the .travis.yml "before_script:" block.
#       sudo apt-get install -qq jq
#   For jq documentation, see https://stedolan.github.io/jq/manual/
# The script uses "git" to update tags in the repo.
#
# Note: Commands using authentication are redirected to /dev/null to prevent leaking the access token into the log.
#
# Syntax: update_release.sh
# ======================================================================================================================
# Ensure shell commands are not echoed in the log to prevent leaking the access token.
set +x

# Ensure the GitHub Personal Access Token for GitHub exists
if [[ -z "$GITHUB_API_KEY" ]]; then
    echo $0 error: You must export the GITHUB_API_KEY containing the personal access token for Travis\; GitHub was not updated.
    exit 1
fi

# GitHub RESTful API URLs
RELEASES_URL="https://api.github.com/repos/nasaworldwind/worldwindjava/releases"
TAGS_URL="https://api.github.com/repos/nasaworldwind/worldwindjava/tags"
UPLOADS_URL="https://uploads.github.com/repos/nasaworldwind/worldwindjava/releases"
DOWNLOADS_URL="https://github.com/nasaworldwind/WorldWindJava/releases/download"

# Get the GitHub remote origin URL.
REMOTE_URL=$(git config --get remote.origin.url) > /dev/null
# Add the GitHub authentication token if it's not already embedded in the URL (test if an "@" sign is present).
if [[ $REMOTE_URL != *"@"* ]]; then
    # Use the stream editor to inject the GitHub authentication token into the remote url after the protocol.
    # Example:  https://github.com/.../repo.git -> https://<token>@github.com/.../repo.git
    REMOTE_URL=$(echo $REMOTE_URL | sed -e "s#://#://$GITHUB_API_KEY@#g") > /dev/null
fi

# Human readable release timestamp used in release notes
RELEASE_DATE=$(date '+%a, %b %e, %Y at %H:%M %Z')

# Initialize the release variables predicated on the tag. Note, on a tagged build, Travis cloned the repo into a branch
# named with the tag, thus the TRAVIS_BRANCH var reflects the tag name, not "master" or "develop" like you might expect.
if [[ -n $TRAVIS_TAG ]]; then
    if [[ "${TRAVIS_TAG}" == "daily"* ]]; then
        # Variables used to publish a "daily" release
        echo Preparing the $TRAVIS_TAG daily release
        RELEASE_TAG=$TRAVIS_TAG
        RELEASE_NAME="Daily Build"
        RELEASE_NOTES="World Wind Java daily builds from the [develop](https://github.com/NASAWorldWind/WorldWindJava/tree/develop) branch. "
        RELEASE_NOTES+="Daily builds have the newest, bleeding-edge World Wind Java SDK features. Intended for developers and early adopters."
        RELEASE_NOTES+="\r\n\r\nBuilt on ${RELEASE_DATE}."
        DRAFT="false"
        PRERELEASE="true"
    else
        # Variables to prepare a draft release
        echo Preparing the $TRAVIS_TAG release
        RELEASE_TAG=$TRAVIS_TAG
        RELEASE_NAME=$TRAVIS_TAG
        RELEASE_NOTES="Built ${RELEASE_DATE}."
        DRAFT="true"
        PRERELEASE="false"
    fi
else
    echo $0 Skipping non-tagged build on $TRAVIS_BRANCH branch
    exit 0  # Exit without error
fi

# ===========================
# GitHub Release Maintenance
# ===========================

# Query the release ids for releases with with the given name. If there's more than one, then we'll use the first.
# Note: in order to see "draft" releases, we need to authenticate with a user that has push access to the repo.
echo Quering the ids for $RELEASE_NAME
RELEASE_ARRAY=( \
    $(curl --silent --header "Authorization: token ${GITHUB_API_KEY}" ${RELEASES_URL} \
    | jq --arg name "${RELEASE_NAME}" '.[] | select(.name == $name) | .id') \
    ) > /dev/null

# Get the first id returned from the query
RELEASE_ID=${RELEASE_ARRAY[0]}
echo $RELEASE_NAME release id: $RELEASE_ID

# Update the GitHub releases (create if release id's length == 0)
if [[ ${#RELEASE_ID} -eq 0 ]]; then
    # Create a release
    echo Creating the $RELEASE_NAME Release

    # Build the JSON (Note: single quotes inhibit variable substitution, must use escaped double quotes)
    # Note: the tag already exists, so the "target_commitish" parameter is not used
    JSON_DATA="{ \
      \"tag_name\": \"${RELEASE_TAG}\", \
      \"name\": \"${RELEASE_NAME}\", \
      \"body\": \"${RELEASE_NOTES}\", \
      \"draft\": ${DRAFT}, \
      \"prerelease\": ${PRERELEASE} \
    }"

    # Create the release on GitHub and retrieve the JSON result
    RELEASE=$(curl --silent \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/json" \
    --header "Accept: application/json" \
    --data "${JSON_DATA}" \
    --request POST ${RELEASES_URL}) > /dev/null

    # Extract the newly created release id from the JSON result
    RELEASE_ID=$(echo $RELEASE | jq '.id')
    echo New release ID: $RELEASE_ID
else
    # Update an existing release
    echo Updating the $RELEASE_NAME release to tag $RELEASE_TAG

    # Define the patch data to update the tag_name and the timestamped release notes
    JSON_DATA="{ \
        \"tag_name\": \"${RELEASE_TAG}\", \
        \"body\": \"${RELEASE_NOTES}\" \
     }"

    # Update GitHub
    curl --silent \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/json" \
    --header "Accept: application/json" \
    --data "${JSON_DATA}" \
    --request PATCH ${RELEASES_URL}/${RELEASE_ID} > /dev/null
fi

# Assert that we found a GitHub release id for the current branch (release id length > 0)
if [[ ${#RELEASE_ID} -eq 0 ]]; then
    echo $0 error: The $RELEASE_NAME release was not found. No artifacts were uploaded to GitHub releases.
    exit 0
fi

# =================
# Release Artifacts
# =================

# Define the file names that are to be deployed
JAR_FILES=( worldwind.jar worldwindx.jar )
ZIP_FILES=( worldwind-javadoc.zip )
ALL_FILES=( "${JAR_FILES[@]}" "${ZIP_FILES[@]}" )

# Remove existing assets if they exist (asset id length > 0)
for FILENAME in ${ALL_FILES[*]}
do
    # Note, we're using the jq "--arg name value" commandline option to create the $filename variable
    ASSET_ID=$(curl --silent \
        ${RELEASES_URL}/${RELEASE_ID}/assets \
        | jq --arg filename $FILENAME '.[] | select(.name == $filename) | .id')

    if [ ${#ASSET_ID} -gt 0 ]; then
        echo DELETE ${RELEASES_URL}/assets/${ASSET_ID}
        curl --silent  \
        --header "Authorization: token ${GITHUB_API_KEY}" \
        --request DELETE ${RELEASES_URL}/assets/${ASSET_ID} > /dev/null
    fi
done

# Upload the release artifacts
for FILENAME in ${JAR_FILES[*]}
do
    curl --silent  \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/java-archive" \
    --header "Accept: application/json" \
    --data-binary @${TRAVIS_BUILD_DIR}/${FILENAME} \
    --request POST ${UPLOADS_URL}/${RELEASE_ID}/assets?name=${FILENAME} > /dev/null
done

for FILENAME in ${ZIP_FILES[*]}
do
    curl --silent  \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/zip" \
    --header "Accept: application/json" \
    --data-binary @${TRAVIS_BUILD_DIR}/${FILENAME} \
    --request POST ${UPLOADS_URL}/${RELEASE_ID}/assets?name=${FILENAME} > /dev/null
done

# ======================
# Daily Tag Maintenance
# ======================

# Cleanup daily tags
echo Cleaning up daily tags
if [[ "${TRAVIS_TAG}" == "daily"* ]]; then

    # Get a sorted array of daily tags (sorting is req'd if you want to do some array slicing)
    DAILY_TAGS=($(git tag --list "daily*" | sort))

    # Delete all the daily tags except this build's tag
    for TAG in ${DAILY_TAGS[*]}
    do
        if [[ $TAG != $TRAVIS_TAG ]]; then
            git tag --delete $TAG
            git push --quiet $REMOTE_URL :${TAG} > /dev/null
            RESULT=$?
            if [[ $RESULT -gt 0 ]]; then
                echo "$0 error: git push failed. Returned $RESULT"
                exit 1
            fi
        fi
    done
fi