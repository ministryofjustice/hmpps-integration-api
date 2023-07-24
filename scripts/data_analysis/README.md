# Data Analysis Tooling <!-- omit in toc -->

## Contents <!-- omit in toc -->
This repository contains Python scripts and modules for various tooling purposes.

- [Requirements](#requirements)
- [Installation](#installation)
- [Scripts](#scripts)
  - [Retrieve Schemas and their fields from Prison API](#retrieve-schemas-and-their-fields-from-prison-api)
  - [Generate a Schema space diagram, output child-parent relations](#generate-a-schema-space-diagram-output-child-parent-relations)
- [Modules](#modules)
  - [Constants](#constants)
- [Contributing](#contributing)

## Requirements

Before running the scripts in this repository, make sure you have the following:

- Python 3.7 or above
- Virtual environment - included in Installation instructions below

## Installation

To set up the virtual environment, you can create your own preference, or you can use the provided `requirements.txt` file:

```shell
pip3 install -r requirements.txt
```

## Scripts
### Retrieve Schemas and their fields from Prison API
[retrieve_schema_fields.py](retrieve_schema_fields.py)
To run this script *from within the tooling folder*, based on your python distribution:

```shell
python3 retrieve_schema_fields.py
```
>Outputs:
> - A csv file in the [outputs](outputs) directory containing schemas and fields within them

### Generate a Schema space diagram, output child-parent relations
[generate_schema_diagram.py](generate_schema_diagram.py)
There are several options for the running of this script:
- Search for one schema
- Search for multiple schema
- No Search option - generate full diagram

To run this script *from within the tooling folder*, based on your Python distribution:
- Search for one schema, where the argument is a schema name in a string format
```shell
python3 generate_schema_diagram.py "AddressDto"
```
- Search for mulitiple schemas, where the arguments are all strings of schema names:
```shell
python3 generate_schema_diagram.py "AddressDto" "SentenceCalcDates"
```
- No search option, generating a full diagram:
```shell
python3 generate_schema_diagram.py
```

>Outputs
> - A csv file with the parent child relations of schemas.
> - A diagram in .dot format, renderable in plantuml or local graphviz renderer, of the schema relations

## Modules
This repository contains the following Python modules:

### Constants
There is a [constants](constants) directory, initialised to be a module directory, containing [constants and common objects](constants/common.py) used by the scripts.
This allows you to edit constants in one location, without having to amend other scripts in the base [data_analysis](../data_analysis/) directory.
Feel free to explore them for more functionality.

## Contributing
Contributions to this tooling section are welcome, as long as they can be executed in Python. Autodocumentation is a potential and the desire is to keep this option open as this tooling section expands.
