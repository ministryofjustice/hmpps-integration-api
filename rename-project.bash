#!/usr/bin/env bash

set -e

if ! echo "$BASH_VERSION" | grep -E "^[45]" &>/dev/null; then
  echo "Found bash version: $BASH_VERSION"
  echo "Ensure you are using bash version 4 or 5"
  exit 1
fi

if [[ $# -ge 1 ]]; then
  PROJECT_INPUT=$1
else
  read -rp "New project name e.g. prison-visits >" PROJECT_INPUT
fi

PROJECT_NAME_LOWER=${PROJECT_INPUT,,}                 # lowercase
PROJECT_NAME_HYPHENS=${PROJECT_NAME_LOWER// /-}       # spaces to hyphens

PROJECT_NAME=${PROJECT_NAME_HYPHENS//[^a-z0-9-]/}     # remove all other characters
PACKAGE_NAME=${PROJECT_NAME//-/}                      # remove hyphen

read -ra PROJECT_NAME_ARRAY <<<"${PROJECT_NAME//-/ }" # convert to array
PROJECT_DESCRIPTION=${PROJECT_NAME_ARRAY[*]^}         # convert array back to string thus capitalising first character
CLASS_NAME=${PROJECT_DESCRIPTION// /}                 # then remove spaces

echo "Found:      Project of $PROJECT_DESCRIPTION"
echo "       Project name of $PROJECT_NAME"
echo "       Package name of $PACKAGE_NAME"
echo "         Class name of $CLASS_NAME"

echo "Performing search and replace"

# exclude files that get in the way and don't make any difference
EXCLUDES="( -path ./build -o -path ./out -o -path ./.git -o -path ./.gradle -o -path ./gradle -o -path ./.idea -o -path ./rename-project.bash )"
# shellcheck disable=SC2086
find . $EXCLUDES -prune -o -type f -exec sed -i.bak \
  -e "s/hmpps-template-kotlin/$PROJECT_NAME/g" \
  -e "s/HMPPS Template Kotlin/$PROJECT_DESCRIPTION/g" \
  -e "s/HmppsTemplateKotlin/$CLASS_NAME/g" \
  -e "s/hmppstemplatepackagename/$PACKAGE_NAME/g" {} \; -exec rm '{}.bak' \;

echo "Performing directory renames"

# move package directory to new name
BASE="kotlin/uk/gov/justice/digital/hmpps"
mv "src/test/${BASE}/hmppstemplatepackagename" "src/test/$BASE/$PACKAGE_NAME"
mv "src/main/${BASE}/hmppstemplatepackagename" "src/main/$BASE/$PACKAGE_NAME"

# and move helm stuff to new name
mv "helm_deploy/hmpps-template-kotlin" "helm_deploy/$PROJECT_NAME"

# rename kotlin files
mv "src/main/$BASE/$PACKAGE_NAME/HmppsTemplateKotlin.kt" "src/main/$BASE/$PACKAGE_NAME/$CLASS_NAME.kt"
mv "src/main/$BASE/$PACKAGE_NAME/config/HmppsTemplateKotlinExceptionHandler.kt" "src/main/$BASE/$PACKAGE_NAME/config/${CLASS_NAME}ExceptionHandler.kt"

# change cron job to be random time otherwise we hit rate limiting with veracode
RANDOM_HOUR=$((RANDOM % (9 - 3 + 1) + 3))
RANDOM_MINUTE=$(($RANDOM%60))
RANDOM_MINUTE2=$(($RANDOM%60))
sed -i -z -E \
  -e "s/security:\n    triggers:\n      - schedule:\n          cron: \"11 5/security:\n    triggers:\n      - schedule:\n          cron: \"$RANDOM_MINUTE $RANDOM_HOUR/" \
  -e "s/security-weekly:\n    triggers:\n      - schedule:\n          cron: \"0 5/security-weekly:\n    triggers:\n      - schedule:\n          cron: \"$RANDOM_MINUTE2 $RANDOM_HOUR/" \
  .circleci/config.yml


# lastly remove ourselves
rm rename-project.bash

echo "Completed."
echo "Please now review changes and generate a banner for src/main/resources/banner.txt"
