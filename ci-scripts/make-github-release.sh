#!/usr/bin/env bash

# set work dir
cd "$(dirname "${BASH_SOURCE[0]}")/.."

# get release notes
release_notes=`cat fastlane/metadata/android/en-US/changelogs/$VERSION_CODE.txt`

echo "Commit Tag: $CI_COMMIT_TAG"

if [[ "${CI_COMMIT_TAG}" = "" ]] ; then
   echo "No CI_COMMIT_TAG" >&2; exit 1
fi

# Check release exists?
echo "Check release exists..."
res=$(curl -X GET -s -H "Content-Type:application/json" -H "Authorization: token $github_token" https://sschueller@api.github.com/repos/sschueller/easyrepost/releases/tags/$CI_COMMIT_TAG)

rel=$(echo $res | jq ".id")

if ! [[ "${rel}" = "null" ]] || [[ "${rel}" = "" ]] ; then
   echo "Release exists $CI_COMMIT_TAG, stopping" >&2; exit 1
else
   echo "Release does not exist.";
fi

# escape release notes
release_notes=$(echo ${release_notes} | jq -aRs .)
postdata="{\"tag_name\":\"$CI_COMMIT_TAG\",\"target_commitish\":\"master\",\"name\":\"Release $CI_COMMIT_TAG\",\"body\":$release_notes,\"draft\":false,\"prerelease\":false}"

# Generate Release
echo "Generate Release..."
echo "$postdata" | jq

res=$(curl -s -X POST -H "Content-Type:application/json" -H "Authorization: token $github_token" https://sschueller@api.github.com/repos/sschueller/easyrepost/releases -d "$postdata")
echo $res | jq

release_id=$(echo $res | jq '.id')

echo "Release ID: $release_id"

re='^[0-9]+$'
if ! [[ $release_id =~ $re ]] ; then
   echo "Invalid ID $release_id" >&2; exit 1
fi

ls -la app/build/outputs
ls -la app/build/outputs/apk
ls -la app/build/outputs/apk/release

echo "Attaching artifact..."
# Attach artifact
curl -X POST -H "Authorization: token $github_token" -F 'data=@app/build/outputs/apk/release/app-release.apk' https://sschueller@uploads.github.com/repos/sschueller/easyrepost/releases/$release_id/assets?name=app-release.apk
