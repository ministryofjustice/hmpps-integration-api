# OpenApi Specifications

The files in this folder are used to validate any changes to our upstream services.

These files have been downloaded by the script in /scripts/update_openapi_specs.sh.
No formating has been applied to these files intentionally.
If you format these using in IntelliJ or any other tool for development purposes, please don't commit any formatting changes.

To update these files run the following command at the root of the project

```bash
cd ../../../../scripts
./update_openapi_specs.sh
```

Run the tests and fix any issues
