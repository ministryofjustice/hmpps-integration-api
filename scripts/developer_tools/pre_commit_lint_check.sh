#!/bin/bash

GREEN='\033[1;32m'
RED='\033[1;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PREPARATION_MESSAGE="${YELLOW}🌟 ${GREEN}Preparing for pre-commit checks...${NC}"
RUN_LINT_MESSAGE="${YELLOW}🚀 ${GREEN}Running ${NC}'./gradlew ktlintCheck'${GREEN}...${NC}"
LINT_PASSED_MESSAGE="${GREEN}✅ './gradlew ktlintCheck' passed. Proceeding with commit.${NC}"
LINT_FAILED_MESSAGE="${RED}❌ './gradlew ktlintCheck' failed. Running ${NC}'./gradlew ktlintFormat'${RED}...${NC}"
FORMAT_PASSED_MESSAGE="${GREEN}✅ './gradlew ktlintFormat' passed. Staging changes and committing.${NC}"
FORMAT_FAILED_MESSAGE="${RED}❌ './gradlew ktlintFormat' failed. Aborting commit.${NC}"

echo -e "$PREPARATION_MESSAGE"

run_lint() {
  echo -e "$RUN_LINT_MESSAGE"
  ./gradlew ktlintCheck

  if [ $? -eq 0 ]; then
    echo -e "$LINT_PASSED_MESSAGE"
    exit 0
  else
    handle_lint_failure
  fi
}

handle_lint_failure() {
  echo -e "$LINT_FAILED_MESSAGE"
  ./gradlew ktlintFormat

  if [ $? -eq 0 ]; then
    echo -e "$FORMAT_PASSED_MESSAGE"
    git add -u
    git add .
    git commit --amend --no-edit
    exit 0
  else
    echo -e "$FORMAT_FAILED_MESSAGE"
    exit 1
  fi
}

run_lint
