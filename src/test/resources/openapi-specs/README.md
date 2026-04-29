# OpenApi Specifications

The files in this folder are used to validate any changes to our upstream services.

These files have been downloaded by the script in /scripts/update_openapi_specs.sh.
Only jq has been used to make them a readable format. Please dont use any other tool to preserve diffs.

To update these files run the following command at the root of the project
 ```bash
 cd ../../../../scripts
 ./update_openapi_specs.sh
 ```
Run the tests and fix any issues
