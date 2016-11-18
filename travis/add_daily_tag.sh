#!/bin/bash
# ======================================================================================================================
# For Travis builds triggered by a cron job, this script adds a "daily" tag to the HEAD of the checked out branch.
#
# The script uses "git" to update tags in the repo. Commands using authentication use --quiet prevent leaking the access
# token into the log. Also, these are also redirected to /dev/null to prevent leaking the access token in case of error.
#
# Syntax: add_daily_tag.sh
# ======================================================================================================================
# Ensure shell commands are not echoed to the log to prevent leaking the access token.
set +x

# Ensure the current build is a cron-job
if [[ "$TRAVIS_EVENT_TYPE" != "cron" ]]; then
    exit 0  # Exit quietly without error
fi

# Get the GitHub remote origin URL.
REMOTE_URL=$(git config --get remote.origin.url) > /dev/null
# Add the GitHub authentication token if it's not already embedded in the URL (test if an "@" sign is present).
if [[ $REMOTE_URL != *"@"* ]]; then
    # Use the stream editor to inject the GitHub authentication token into the remote url after the protocol.
    # Example:  https://github.com/.../repo.git -> https://<token>@github.com/.../repo.git
    REMOTE_URL=$(echo $REMOTE_URL | sed -e "s#://#://$GITHUB_API_KEY@#g") > /dev/null
fi

# Add a "daily/YYYYMMDD" tag to the HEAD. Using a "/" in the name allows GUI tools to collect all "daily" tags in a folder.
# RELEASE_DATE=$(date '+%Y%m%d-%H%M%Z') # add time for testing
RELEASE_DATE=$(date '+%Y%m%d')
DAILY_TAG="daily/${RELEASE_DATE}"

# Create a lightweight tag (vs an annotated tag) and push it to the remote
git tag $DAILY_TAG
git push --quiet $REMOTE_URL tag $DAILY_TAG > /dev/null

# Error handling
RESULT=$?
if [[ $RESULT -gt 0 ]]; then
    echo "$0 error: git push failed. Returned $RESULT"
    exit 1
fi
