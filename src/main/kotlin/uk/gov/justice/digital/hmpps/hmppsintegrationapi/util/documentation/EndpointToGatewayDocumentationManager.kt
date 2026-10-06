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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class EndpointToGatewayDocumentationManager(
  val fileManager: FileManager,
) : DocumentationManager {
  val srcPath = "src/main/kotlin"
  val controllerPath = "uk/gov/justice/digital/hmpps/hmppsintegrationapi/controllers"

  override fun generate() {
    val fileName = "README.md"
    val path = fileManager.getSourcePath("controllers")
    val data = getData()
    val contents = createContent(data)
    fileManager.write("$path/$fileName", contents)
  }

  fun swaggerLink(link: String): String {
    val badge = "[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](API_DOC_LINK)"
    return badge.replace("API_DOC_LINK", link)
  }

  fun formatData(endpoints: Map<String, List<Function>>): List<Pair<String, String>> =
    endpoints.entries.map { (endpointName, gateways) ->
      val endpoint = endpointName.split("\", \"").first()
      val x =
        gateways.map {
          "${it.inClass} (${it.name}) ${it.metadata?.apiDocUrl?.let {link -> swaggerLink(link) }}"
        }
      Pair(endpoint, x.joinToString("<br>"))
    }

  fun createContent(endpoints: Map<String, List<Function>>): String {
    val content = StringBuilder()
    val leftSize = 83
    val rightSize = 1474
    val leftTitle = "Endpoint"
    val rightTitle = "Upstream Gateways"
    content.appendLine("# HMPPS External API Endpoints")
    content.appendLine()
    content.appendLine("## Endpoints and Related Upstream Services")
    content.appendLine()
    val title = "| $leftTitle${" ".repeat(leftSize - leftTitle.length - 1)}| $rightTitle${" ".repeat(rightSize - rightTitle.length - 1)}|"
    val lineBreak = "| ${"-".repeat(leftSize-2)} | ${"-".repeat(rightSize-2)} |"

    content.appendLine("<!-- prettier-ignore -->")
    content.appendLine(title)
    content.appendLine(lineBreak)

    formatData(endpoints).sortedBy { it.first }.forEach {
      content.appendLine("| ${it.first}${" ".repeat(leftSize - it.first.length - 1)}| ${it.second}${" ".repeat(rightSize - it.second.length - 1)}|")
    }
    return content.toString()
  }

  fun getData(): Map<String, List<Function>> {
    val controllerFilePath = Paths.get("$srcPath/$controllerPath/")
    val files =
      Files
        .walk(controllerFilePath)
        .filter { it.absolutePathString().endsWith(".kt") }
        .map {
          it.absolutePathString()
        }.toList()

    // Initial pass to get all functions
    val firstPass =
      files
        .flatMap {
          val funcList = ArrayList<Function>()
          val path = it.split("$srcPath/").last()
          val name = path.split("/").last()
          SourceFile(path, name, funcList)
          funcList
        }.distinct()

    val srcCodeFiles =
      files.map {
        val path = it.split("$srcPath/").last()
        val name = path.split("/").last()
        SourceFile(path, name, ArrayList(firstPass), true)
      }

    return srcCodeFiles
      .filter { it.functions != null }
      .flatMap { src ->
        src.functions!!.filter { func -> func.endpoint != null }.map { func ->
          //
          Pair(
            "${func.httpMethod} ${func.endpoint!!}",
            func
              .recurseFunctionCalls()
              .filter {
                it.inClass.contains("Gateway") &&
                  it.name?.contains("authenticationHeader") == false &&
                  !it.name.contains("getClientToken") &&
                  !it.name.contains("useRestApiClient")
              }.distinctBy { "${it.inClass}:${it.name}" },
          )
        }
      }.toMap()
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
  val calledBy: List<Function>? = emptyList(),
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
  var funcList: ArrayList<Function>,
  val secondPass: Boolean = false,
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
    lazy {
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
        }?.plus(getFilesInPackage(ktFile))
    }

  fun getFilesInPackage(ktFile: KtFile): Map<String, String> {
    val srcPath = "src/main/kotlin"
    val filePath = ktFile.packageFqName.asString().replace('.', '/')
    val controllerFilePath = Paths.get("$srcPath/$filePath/")
    val files =
      Files
        .walk(controllerFilePath)
        .filter { it.absolutePathString().endsWith(".kt") }
        .map {
          it.absolutePathString()
        }.toList()
        .map {
          val path = it.split("$srcPath/")[1].replace(".kt", "")
          val type = path.split("/").last()
          Pair(type, path)
        }
    return files.toMap()
  }

  val packageFqName = ktFile.packageFqName.asString()
  val constructorDeclarations =
    lazy {
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
                imports.value?.get(declarationType) != null
              }.map { type ->
                val declarationType =
                  type.children
                    .filterIsInstance<KtTypeReference>()
                    .first()
                    .getTypeText()
                val declarationPath = imports.value?.get(declarationType) ?: ("$packageFqName/$declarationType")
                val declaration = SourceFile("$declarationPath.kt", "$declarationType.kt", funcList)
                Pair(
                  type.name,
                  declaration,
                )
              }
          }
        }.toMap()
    }

  // Get the class declarations
  val otherDeclarations =
    lazy {
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
              imports.value?.get(declarationType) != null
            }.map {
              val declarationType =
                it.children
                  .filterIsInstance<KtNameReferenceExpression>()
                  .first()
                  .text
              val declarationPath = imports.value?.get(declarationType)!!
              val declaration = SourceFile("$declarationPath.kt", "$declarationType.kt", funcList)
              Pair(prop.name, declaration)
            }
        }?.toMap()
    }

  // Merge all declarations into one
  val allDeclarations = lazy { if (otherDeclarations.value != null) constructorDeclarations.value + otherDeclarations.value!! else constructorDeclarations.value }

  val localFunctions = getFunctions(fileName, false)?.map { Declaration(it.name, it) }

  // Identify all available function calls
  fun availableFunctionCalls() =
    allDeclarations.value.entries
      .flatMap { entry ->
        entry.value.functions?.map { func ->
          Declaration(name = entry.key + "." + func.name, func)
        } ?: emptyList()
      }.plus(allLocalFunctions(ktFile.name))

  fun allLocalFunctions(fileName: String): List<Declaration> =
    getFunctions(fileName, false)?.map {
      Declaration(it.name, it)
    } ?: emptyList()

  fun associateToFunctions(body: String?): List<Function?> =
    availableFunctionCalls()
      .filter { it.name?.let { other -> body?.contains(other) } == true }
      .map { it.func }

  fun associateToFunctionsFromFuncList(body: String?): List<Function?> =
    availableFunctionCallsFromFuncList()
      .filter { it.name?.let { other -> body?.contains(other) } == true }
      .map { it.func }

  fun availableFunctionCallsFromFuncList() =
    funcList
      .map { func ->
        Declaration(func.name, func)
      }.plus(localFunctions ?: emptyList())

  fun metadata() =
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

  fun getFunctions(
    fileName: String,
    associate: Boolean = true,
  ) = ktClassBody?.children?.filterIsInstance<KtNamedFunction>()?.map {
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
    val funcs =
      if (associate) {
        associateToFunctions(body)
      } else {
        associateToFunctionsFromFuncList(body)
      }
    val metadata = metadata()
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
    val add = !secondPass && funcList.filter { f -> f.name == it.name && f.inClass == fileName }.isEmpty()
    if (add) {
      funcList.add(func.copy(functionCalls = emptyList()))
    }
    func
  }

  val functions = getFunctions(fileName)
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
