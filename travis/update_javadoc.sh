#!/bin/bash
# ======================================================================================================================
# This script updates the GitHub Pages website with the updated JavaDoc from the build.
#
# The script clones the GitHub pages to the local filesystem, deletes the javadoc for the specified branch,
# copies in the new javadoc, and then commits and pushes the changes.
#
# Note: git commands using authentication are use --quiet and redirect to null to prevent leaking the token to the log.
#
# Syntax: update_javadoc.sh
# ======================================================================================================================
# Ensure shell commands are not echoed in the log to prevent leaking the access token.
set +x

# Assert the GitHub Personal Access Token exists.
if [[ -z "$GITHUB_API_KEY" ]]; then
    echo $0 error: You must export the GITHUB_API_KEY containing the personal access token for Travis\; the repo was not cloned.
    exit 1
fi

# Assert the GitHub Pages repo is defined.
if [[ -z "$GH_PAGES_REPO" ]]; then
    echo $0 error: You must export the GH_PAGES_REPO containing GitHub Pages URL sans protocol\; the repo was not cloned.
    exit 1
fi

# Initialize the BRANCH var predicated on the tag. Note, on a tagged build, Travis cloned the repo into a branch named
# with the tag, thus the TRAVIS_BRANCH var reflects the tag name, not "master" or "develop" like you might expect.
if [[ "${TRAVIS_TAG}" == "daily"* ]]; then
    # Javadoc for daily tags are associated with the develop branch
    BRANCH="develop"
else
    # TODO: test the TRAVIS_TAG for semantic versioning as a condition for assigning to master branch.
    # Javadoc for all other tagged releases are associated with the master branch
    BRANCH="master"
fi

# Configure the user to be associated with commits to the GitHub pages
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"

# Define the path in the local filesystem where the repo will be cloned
GH_PAGES_DIR=${HOME}/gh_pages

echo Cloning $GH_PAGES_REPO to $GH_PAGES_DIR

git clone --quiet --branch=master https://${GITHUB_API_KEY}@${GH_PAGES_REPO} $GH_PAGES_DIR > /dev/null
cd $GH_PAGES_DIR

echo Updating ${GH_PAGES_REPO}/assets/java/${BRANCH}/javadoc

# Remove the existing javadocs from the GitHub pages
git rm -rfq --ignore-unmatch ./assets/java/${BRANCH}/javadoc

# Copy the new javadocs into the pages repo
mkdir -p ./assets/java/${BRANCH}/javadoc
cp -Rf ${TRAVIS_BUILD_DIR}/doc/javadoc/* ./assets/java/${BRANCH}/javadoc

# Commit the changes
git add -f .
git commit -m "Updated javadoc from successful travis build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"

# Push the changes (quietly) to the GitHub pages remote
git push -fq origin master > /dev/null
