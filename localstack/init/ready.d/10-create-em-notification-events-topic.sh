#!/bin/sh

awslocal sns create-topic \
  --name em-notification-events-topic \
  --region eu-west-2

