@file:Suppress("UnnecessaryVariable")
//https://github.com/dkandalov/live-plugin#how-to-start-writing-plugins
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import liveplugin.*
import java.io.File

val userHome: String = System.getProperty("user.home")
val m2Repository = "/.m2/repository"
val groupId = "com.github.davidmoten"
val artifactId = "openapi-to-plantuml"
val version = "2022-05-11"
val scriptExtension = "cmd"
val mavenJarFile = getMavenJarFile(groupId, artifactId, version)
val openAPIFileTypes = LinkedHashSet<String>()
openAPIFileTypes.add("YAML")
openAPIFileTypes.add("JSON")
registerAction(id = "OpenAPI 2 Plant UML + SVG Script Files Popup", keyStroke = "ctrl shift P") { event: AnActionEvent ->
    val actionGroup = PopupActionGroup("OpenAPI 2 Plant UML + SVG Script Files",
            AnAction("OpenAPI 2 Plant UML + SVG Folders Script Files") { popupEvent ->
                try {
                    val presentation = event.presentation
                    val message = StringBuilder(presentation.text).append("<br/>")
                    val currentProject: Project? = event.project
                    if (currentProject != null) {
                        val projectBasePathFile = getProjectBasePathFile(popupEvent, message)
                        val cliCommands = getFolderCLIScripts(event, mavenJarFile, message)
                        if (projectBasePathFile != null && cliCommands.isNotEmpty()) {
                            show(message)
                        }
                    }
                } catch (e: Exception) {
                    show(e)
                }
            },
            AnAction("OpenAPI 2 Plant UML + SVG Files Script Files") { popupEvent ->
                try {
                    val presentation = event.presentation
                    val message = StringBuilder(presentation.text).append("<br/>")
                    val currentProject: Project? = event.project
                    if (currentProject != null) {
                        val projectBasePathFile = getProjectBasePathFile(popupEvent, message)
                        val cliCommands = getFileCLIScripts(event, mavenJarFile, message)
                        if (projectBasePathFile != null && cliCommands.isNotEmpty()) {
                            show(message)
                        }
                    }
                    show(message)
                } catch (e: Exception) {
                    show(e)
                }
            },
            Separator.getInstance(),
            AnAction("Execute Shell Command") {
                val command = Messages.showInputDialog("Enter a command (e.g. 'ls'):", "Dialog Title", null)
                runCLICommand(command)
            },
            AnAction("Edit Popup...") {
                it.project?.openInEditor("$pluginPath/plugin.kts")
            },
            PopupActionGroup("Documentation",
                    AnAction("IntelliJ API Mini-Cheatsheet") {
                        openInBrowser("https://github.com/dkandalov/live-plugin/blob/master/IntellijApiCheatSheet.md")
                    },
                    AnAction("Plugin SDK - Fundamentals") {
                        openInBrowser("https://www.jetbrains.org/intellij/sdk/docs/platform/fundamentals.html")
                    }
            )
    )
    actionGroup.createPopup(event.dataContext)
            .showCenteredInCurrentWindow(event.project!!)
}

if (!isIdeStartup) show("Loaded 'Plant UML Popup'<br/>Use Ctrl+Shift+P to run it")

fun getProjectBasePathFile(popupEvent: AnActionEvent, message: StringBuilder): File? {
    val projectName = popupEvent.project?.name
    val projectBasePath = popupEvent.project?.basePath
    message.append("Project ").append(projectName)
    if (projectBasePath != null) {
        val projectBasePathFile = File(projectBasePath)
        message.append(" - ")
                .append("BasePath ").append(projectBasePathFile.absolutePath).append("<br/>")
        return projectBasePathFile
    }
    return null
}

fun getMavenJarFile(groupId: String, artifactId: String, version: String): File {
    val groupIdPath = groupId.replace('.', '/')
    val jarFile = File(userHome, "$m2Repository/$groupIdPath/$artifactId/$version-SNAPSHOT/$artifactId-$version-SNAPSHOT-jar-with-dependencies.jar")
    return jarFile
}

fun getFolderCLIScripts(event: AnActionEvent, mavenJarFile: File, message: StringBuilder): LinkedHashMap<String, CLICommand> {
    val parentFolders = LinkedHashSet<File>()
    val cliCommands = LinkedHashMap<String, CLICommand>()
    val selectedVirtualFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
    if (selectedVirtualFiles != null && selectedVirtualFiles.isNotEmpty()) {
        selectedVirtualFiles.forEach { virtualFile: VirtualFile ->
            val fileType = virtualFile.fileType.name
            if (openAPIFileTypes.contains(fileType)) {
                val path = virtualFile.path
                val openApiFile = File(path)
                val parentFolder = openApiFile.parentFile
                if (!parentFolders.contains(parentFolder)) {
                    parentFolders.add(parentFolder)
                    val folderCLICommand = getFolderCLICommand(mavenJarFile, parentFolder)
                    val cliScript = folderCLICommand.toCLIScript()
                    val cliScriptFile = folderCLICommand.cliScriptFile
                    val cliScriptFileName = cliScriptFile.name
                    cliScriptFile.writeText(cliScript)
                    message.append(cliScript).append("<br/>saved to ")
                            .append(cliScriptFile.absolutePath).append("<br/>")
                            .append("<br/>")
                    cliCommands[cliScriptFileName] = folderCLICommand
                }
            }
        }
    }
    return cliCommands
}

