#!/bin/bash

node dist/index.js mock -p 4010 -h 0.0.0.0 /prismMocks/adjudications.json & port=4011;
node dist/index.js mock -p 4011 -h 0.0.0.0 /prismMocks/assess-risks-and-needs.json & port=4012;
node dist/index.js mock -p 4012 -h 0.0.0.0 /prismMocks/case-notes.json & port=4013;
node dist/index.js mock -p 4013 -h 0.0.0.0 /prismMocks/create-and-vary-licence.json & port=4014;
node dist/index.js mock -p 4014 -h 0.0.0.0 /prismMocks/ndelius.json & port=4015;
wait
