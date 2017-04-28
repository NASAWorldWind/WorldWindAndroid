#!/bin/bash

# ======================================================================================================================
# Prepares the Travis build environment for the project build and conditional post-build actions.
# ======================================================================================================================

# Ensure shell commands are not echoed to the log to prevent leaking encrypted environment variables
set +x

# Decrypt the Android signing keystore. Skip the decryption step on untrusted pull request builds, as the encryption
# environment variables are not available.
if [[ "${TRAVIS_PULL_REQUEST}" == "false"  ]]; then
    openssl aes-256-cbc -K "${encrypted_2eaf8cabe659_key}" -iv "${encrypted_2eaf8cabe659_iv}" -in keystore.jks.enc -out keystore.jks -d
fi

# Install the jq shell filter so we can extract data from GitHub API JSON results
# See apt configuration: https://docs.travis-ci.com/user/ci-environment/#apt-configuration
sudo apt-get install -qq jq
jq --version
