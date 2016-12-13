#!/bin/bash
# ======================================================================================================================
# Updates the GitHub Pages website with updated build assets. Clones the GitHub Pages to the local filesystem, deletes
# the assets for the current build, copies the new assets, then commits and pushes the changes.
#
# Uses Git to update tags in the repo. Git commands using authentication are redirected to /dev/null to prevent leaking
# the access token into the log.
# ======================================================================================================================

# Ensure shell commands are not echoed in the log to prevent leaking the access token.
set +x

# Assert the GitHub Personal Access Token exists
if [[ -z "$GITHUB_API_KEY" ]]; then
    echo "$0 error: You must export the GITHUB_API_KEY containing the personal access token for Travis\; the repo was not cloned."
    exit 1
fi

# Assert the GitHub Pages repo is defined
if [[ -z "$GH_PAGES_REPO" ]]; then
    echo "$0 error: You must export the GH_PAGES_REPO containing GitHub Pages URL sans protocol\; the repo was not cloned."
    exit 1
fi

# Initialize the FOLDER var predicated on the build configuration. On a tagged build, Travis cloned the repo into a
# branch named with the tag, thus the TRAVIS_BRANCH var reflects the tag name, not "master" or "develop" like you might
# expect.
if [[ "${TRAVIS_TAG}" == "daily"* ]]; then # daily build associated with a tag in the format daily/YYYYMMDD
    FOLDER="daily"
elif [[ -n $TRAVIS_TAG ]]; then # manually created tag; draft release
    FOLDER="${TRAVIS_TAG}"
elif [[ "${TRAVIS_BRANCH}" == "master" ]]; then # latest stable build from the master branch
    FOLDER="latest"
else # all other build types; exit quietly without error
    exit 0
fi

# Emit a log message for the update
echo "Updating assets to ${GH_PAGES_REPO}/assets/java/${FOLDER}"

# Configure the user to be associated with commits to the GitHub pages
git config --global user.email "travis@travis-ci.org"
git config --global user.name "travis-ci"

# Clone the GitHub Pages repository to the local filesystem
GH_PAGES_DIR=${HOME}/gh_pages
git clone --quiet --branch=master https://${GITHUB_API_KEY}@${GH_PAGES_REPO} $GH_PAGES_DIR > /dev/null
cd $GH_PAGES_DIR

# Remove existing build assets from the repository, if any
git rm -rfq --ignore-unmatch ./assets/java/${FOLDER}

# Copy new Javadocs to the repository
mkdir -p ./assets/java/${FOLDER}/javadoc
cp -Rf ${TRAVIS_BUILD_DIR}/build/doc/javadoc/* ./assets/java/${FOLDER}/javadoc

# Copy new Web Start files to the repository
mkdir -p ./assets/java/${FOLDER}/webstart
cp -Rf ${TRAVIS_BUILD_DIR}/build/webstart/jar/* ./assets/java/${FOLDER}/webstart
cp -Rf ${TRAVIS_BUILD_DIR}/build/webstart/jnlp/* ./assets/java/${FOLDER}/webstart

# Replace the empty codebase attribute with the fully qualified Web Start path.
sed -i -- "s codebase=\"\" codebase=\"https://worldwind.arc.nasa.gov/java/${FOLDER}/webstart/\" g" ./assets/java/${FOLDER}/webstart/*.jnlp

# Commit and push the changes (quietly)
git add -f .
git commit -m "Updated assets from successful travis build $TRAVIS_BUILD_NUMBER in $TRAVIS_BRANCH"
git push -fq origin master > /dev/null
