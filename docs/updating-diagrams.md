# Updating diagrams

Diagrams such as our [context diagram](/docs/diagrams/context.svg) are created
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

6. Push your code back to the repository, the [Generate PlantUML Diagrams GitHub Action](#automatic-diagram-generation) will generate the required output file.
   files automatically.

## Automatic Diagram Generation

We have a GitHub Actions workflow named [Generate PlantUML Diagrams](https://github.com/ministryofjustice/hmpps-integration-api/blob/main/.github/workflows/generate-plantuml-diagrams.yml)
has been created to assist in the generation of diagrams. This automatically creates `.svg` diagrams for each `.puml`
file within the `./docs/diagrams` directory. The benefit of this workflow is that the author does not have to manually
generate these diagrams.

This will execute on all branches on a `git push`. The workflow can also be executed manually via the [GitHub Actions page](https://github.com/ministryofjustice/hmpps-integration-api/actions).