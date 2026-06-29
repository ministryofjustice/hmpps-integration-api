package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.documentation

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import tools.jackson.core.json.JsonReadFeature
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.GatewayMetadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.DocumentationManager
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class EndpointToGatewayDocumentationManager : DocumentationManager {
  val srcPath = "src/main/kotlin"
  val controllerPath = "uk/gov/justice/digital/hmpps/hmppsintegrationapi/controllers"

  fun getData(): Map<String, List<Function>> {
    val controllerFilePath = Paths.get("$srcPath/$controllerPath")
    val files =
      Files
        .walk(controllerFilePath)
        .filter { it.absolutePathString().endsWith(".kt") }
        .map {
          it.absolutePathString()
        }.toList()

    val srcCodeFiles =
      files.map {
        val path = it.split("$srcPath/").last()
        val name = path.split("/").last()
        SourceFile(path, name)
      }

    return srcCodeFiles
      .filter { it.functions != null }
      .flatMap {
        it.functions!!.filter { func -> func.endpoint != null }.map { func ->
          Pair("${func.httpMethod} ${func.endpoint!!}", func.recurseFunctionCalls())
        }
      }.toMap()
  }

  override fun generate() {
    TODO("Not yet implemented")
  }
}

data class Function(
  val inClass: String,
  val name: String? = null,
  val endpoint: String? = null,
  val httpMethod: String? = null,
  val body: String? = null,
  val metadata: GatewayMetadata? = null,
  val functionCalls: List<Function?> = emptyList(),
) {
  fun recurseFunctionCalls(): List<Function> {
    val funcs = mutableListOf<Function>()
    this.functionCalls.filterNotNull().forEach {
      funcs.add(it)
      funcs.addAll(it.recurseFunctionCalls())
    }
    return funcs
  }
}

data class Declaration(
  val name: String? = null,
  val func: Function? = null,
)

