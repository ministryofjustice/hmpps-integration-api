#!/bin/bash

# Define color codes
GREEN='\033[1;32m'
RED='\033[1;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Messages
PREPARATION_MESSAGE="${YELLOW}🌟 ${GREEN}Preparing for pre-commit checks...${NC}"
RUN_LINT_MESSAGE="${YELLOW}🚀 ${GREEN}Running ${NC}'make lint'${GREEN}...${NC}"
LINT_PASSED_MESSAGE="${GREEN}✅ 'make lint' passed. Proceeding with commit.${NC}"
LINT_FAILED_MESSAGE="${RED}❌ 'make lint' failed. Running ${NC}'make format'${RED}...${NC}"
FORMAT_PASSED_MESSAGE="${GREEN}✅ 'make format' passed. Staging changes and committing.${NC}"
FORMAT_FAILED_MESSAGE="${RED}❌ 'make format' failed. Aborting commit.${NC}"

echo -e "$PREPARATION_MESSAGE"

# Function to run lint and handle the result
run_lint() {
  echo -e "$RUN_LINT_MESSAGE"
  make lint

  # Check the exit status of make lint
  if [ $? -eq 0 ]; then
    echo -e "$LINT_PASSED_MESSAGE"
    exit 0
  else
    handle_lint_failure
  fi
}

# Function to handle lint failure
handle_lint_failure() {
  echo -e "$LINT_FAILED_MESSAGE"
  make format

  # Check the exit status of make format
  if [ $? -eq 0 ]; then
    echo -e "$FORMAT_PASSED_MESSAGE"
    git status
    # Stage files modified by make format
    git add $(git status -s | awk '/^[ M]/ {print $2}')

    exit 0
  else
    echo -e "$FORMAT_FAILED_MESSAGE"
    exit 1
  fi
}

# Start by running lint
run_lint
