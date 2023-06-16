#!/bin/bash

set -o pipefail

retry_attempts=5
retry_after_seconds=10
requiredVars=("MTLS_KEY" "MTLS_CERT" "SERVICE_URL" "API_KEY")

echo "Heartbeat Checker"
echo -e "=================\n"

echo "[Config] Maximum retry attempts: $retry_attempts"
echo "[Config] Retry after: $retry_after_seconds seconds"

for str in "${requiredVars[@]}"; do
  if [[ -z "${!str}" ]]; then
    echo "[Config] CircleCI context variable was undefined: $str"
    fail=1
  fi
done

if [[ $fail == 1 ]]; then
  echo "[Config] 💔️️ Failed! Missing CircleCI context variable(s)"
  exit 1
fi

echo -e "\n[Setup] Retrieving certificates from context";
echo -n "${MTLS_CERT}" | base64 --decode > /tmp/client.pem
echo -n "${MTLS_KEY}" | base64 --decode > /tmp/client.key
echo -e "[Setup] Certificates retrieved\n";

for (( attempts=1;attempts<=retry_attempts;attempts++ ))
do
  echo "[Attempt ${attempts}] 🧑‍⚕️ Checking for a heartbeat..."

  http_status_code=$(curl -s -o response.txt -w "%{http_code}" "${SERVICE_URL}" -H "x-api-key: ${API_KEY}" --cert /tmp/client.pem --key /tmp/client.key)

  if [[ $http_status_code != "200" ]]; then
    echo -e "[Attempt ${attempts}] 💔️ Failed at $(date)!"
    echo -e "[Attempt ${attempts}] 📋 $http_status_code - $(jq '.userMessage' response.txt)"
  else
    grep firstName response.txt > /dev/null

    if [[ $? == 0 ]]; then
      echo -e "[Attempt ${attempts}] ✅ Success! $http_status_code - Located firstName in response\n"
      exit 0
    else
      echo -e "[Attempt ${attempts}] 💔️ Failed at $(date)!"
      echo -e "[Attempt ${attempts}] 📋 Unable to locate firstName in response"
    fi
  fi

  if [[ $attempts != "$retry_attempts" ]]; then
    echo -e "[Attempt ${attempts}] 🔁 Retrying in $retry_after_seconds seconds..."
    sleep $retry_after_seconds
  fi
  echo
done

exit 1
