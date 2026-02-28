package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileManager {
  /**
   * Function to create a file at the specified path with the specified content
   *
   * @param path
   * @param content
   */
  fun write(
    path: String,
    content: String,
  ) {
    File(path).createNewFile()
    File(path).writeText(content)
  }

  /**
   * Function to read a file at the specified path
   *
   * @param path
   */
  fun read(path: String): File? = File(path).takeIf { it.exists() }

  /**
   * Function that creates a folder at the specified path
   *
   * @param path
   */
  fun checkOrCreateDirectory(path: String) {
    Files.createDirectories(Paths.get(path))
  }

  /**
   * Function that reads file from a path relative to the resource folder
   *
   * @param path
   */
  fun readFileFromResourcesFolder(path: String): File? = read("${getResourcesFolderPath()}/$path")

  /**
   * Function that reads the file contents from a path relative to the resources folder
   *
   * @param path
   */
  fun readFileContentsFromResourcesFolder(path: String): String? = read("${getResourcesFolderPath()}/$path")?.readText()

  /**
   * Function that returns the resources folder
   *
   */
  fun getResourcesFolderPath(): String {
    val resourceDirectory: Path = Paths.get("src", "main", "resources")
    return resourceDirectory.toFile().absolutePath
  }
}
