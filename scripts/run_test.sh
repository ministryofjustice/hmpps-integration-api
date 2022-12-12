#!/bin/bash

set -e

result=`curl 127.0.0.1:8081/offences/all`
expected_result="{}"

if [[ $result -ne $expected_result ]] {
  echo "test failed"
  exit 1
}

echo $result
echo "test passed"
exit 0