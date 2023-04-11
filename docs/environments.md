# Environments

This API is designed to run in 4 environments:

| Name           | Deployed | Data                                               | Purpose                                           |
| -------------- | -------- | -------------------------------------------------- | ------------------------------------------------- |
| Local          | N/A      | Prism Stub                                         | Local Feature Development                         |
| Development    | On Merge | T3/Development Seed                                | Internal Testing                                  |
| Pre-Production | On Merge | Real - copy of live imported every couple of weeks | Internal Testing / QA / External Consumer Testing |
| Production     | Manually | Real                                               | Live Services                                     |