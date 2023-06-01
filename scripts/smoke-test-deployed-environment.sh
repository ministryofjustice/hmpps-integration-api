#!/bin/bash

set -o pipefail

curl -s -o /dev/null -w "%{http_code}%" -L "https://google.com";
