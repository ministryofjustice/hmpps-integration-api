package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileManager {
  /**
   * Function to create a file at the specified path with the specified content
   *
   * @param path The absolute path to where the file should be written
   * @param content The file content
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
   * @param path The absolute path to the file
   * @return The file if exists
   */
  fun read(path: String): File? = File(path).takeIf { it.exists() }

  /**
   * Function that creates a folder at the specified path if it does not already exist
   *
   * @param path The absolute path to where the folder should exist
   */
  fun checkOrCreateDirectory(path: String) {
    Files.createDirectories(Paths.get(path))
  }

  /**
   * Function that reads file from a path relative to the main resource folder
   *
   * @param path The path of the file relative to the main resources folder
   * @return The file if it exists
   */
  fun readFileFromResourcesFolder(path: String): File? = read("${getResourcesFolderPath()}/$path")

  /**
   * Function that reads the file contents from a path relative to the main resources folder
   *
   * @param path The path of the file relative to the main resources folder
   * @return The contents of the file if the file exists
   */
  fun readFileContentsFromResourcesFolder(path: String): String? = read("${getResourcesFolderPath()}/$path")?.readText()

  /**
   * Function that returns the resources folder
   * @return The absolute path of the main resources folder
   */
  fun getResourcesFolderPath(): String {
    val resourceDirectory: Path = Paths.get("src", "main", "resources")
    return resourceDirectory.toFile().absolutePath
  }
}
