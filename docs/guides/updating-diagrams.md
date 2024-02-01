# Updating diagrams

Diagrams such as our [context diagram](/docs/diagrams/context.svg) are created
using [PlantUML](https://plantuml.com/) i.e. diagrams as code.

> 💡 **Hint:** For [C4 diagrams](https://c4model.com/),
> see [PlantUML library for C4](https://github.com/plantuml-stdlib/C4-PlantUML) for more information.

## Using Visual Studio Code

1. Install the [PlantUML extension](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml).
2. Run a local [PlantUML server](https://github.com/plantuml/plantuml-server) using Docker:

```bash
docker run --name plantuml -d -p 1234:8080 plantuml/plantuml-server:jetty
```

3. Create a `.vscode/settings.json` if one does not exist.
4. Within the `settings.json` file, add the follow properties:

```json
{
  "plantuml.server": "http://localhost:1234",
  "plantuml.render": "PlantUMLServer"
}
```

This tells the PlantUML extension to use the local PlantUML server to render diagrams.

5. Edit the `.puml` file for the diagram.
6. Preview any changes to the diagram by using `Alt + D` on Windows or `Option + D` on MacOS.

When all changes have been made:

7. Push your code back to the repository, the [Generate PlantUML Diagrams GitHub Action](#automatic-diagram-generation) will generate the required output file(s) automatically.

## Automatic Diagram Generation

We have a GitHub Actions workflow named [Generate PlantUML Diagrams](https://github.com/ministryofjustice/hmpps-integration-api/blob/main/.github/workflows/generate-plantuml-diagrams.yml)
has been created to assist in the generation of diagrams. This automatically creates `.svg` diagrams for each `.puml`
file within the `./docs/diagrams` directory. The benefit of this workflow is that the author does not have to manually
generate these diagrams.

This will execute on all branches on a `git push`. The workflow can also be executed manually via the [GitHub Actions page](https://github.com/ministryofjustice/hmpps-integration-api/actions).
