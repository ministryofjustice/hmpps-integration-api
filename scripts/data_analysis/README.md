# Data Analysis Tooling <!-- omit in toc -->

## Contents <!-- omit in toc -->
This repository contains Python scripts and modules for various tooling purposes.

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
Outputs:
A csv file in the [outputs](outputs) directory containing

## Modules
This repository contains the following Python modules:

### Constants
There is a [constants](constants) directory, initialised to be a module directory, containing [constants and common objects](constants/common.py) used by the scripts.
This allows you to edit constants in one location, without having to amend other scripts in the base [data_analysis](../data_analysis/) directory.
Feel free to explore them for more functionality.

## Contributing
Contributions to this tooling section are welcome, as long as they can be executed in python. Autodocumentation is a potential and the desire is to keep this option open as this tooling section expands.