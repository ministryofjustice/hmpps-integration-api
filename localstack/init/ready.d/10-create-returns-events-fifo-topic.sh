#!/bin/sh

awslocal sns create-topic \
  --name returns-events.fifo \
  --attributes '{"FifoTopic":"true","ContentBasedDeduplication":"true"}' \
  --region eu-west-2


