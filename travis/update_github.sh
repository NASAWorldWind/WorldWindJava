#!/bin/bash
# ======================================================================================================================
# This script encapsulates the logic for the Travis "after_success" build step.
#
# The script conditionally updates the GitHub releases and the GitHub pages.
#
# Syntax: update_github.sh
# ======================================================================================================================

# Ensure the current build is NOT a cron job, exit otherwise.
if [[ "$TRAVIS_EVENT_TYPE" == "cron" ]]; then
    # Cron jobs are used to generate "daily" tags -- there are no build artifacts or javadoc to upload.
    # Exit quietly without error
    exit 0
fi

# Upload build artifacts to the GitHub releases
echo Updating GitHub Releases
./travis/update_release.sh

# Ensure the current build is a "tagged" build.
if  [[ -n "$TRAVIS_TAG" ]]; then
    echo Updating GitHub Pages
    ./travis/update_javadoc.sh
fi
