#!/usr/bin/env bash

# set work dir
cd "$(dirname "${BASH_SOURCE[0]}")/.."

# get release notes
release_notes=`cat fastlane/metadata/android/en-US/changelogs/$VERSION_CODE.txt`

# Check release exists?
rel=$(curl -X GET -s -H "Content-Type:application/json" -H "Authorization: token $github_token" https://sschueller@api.github.com/repos/sschueller/easyrepost/releases/tags/$CI_COMMIT_TAG | jq ".id")

if ! [[ "${rel}" = "null" ]] ; then
   echo "Release exists $CI_COMMIT_TAG" >&2; exit 1
fi

postdata="{\"tag_name\":\"$CI_COMMIT_TAG\",\"target_commitish\": \"master\",\"name\": \"Release $CI_COMMIT_TAG\",\"body\": \"$release_notes\",\"draft\": false,\"prerelease\": false}"

# Generate Release
#
release_id=$(curl -s -X POST -H "Content-Type:application/json" -H "Authorization: token $github_token" https://sschueller@api.github.com/repos/sschueller/easyrepost/releases -d "$postdata" | jq '.id')

re='^[0-9]+$'
if ! [[ $release_id =~ $re ]] ; then
   echo "Invalid ID $release_id" >&2; exit 1
fi

# Attach artifact
curl -X POST -H "Authorization: token $github_token" -F 'data=@build/outputs/apk/release/app-release.apk' https://sschueller@uploads.github.com/repos/sschueller/easyrepost/releases/$release_id/assets?name=app-release.apk