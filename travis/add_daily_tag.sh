#!/bin/bash

# ======================================================================================================================
# Pushes a daily build tag to the HEAD of the checked out branch (usually develop). Does nothing if the build is not
# initiated by a cron job.
#
# Uses Git to update tags in the repo. Git commands using authentication are redirected to /dev/null to prevent leaking
# the access token into the log.
# ======================================================================================================================

# Ensure shell commands are not echoed to the log to prevent leaking the access token
set +x

# Exit without error when this build is not a cron job
if [[ "$TRAVIS_EVENT_TYPE" != "cron" ]]; then
    exit 0
fi

# Get the GitHub remote origin URL
REMOTE_URL=$(git config --get remote.origin.url) > /dev/null
# Add the GitHub authentication token if it's not already embedded in the URL (test if an "@" sign is present)
if [[ $REMOTE_URL != *"@"* ]]; then
    # Use the stream editor to inject the GitHub authentication token into the remote url after the protocol
    # Example:  https://github.com/.../repo.git -> https://<token>@github.com/.../repo.git
    REMOTE_URL=$(echo $REMOTE_URL | sed -e "s#://#://$GITHUB_API_KEY@#g") > /dev/null
fi

# Add the tag "daily/YYYYMMDD" tag to the HEAD. Delimiting the date with a slash allows Git tools to collect daily tags in a folder
# RELEASE_DATE=$(date '+%Y%m%d-%H%M%Z') # add time for testing
RELEASE_DATE=$(date '+%Y%m%d')
DAILY_TAG="daily/${RELEASE_DATE}"

# Create a lightweight tag (vs an annotated tag) and push it to the remote
echo "Pushing daily tag ${DAILY_TAG}"
git tag $DAILY_TAG
git push --quiet $REMOTE_URL tag $DAILY_TAG > /dev/null

# Error handling
RESULT=$?
if [[ $RESULT -gt 0 ]]; then
    echo "$0 error: git push failed. Returned $RESULT"
    exit 1
fi