fun getFolderCLICommand(mavenJarFile: File, parentFolder: File): CLICommand {
    val mavenJarFilePath = mavenJarFile.absolutePath
    val openApiFolderPath = parentFolder.absolutePath
    val parentFolderName = parentFolder.name
    val cliScriptFileName = "$parentFolderName OpenAPI Folder 2 Plant UML.$scriptExtension"
    val cliScriptFile = File(parentFolder, cliScriptFileName)
    val folderCLICommand = CLICommand(cliScriptFile,"java", "-jar", mavenJarFilePath, openApiFolderPath, openApiFolderPath)
    return folderCLICommand
}

fun getFileCLIScripts(event: AnActionEvent, mavenJarFile: File, message: StringBuilder): LinkedHashMap<String, CLICommand> {
    val openApiFiles = LinkedHashSet<File>()
    val cliCommands = LinkedHashMap<String, CLICommand>()
    val selectedVirtualFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
    if (selectedVirtualFiles != null && selectedVirtualFiles.isNotEmpty()) {
        selectedVirtualFiles.forEach { virtualFile: VirtualFile ->
            val fileType = virtualFile.fileType.name
            if (openAPIFileTypes.contains(fileType)) {
                val path = virtualFile.path
                val openApiFile = File(path)
                if (!openApiFiles.contains(openApiFile)) {
                    openApiFiles.add(openApiFile)
                    val fileCLICommand = getFileCLICommand(mavenJarFile, openApiFile)
                    val cliScript = fileCLICommand.toCLIScript()
                    val cliScriptFile = fileCLICommand.cliScriptFile
                    val cliScriptFileName = cliScriptFile.name
                    cliScriptFile.writeText(cliScript)
                    message.append(cliScript).append("<br/>saved to ")
                            .append(cliScriptFile.absolutePath).append("<br/>")
                            .append("<br/>")
                    cliCommands[cliScriptFileName] = fileCLICommand
                }
            }
        }
    }
    return cliCommands
}

fun getFileCLICommand(mavenJarFile: File, openApiFile: File): CLICommand {
    val mavenJarFilePath = mavenJarFile.absolutePath
    val openApiFilePath = openApiFile.absolutePath
    val openApiFolderPath = openApiFile.parentFile.absolutePath
    val openApiFileName = openApiFile.name
    val scriptsFolder = openApiFile.parentFile
    val cliScriptFileName = "$openApiFileName OpenAPI File 2 Plant UML.$scriptExtension"
    val cliScriptFile = File(scriptsFolder, cliScriptFileName)
    val fileCLICommand = CLICommand(cliScriptFile,"java", "-jar", mavenJarFilePath, openApiFilePath, openApiFolderPath)
    return fileCLICommand
}

fun runCLIScripts(cliCommands: LinkedHashMap<String, CLICommand>, message: StringBuilder) {
    for (cliCommandEntry: Map.Entry<String, CLICommand> in cliCommands) {
        val name = cliCommandEntry.key
        val cliCommand = cliCommandEntry.value
        val cliScript = cliCommand.toCLIScript()
        message.append(name).append(" - ")
                .append(cliScript).append("<br/>")
                .append("<br/>")
        runCLIScript(cliScript)
    }
}

fun runCLIScript(command: @NlsSafe String?) {
    if (command != null) show(runShellScript(command).toString().replace("\n", "<br/>"))
}

fun runCLICommands(cliCommands: LinkedHashMap<String, CLICommand>, message: StringBuilder) {
    for (cliCommandEntry: Map.Entry<String, CLICommand> in cliCommands) {
        val name = cliCommandEntry.key
        val cliCommand = cliCommandEntry.value
        val cliScript = cliCommand.toCLIScript()
        message.append(name).append(" - ")
                .append(cliScript).append("<br/>")
                .append("<br/>")
        runCLICommand(cliCommand.command, *cliCommand.arguments)
    }
}

fun runCLICommand(command: @NlsSafe String?, vararg arguments: String) {
    if (command != null) show(runShellCommand(command, *arguments).toString().replace("\n", "<br/>"))
}

class CLICommand(val cliScriptFile: File, val command: @NlsSafe String?, vararg val arguments: String) {
    fun toCLIScript(): String {
        val cliScriptStringBuilder = StringBuilder()
        cliScriptStringBuilder.append(command)
        for (argument: String in arguments)
            cliScriptStringBuilder.append(" \"").append(argument).append("\"")
        val cliScript = cliScriptStringBuilder.toString()
        return cliScript
    }
}

