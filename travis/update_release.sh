#!/bin/bash

# ======================================================================================================================
# Creates or updates a GitHub release and uploads release artifacts for builds initiated by a tag. Does nothing if the
# build is not associated with a tag.
#
# Uses curl to performs CRUD operations on the GitHub Repos/Releases REST API.
#
# Uses jq to process the REST API JSON results. jq should be installed before this script is executed, e.g., in
# .travis.yml before_script block:
#       sudo apt-get install -qq jq
# See https://stedolan.github.io/jq/manual/ for jq documentation.
#
# Uses Git to update tags in the repo. Git commands using authentication are redirected to /dev/null to prevent leaking
# the access token into the log.
# ======================================================================================================================

# Ensure shell commands are not echoed in the log to prevent leaking the access token.
set +x

# Ensure the GitHub Personal Access Token for GitHub exists
if [[ -z "$GITHUB_API_KEY" ]]; then
    echo "$0 error: You must export the GITHUB_API_KEY containing the personal access token for Travis\; GitHub was not updated."
    exit 1
fi

# GitHub RESTful API URLs
RELEASES_URL="https://api.github.com/repos/nasaworldwind/worldwindjava/releases"
TAGS_URL="https://api.github.com/repos/nasaworldwind/worldwindjava/tags"
UPLOADS_URL="https://uploads.github.com/repos/nasaworldwind/worldwindjava/releases"
DOWNLOADS_URL="https://github.com/nasaworldwind/worldwindjava/releases/download"

# Get the GitHub remote origin URL
REMOTE_URL=$(git config --get remote.origin.url) > /dev/null
# Add the GitHub authentication token if it's not already embedded in the URL (test if an "@" sign is present)
if [[ $REMOTE_URL != *"@"* ]]; then
    # Use the stream editor to inject the GitHub authentication token into the remote url after the protocol
    # Example:  https://github.com/.../repo.git -> https://<token>@github.com/.../repo.git
    REMOTE_URL=$(echo $REMOTE_URL | sed -e "s#://#://$GITHUB_API_KEY@#g") > /dev/null
fi

# Initialize the release variables predicated on the tag. On a tagged build, Travis cloned the repo into a branch named
# with the tag, thus the TRAVIS_BRANCH var reflects the tag name, not "master" or "develop" like you might expect.
if [[ "${TRAVIS_TAG}" == "daily"* ]]; then # daily build associated with a tag in the format daily/YYYYMMDD
    RELEASE_NAME="Daily Build"
    RELEASE_SUFFIX="daily"
    DRAFT="false"
    PRERELEASE="true"
elif [[ -n $TRAVIS_TAG ]]; then # manually created tag; prepare a draft release
    RELEASE_NAME=$TRAVIS_TAG
    RELEASE_SUFFIX=$TRAVIS_TAG
    DRAFT="true"
    PRERELEASE="false"
else # build is not associated with a tag; exit quietly without error
    exit 0
fi


# ===========================
# GitHub Release Maintenance
# ===========================

# Query the release ids for releases with with the given name. If there's more than one, then we'll use the first. In
# order to see draft releases, we need to authenticate with a user that has push access to the repo.
RELEASE_ARRAY=( \
    $(curl --silent --header "Authorization: token ${GITHUB_API_KEY}" ${RELEASES_URL} \
    | jq --arg name "${RELEASE_NAME}" '.[] | select(.name == $name) | .id') \
    ) > /dev/null

# Get the first id returned from the query
RELEASE_ID=${RELEASE_ARRAY[0]}

# Update the GitHub releases (create if release id's length == 0)
if [[ ${#RELEASE_ID} -eq 0 ]]; then
    # Emit a log message for the new release
    echo "Creating release ${RELEASE_NAME} with tag ${TRAVIS_TAG}"

    # Build the JSON (Note: single quotes inhibit variable substitution, must use escaped double quotes)
    # Note: the tag already exists, so the "target_commitish" parameter is not used
    JSON_DATA="{ \
      \"tag_name\": \"${TRAVIS_TAG}\", \
      \"name\": \"${RELEASE_NAME}\", \
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
else
    # Emit a log message for the updated release
    echo "Updating release ${RELEASE_NAME} with tag ${TRAVIS_TAG}"

    # Define the patch data to update the tag_name
    JSON_DATA="{ \
        \"tag_name\": \"${TRAVIS_TAG}\" \
     }"

    # Update the release on GitHub
    curl --silent \
    --header "Authorization: token ${GITHUB_API_KEY}" \
    --header "Content-Type: application/json" \
    --header "Accept: application/json" \
    --data "${JSON_DATA}" \
    --request PATCH ${RELEASES_URL}/${RELEASE_ID} > /dev/null
fi

# Assert that we found a GitHub release id for the current branch (release id length > 0)
if [[ ${#RELEASE_ID} -eq 0 ]]; then
    echo "$0 error: Release $RELEASE_NAME was not found. No artifacts were uploaded to GitHub releases."
    exit 0
fi

# =================
# Release Artifacts
# =================

# Copy and rename build assets that are to be deployed.
cp -f ${TRAVIS_BUILD_DIR}/build/worldwind.zip ${TRAVIS_BUILD_DIR}/worldwind-${RELEASE_SUFFIX}.zip
cp -f ${TRAVIS_BUILD_DIR}/build/doc/worldwind-javadoc.zip ${TRAVIS_BUILD_DIR}/worldwind-javadoc-${RELEASE_SUFFIX}.zip
cp -f ${TRAVIS_BUILD_DIR}/build/webstart/worldwind-webstart.zip ${TRAVIS_BUILD_DIR}/worldwind-webstart-${RELEASE_SUFFIX}.zip

# Define the file names that are to be deployed
ALL_FILES=( worldwind-${RELEASE_SUFFIX}.zip worldwind-javadoc-${RELEASE_SUFFIX}.zip worldwind-webstart-${RELEASE_SUFFIX}.zip )

# Emit a log message for the updated release
echo "Uploading release assets for ${RELEASE_NAME}"

# Remove existing assets if they exist (asset id length > 0)
for FILENAME in ${ALL_FILES[*]}
do
    # Note, we're using the jq "--arg name value" commandline option to create the $filename variable
    ASSET_ID=$(curl --silent \
        ${RELEASES_URL}/${RELEASE_ID}/assets \
        | jq --arg filename $FILENAME '.[] | select(.name == $filename) | .id')

    if [ ${#ASSET_ID} -gt 0 ]; then
        echo "Deleting ${FILENAME}"
        curl --silent  \
        --header "Authorization: token ${GITHUB_API_KEY}" \
        --request DELETE ${RELEASES_URL}/assets/${ASSET_ID} > /dev/null
    fi
done

for FILENAME in ${ALL_FILES[*]}
do
    echo "Posting ${FILENAME}"
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
if [[ "${TRAVIS_TAG}" == "daily"* ]]; then
    # Emit a log message for the daily tag cleanup
    echo "Cleaning up daily tags"

    # Get a sorted array of daily tags (sorting is req'd if you want to do some array slicing)
    DAILY_TAGS=($(git tag --list "daily*" | sort))

    # Delete all the daily tags except this build's tag
    for TAG in ${DAILY_TAGS[*]}
    do
        if [[ $TAG != $TRAVIS_TAG ]]; then
            echo "Deleting ${TAG}"
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