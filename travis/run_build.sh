#!/bin/bash
# ======================================================================================================================
# This script encapsulates the logic for the the Travis "script" build step.
#
# The script controls the build tasks for cron jobs and otherwise.
#
# Syntax: run_build.sh
# ======================================================================================================================
# Exit
set -e

if [[ "$TRAVIS_EVENT_TYPE" == "cron" ]]; then
    # Add a "daily" tag to the repository, which will subsequently triggers a "tagged" build by Travis
    echo Creating the daily tag
    ./travis/add_daily_tag.sh
else
    # Build the World Wind Java SDK
    echo Building the SDK with ant
    ant worldwind.bundle

    # TODO: Run unit tests
fi
