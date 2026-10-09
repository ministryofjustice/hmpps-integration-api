#!/bin/bash

node dist/index.js mock -p 4010 -h 0.0.0.0 /prismMocks/adjudications.json & port=4011;
wait