class SourceFile(
  filePath: String,
  fileName: String,
) {
  companion object {
    val project =
      KotlinCoreEnvironment
        .createForProduction(
          Disposer.newDisposable(),
          CompilerConfiguration(),
          EnvironmentConfigFiles.JVM_CONFIG_FILES,
        ).project

    val jsonMapper: JsonMapper =
      JsonMapper
        .builder()
        .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .build()
  }

  private fun createKtFile(
    codeString: String,
    fileName: String,
  ) = PsiManager
    .getInstance(project)
    .findFile(
      LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString),
    ) as KtFile

  fun getFile(
    filePath: String,
    fileName: String,
  ): KtFile {
    val src = Paths.get("src", "main", "kotlin", filePath).toFile().readText()
    return createKtFile(src, fileName)
  }

  private val ktFile = getFile(filePath, fileName)
  private val ktClass = ktFile.children.filterIsInstance<KtClass>().first()
  private val ktClassBody = ktClass.children.filterIsInstance<KtClassBody>().firstOrNull()
  private val controllerEndpoint =
    ktClass.annotationEntries.firstOrNull { it.text.contains("Mapping") }?.text?.let {
      extractFromAnnotation(it)
    }

  // Build a list of imports that the file uses
  val imports =
    ktFile.importList
      ?.imports
      ?.filterIsInstance<KtImportDirective>()
      ?.filter {
        it.importPath
          ?.fqName
          ?.asString()
          ?.contains("hmpps.hmppsintegrationapi") == true
      }?.associate {
        val dir =
          it.importPath
            ?.fqName
            ?.asString()
            ?.split(".")
        Pair(dir?.last(), dir?.joinToString("/"))
      }

  // Get the constructor params
  val constructorDeclarations =
    ktClass.children
      .filterIsInstance<KtPrimaryConstructor>()
      .flatMap {
        it.children.filterIsInstance<KtParameterList>().flatMap { parameterList ->
          parameterList.children
            .filterIsInstance<KtParameter>()
            .filter { type ->
              val declarationType =
                type.children
                  .filterIsInstance<KtTypeReference>()
                  .first()
                  .getTypeText()
              imports?.get(declarationType) != null
            }.map { type ->
              val declarationType =
                type.children
                  .filterIsInstance<KtTypeReference>()
                  .first()
                  .getTypeText()
              val declarationPath = imports?.get(declarationType)!!
              val declaration = SourceFile("$declarationPath.kt", "$declarationType.kt")
              Pair(
                type.name,
                declaration,
              )
            }
        }
      }.toMap()

  // Get the class declarations
  val otherDeclarations =
    ktClassBody
      ?.children
      ?.filterIsInstance<KtProperty>()
      ?.flatMap { prop ->
        prop.children
          .filterIsInstance<KtCallExpression>()
          .filter { type ->
            val declarationType =
              type.children
                .filterIsInstance<KtNameReferenceExpression>()
                .first()
                .text
            imports?.get(declarationType) != null
          }.map {
            val declarationType =
              it.children
                .filterIsInstance<KtNameReferenceExpression>()
                .first()
                .text
            val declarationPath = imports?.get(declarationType)!!
            val declaration = SourceFile("$declarationPath.kt", "$declarationType.kt")
            Pair(prop.name, declaration)
          }
      }?.toMap()

  // Merge all declarations into one
  val allDeclarations = if (otherDeclarations != null) constructorDeclarations + otherDeclarations else constructorDeclarations

  // Identify all available function calls
  val availableFunctionCalls =
    allDeclarations.entries.flatMap { entry ->
      entry.value.functions?.map { func ->
        Declaration(name = entry.key + "." + func.name, func)
      } ?: emptyList()
    }

  fun associateToFunctions(body: String?): List<Function?> = availableFunctionCalls.filter { it.name?.let { other -> body?.contains(other) } == true }.map { it.func }

  val metadata =
    ktClassBody
      ?.children
      ?.filterIsInstance<KtNamedFunction>()
      ?.firstOrNull {
        it.name?.lowercase() == "metadata"
      }?.let {
        val x =
          it.children
            .filterIsInstance<KtExpression>()
            .firstOrNull()
            ?.text!!
        val json = x.replace("GatewayMetadata(", "{").replace("=", ":").replace(")", "}")
        jsonMapper.readValue(json, GatewayMetadata::class.java)
      }

  val functions =
    ktClassBody?.children?.filterIsInstance<KtNamedFunction>()?.map {
      val bodyString =
        it.children
          .filterIsInstance<KtExpression>()
          .firstOrNull()
          ?.text
      val body = bodyString?.replace("\n\\s+[.]".toRegex(), ".")
      val requestAnnotation = it.annotationEntries.firstOrNull { annotation -> annotation.text.contains("Mapping") }?.text
      val endpoint = requestAnnotation?.let { extractFromAnnotation(requestAnnotation) }
      val fullEndpoint = if (controllerEndpoint?.first != null && endpoint?.first != null) "${controllerEndpoint.first}/${endpoint.first}" else null

      // Now list all of the functions it calls
      val funcs = associateToFunctions(body)

      val func =
        Function(
          inClass = fileName,
          name = it.name,
          endpoint = fullEndpoint?.replace("//", "/"),
          httpMethod = endpoint?.second,
          body = body,
          metadata = metadata,
          functionCalls = funcs,
        )
      func
    }
}

// Function to strip common annotation characters
fun String.strip(): String {
  var string = this
  listOf("[\"", "\"", "[", "]", ")", "RequestMethod.").forEach {
    string = string.replace(it, "")
  }
  return string
}

fun extractFromAnnotation(annotation: String): Pair<String, String> {
  return if (annotation.contains("@Request")) {
    var endpoint = ""
    var method = "GET"
    annotation
      .split("Mapping(")
      .last()
      .replace(" ", "")
      .replace("],", "|")
      .split("|")
      .forEach {
        if (it.contains("value=")) {
          endpoint = it.split("value=").last().strip()
        } else if (it.contains("\")")) {
          endpoint = it.strip()
        }
        if (it.contains("method=")) {
          method = it.split("method=").last().strip()
        }
      }
    Pair(endpoint, method)
  } else {
    val x = if (annotation.contains("Mapping(\"")) annotation.split("Mapping(\"") else annotation.split("Mapping")
    if (x.size == 1) {
      return Pair("", "")
    }
    val endpoint = x.last().replace("\")", "")
    val method = x.first().replace("@", "")
    Pair(endpoint, method.uppercase())
  }
}
