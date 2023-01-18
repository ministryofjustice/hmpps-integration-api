# Updating diagrams

Diagrams such as our [context diagram](/docs/diagrams/context-diagram.png) are created
using [PlantUML](https://plantuml.com/) i.e. diagrams as code.

> 💡 **Hint:** For [C4 diagrams](https://c4model.com/),
> see [PlantUML library for C4](https://github.com/plantuml-stdlib/C4-PlantUML) for more information.

## Using Visual Studio Code

1. Install the [PlantUML extension](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml).
2. Create a `.vscode/settings.json` if one does not exist.
3. Within the `settings.json` file, add the follow properties:

```json
{
  "plantuml.server": "https://www.plantuml.com/plantuml",
  "plantuml.render": "PlantUMLServer"
}
```

This tells the PlantUML extension to use the PlantUML server to render diagrams. It's also possible to point to a
locally hosted server by changing the value of `plantuml.server`.

4. Edit the `.puml` file for the diagram.
5. Preview any changes to the diagram by using `Alt + D` on Windows or `Option + D` on MacOS.

When all changes have been made:

6. With the diagram file open, using `Ctrl + shift + P` on Windows or `command + shift + P` on MacOS and searching "
   export current diagram", choose the `PlantUML: Export Current Diagram` option.

This will display options for different file types to export to.

7. Choose the `png` option.

The exported diagram will then appear in an `out` directory in the root of the repository.

8. Move the exported diagram to where the current diagram is located to replace it.

## Using PlantUML Web Server

[PlantUML Web Server](http://www.plantuml.com/plantuml/uml/) allows live editing through the browser.

1. Go to [PlantUML Web Server](http://www.plantuml.com/plantuml/uml/) in a browser.
2. Copy the contents of the `.puml` file of the diagram from the repository.
3. Paste the contents into the box of PlantUML Web Server browser tab.
4. Make the changes to the code to update the diagram.

When all changes have been made:

5. Click the `PNG` link.

This will open the diagram on the current tab.

6. Download and replace the exported diagram in the repository.
