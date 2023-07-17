# Python Repository

This repository contains Python scripts and modules for various purposes.

## Contents

- [Python Repository](#python-repository)
  - [Contents](#contents)
  - [Requirements](#requirements)
  - [Installation](#installation)
  - [Scripts](#scripts)
    - [Retrieve Schemas and their fields from Prison API](#retrieve-schemas-and-their-fields-from-prison-api)
  - [Modules](#modules)
    - [Constants](#constants)
  - [Contributing](#contributing)

## Requirements

Before running the scripts in this repository, make sure you have the following:

- Python 3.6 or above
- Virtual environment (optional but recommended, included in instructions below)

## Installation

To set up the virtual environment, you can use the provided `requirements.txt` file:

```shell
pip install -r requirements.txt
```
or if you are running pip3
```shell
pip3 install -r requirements.txt
```

## Scripts
### Retrieve Schemas and their fields from Prison API
[retrieve_schema_fields.py](retrieve_schema_fields.py)
To run this script *from within the tooling folder*, based on your python distribution:

```shell
python retrieve_schema_fields.py
```
If funning multiple versions of python, you can specify to use python 3 with
```shell
python3 retrieve_schema_fields.py
```
Outputs:
A csv file in the [outputs](outputs) directory containing

## Modules
This repository contains the following Python modules:

### Constants
There is a [constants](constants) directory, initialised to be a module directory, containing [constants and common objects](constants/common.py) used by the scripts in this tolling section.
This allows you to edit constants in one location, without having to amend other scripts in the base [tooling](../tooling/) directory.
Feel free to explore them for more functionality.

## Contributing
Contributions to this tooling section are welcome, as long as they can be executed in python. Autodocumentation is a potential and the desire is to keep this option open as this tooling section expands.