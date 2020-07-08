/*
 *
 * Copyright 2004-2008 The Ant-Contrib project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.sf.antcontrib.cpptasks.devstudio;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.TargetInfo;
import net.sf.antcontrib.cpptasks.compiler.CommandLineCompilerConfiguration;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinkerConfiguration;
import net.sf.antcontrib.cpptasks.compiler.ProcessorConfiguration;
import net.sf.antcontrib.cpptasks.ide.CommentDef;
import net.sf.antcontrib.cpptasks.ide.DependencyDef;
import net.sf.antcontrib.cpptasks.ide.ProjectDef;
import net.sf.antcontrib.cpptasks.ide.ProjectWriter;
import org.apache.tools.ant.BuildException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import static net.sf.antcontrib.cpptasks.CUtil.getRelativePath;
import static net.sf.antcontrib.cpptasks.CUtil.isSystemPath;
import static net.sf.antcontrib.cpptasks.CUtil.toWindowsPath;

/**
 * <p>
 * Writes a Microsoft Visual Studio 97 or Visual Studio 6 project file.
 * </p>
 * <p>
 * Status: Collects file list but does not pick
 * up libraries and settings from project.
 * </p>
 *
 * @author Curt Arnold
 */
public final class DevStudioProjectWriter implements ProjectWriter {
    /**
     * Visual Studio version.
     */
    private String version;

    /**
     * Constructor.
     *
     * @param versionArg String Visual Studio version.
     */
    public DevStudioProjectWriter(final String versionArg) {
        this.version = versionArg;
    }

    private static String toProjectName(final String name) {
        //
        //    some characters are apparently not allowed in VS project names
        //       but have not been able to find them documented
        //       limiting characters to letters, numbers and hyphens
        StringBuilder projectNameBuf = new StringBuilder(name);
        for (int i = 0; i < projectNameBuf.length(); i++) {
            final char ch = projectNameBuf.charAt(i);
            if (!((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))) {
                projectNameBuf.setCharAt(i, '_');
            }
        }
        return projectNameBuf.toString();

    }

    /**
     * Writes a project definition file.
     *
     * @param fileName   File name base, writer may append appropriate extension
     * @param task       cc task for which to write project
     * @param projectDef project element
     * @param files      source files
     * @param targets    compilation targets
     * @param linkTarget link target
     * @throws IOException if error writing project file
     */
    public void writeProject(final File fileName,
                             final CCTask task,
                             final ProjectDef projectDef,
                             final List<File> files,
                             final Hashtable<String, TargetInfo> targets,
                             final TargetInfo linkTarget) throws IOException {

        //
        //    some characters are apparently not allowed in VS project names
        //       but have not been able to find them documented
        //       limiting characters to letters, numbers and hyphens
        String projectName = projectDef.getName();
        if (projectName != null) {
            projectName = toProjectName(projectName);
        } else {
            projectName = toProjectName(fileName.getName());
        }

        final String basePath = fileName.getAbsoluteFile().getParent();

        File dspFile = new File(fileName + ".dsp");
        if (!projectDef.getOverwrite() && dspFile.exists()) {
            throw new BuildException("Not allowed to overwrite project file "
                    + dspFile.toString());
        }
        File dswFile = new File(fileName + ".dsw");
        if (!projectDef.getOverwrite() && dswFile.exists()) {
            throw new BuildException("Not allowed to overwrite project file "
                    + dswFile.toString());
        }

        CommandLineCompilerConfiguration compilerConfig = getBaseCompilerConfiguration(targets);
        if (compilerConfig == null) {
            throw new BuildException("Unable to generate Visual Studio project "
                    + "when Microsoft C++ is not used.");
        }

        Writer writer = new BufferedWriter(new FileWriter(dspFile));
        writer.write("# Microsoft Developer Studio Project File - Name=\"");
        writer.write(projectName);
        writer.write("\" - Package Owner=<4>\r\n");
        writer.write("# Microsoft Developer Studio Generated Build File, Format Version ");
        writer.write(this.version);
        writer.write("\r\n");
        writer.write("# ** DO NOT EDIT **\r\n\r\n");

        writeComments(writer, projectDef.getComments());

        String outputType = task.getOuttype();
        String subsystem = task.getSubsystem();
        String targtype = "Win32 (x86) Dynamic-Link Library";
        String targid = "0x0102";
        if ("executable".equals(outputType)) {
            if ("console".equals(subsystem)) {
                targtype = "Win32 (x86) Console Application";
                targid = "0x0103";
            } else {
                targtype = "Win32 (x86) Application";
                targid = "0x0101";
            }
        } else if ("static".equals(outputType)) {
            targtype = "Win32 (x86) Static Library";
            targid = "0x0104";
        }
        writer.write("# TARGTYPE \"");
        writer.write(targtype);
        writer.write("\" ");
        writer.write(targid);
        writer.write("\r\n\r\nCFG=");

        writer.write(projectName + " - Win32 Debug");
        writer.write("\r\n");

        writeMessage(writer, projectName, targtype);

        writer.write("# Begin Project\r\n");
        if (version.equals("6.00")) {
            writer.write("# PROP AllowPerConfigDependencies 0\r\n");
        }
        writer.write("# PROP Scc_ProjName \"\"\r\n");
        writer.write("# PROP Scc_LocalPath \"\"\r\n");
        writer.write("CPP=cl.exe\r\n");
        writer.write("MTL=midl.exe\r\n");
        writer.write("RSC=rc.exe\r\n");

        writer.write("\r\n!IF  \"$(CFG)\" == \"" + projectName + " - Win32 Release\"\r\n");

        writeConfig(writer, false, projectDef.getDependencies(), basePath, compilerConfig, linkTarget, targets);

        writer.write("\r\n!ELSEIF  \"$(CFG)\" == \"" + projectName + " - Win32 Debug\"\r\n");

        writeConfig(writer, true, projectDef.getDependencies(), basePath, compilerConfig, linkTarget, targets);

        writer.write("\r\n!ENDIF\r\n");

        writer.write("# Begin Target\r\n\r\n");
        writer.write("# Name \"" + projectName + " - Win32 Release\"\r\n");
        writer.write("# Name \"" + projectName + " - Win32 Debug\"\r\n");


        File[] sortedSources = getSources(files);

        if (version.equals("6.00")) {
            final String sourceFilter = "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat";
            final String headerFilter = "h;hpp;hxx;hm;inl";
            final String resourceFilter = "ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe";

            writer.write("# Begin Group \"Source Files\"\r\n\r\n");
            writer.write("# PROP Default_Filter \"" + sourceFilter + "\"\r\n");

            for (File sortedSource : sortedSources) {
                if (!isGroupMember(headerFilter, sortedSource)
                        && !isGroupMember(resourceFilter, sortedSource)) {
                    writeSource(writer, basePath, sortedSource);
                }
            }
            writer.write("# End Group\r\n");

            writer.write("# Begin Group \"Header Files\"\r\n\r\n");
            writer.write("# PROP Default_Filter \"" + headerFilter + "\"\r\n");

            for (File sortedSource : sortedSources) {
                if (isGroupMember(headerFilter, sortedSource)) {
                    writeSource(writer, basePath, sortedSource);
                }
            }
            writer.write("# End Group\r\n");

            writer.write("# Begin Group \"Resource Files\"\r\n\r\n");
            writer.write("# PROP Default_Filter \"" + resourceFilter + "\"\r\n");

            for (File sortedSource : sortedSources) {
                if (isGroupMember(resourceFilter, sortedSource)) {
                    writeSource(writer, basePath, sortedSource);
                }
            }
            writer.write("# End Group\r\n");

        } else {
            for (File sortedSource : sortedSources) {
                writeSource(writer, basePath, sortedSource);
            }
        }

        writer.write("# End Target\r\n");
        writer.write("# End Project\r\n");
        writer.close();

        //
        //    write workspace file
        //
        writer = new BufferedWriter(new FileWriter(dswFile));
        writeWorkspace(writer, projectDef, projectName, dspFile);
        writer.close();

    }

    private void writeConfig(final Writer writer,
                             boolean isDebug,
                             final List<DependencyDef> dependencies,
                             final String basePath,
                             CommandLineCompilerConfiguration compilerConfig,
                             TargetInfo linkTarget,
                             Hashtable<String, TargetInfo> targets) throws IOException {
        writer.write("# PROP BASE Use_MFC 0\r\n");

        String configType = "Release";
        String configInt = "0";
        String configMacro = "NDEBUG";
        if (isDebug) {
            configType = "Debug";
            configInt = "1";
            configMacro = "_DEBUG";
        }

        writer.write("# PROP BASE Use_Debug_Libraries ");
        writer.write(configInt);
        writer.write("\r\n# PROP BASE Output_Dir \"");
        writer.write(configType);
        writer.write("\"\r\n");
        writer.write("# PROP BASE Intermediate_Dir \"");
        writer.write(configType);
        writer.write("\"\r\n");
        writer.write("# PROP BASE Target_Dir \"\"\r\n");
        writer.write("# PROP Use_MFC 0\r\n");
        writer.write("# PROP Use_Debug_Libraries ");
        writer.write(configInt);
        writer.write("\r\n# PROP Output_Dir \"");
        writer.write(configType);
        writer.write("\"\r\n");
        writer.write("# PROP Intermediate_Dir \"");
        writer.write(configType);
        writer.write("\"\r\n");
        writer.write("# PROP Target_Dir \"\"\r\n");
        writeCompileOptions(writer, isDebug, basePath, compilerConfig);
        writer.write("# ADD BASE MTL /nologo /D \"" + configMacro
                + "\" /mktyplib203 /o NUL /win32\r\n");
        writer.write("# ADD MTL /nologo /D \"" + configMacro
                + "\" /mktyplib203 /o NUL /win32\r\n");
        writer.write("# ADD BASE RSC /l 0x409 /d \"" + configMacro + "\"\r\n");
        writer.write("# ADD RSC /l 0x409 /d \"" + configMacro + "\"\r\n");
        writer.write("BSC32=bscmake.exe\r\n");
        writer.write("# ADD BASE BSC32 /nologo\r\n");
        writer.write("# ADD BSC32 /nologo\r\n");
        writer.write("LINK32=link.exe\r\n");
        writeLinkOptions(writer, isDebug, dependencies, basePath, linkTarget, targets);
    }

    private static void writeWorkspaceProject(final Writer writer,
                                              final String projectName,
                                              final String projectFile,
                                              final List<String> dependsOn) throws IOException {
        writer.write("############################################");
        writer.write("###################################\r\n\r\n");
        String file = projectFile;
        if (!file.startsWith(".") && !file.startsWith("\\") && !file.startsWith("/")) {
            file = ".\\" + file;
        }
        writer.write("Project: \"" + projectName + "\"=\""
                + file
                + "\" - Package Owner=<4>\r\n\r\n");

        writer.write("Package=<5>\r\n{{{\r\n}}}\r\n\r\n");
        writer.write("Package=<4>\r\n{{{\r\n");
        if (dependsOn != null) {
            for (String object : dependsOn) {
                writer.write("    Begin Project Dependency\r\n");
                writer.write("    Project_Dep_Name " + toProjectName(String.valueOf(object)) + "\r\n");
                writer.write("    End Project Dependency\r\n");
            }
        }
        writer.write("}}}\r\n\r\n");

    }

    private void writeWorkspace(final Writer writer,
                                final ProjectDef project,
                                final String projectName,
                                final File dspFile) throws IOException {

        writer.write("Microsoft Developer Studio Workspace File, Format Version ");
        writer.write(version);
        writer.write("\r\n");
        writer.write("# WARNING: DO NOT EDIT OR DELETE");
        writer.write(" THIS WORKSPACE FILE!\r\n\r\n");

        writeComments(writer, project.getComments());


        List<String> projectDeps = new ArrayList<String>();
        String basePath = dspFile.getParent();
        for (DependencyDef dep : project.getDependencies()) {
            if (dep.getFile() != null) {
                String projName = toProjectName(dep.getName());
                projectDeps.add(projName);
                String depProject = toWindowsPath(getRelativePath(basePath,
                        new File(dep.getFile() + ".dsp")));
                writeWorkspaceProject(writer, projName, depProject, dep.getDependsList());
            }
        }

        writeWorkspaceProject(writer, projectName, dspFile.getName(), projectDeps);

        writer.write("############################################");
        writer.write("###################################\r\n\r\n");


        writer.write("Global:\r\n\r\nPackage=<5>\r\n{{{\r\n}}}");
        writer.write("\r\n\r\nPackage=<3>\r\n{{{\r\n}}}\r\n\r\n");

        writer.write("########################################");
        writer.write("#######################################\r\n\r\n");

    }

    /**
     * Returns true if the file has an extension that appears in the group filter.
     *
     * @param filter    String group filter
     * @param candidate File file
     * @return boolean true if member of group
     */
    private boolean isGroupMember(final String filter, final File candidate) {
        String fileName = candidate.getName();
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < fileName.length() - 1) {
            String extension = ";" + fileName.substring(lastDot + 1).toLowerCase() + ";";
            String semiFilter = ";" + filter + ";";
            return semiFilter.contains(extension);
        }
        return false;
    }

    /**
     * Writes the entry for one source file in the project.
     *
     * @param writer      Writer writer
     * @param basePath    String base path for project
     * @param groupMember File project source file
     * @throws IOException if error writing project file
     */
    private void writeSource(final Writer writer,
                             final String basePath,
                             final File groupMember) throws IOException {
        writer.write("# Begin Source File\r\n\r\nSOURCE=");
        String relativePath = getRelativePath(basePath,
                groupMember);
        //
        //  if relative path is just a name (hello.c) then
        //    make it .\hello.c
        if (!relativePath.startsWith(".") && !relativePath.contains(":")
                && !relativePath.startsWith("\\")) {
            relativePath = ".\\" + relativePath;
        }
        writer.write(toWindowsPath(relativePath));
        writer.write("\r\n# End Source File\r\n");
    }

    /**
     * Get alphabetized array of source files.
     *
     * @param sourceList list of source files
     * @return File[] source files
     */
    private File[] getSources(final List<File> sourceList) {
        File[] sortedSources = new File[sourceList.size()];
        sourceList.toArray(sortedSources);
        Arrays.sort(sortedSources, new Comparator<File>() {
            public int compare(final File o1, final File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return sortedSources;
    }

    /**
     * Writes "This is not a makefile" warning.
     *
     * @param writer      Writer writer
     * @param projectName String project name
     * @param targtype    String target type
     * @throws IOException if error writing project
     */

    private void writeMessage(final Writer writer,
                              final String projectName,
                              final String targtype) throws IOException {
        writer.write("!MESSAGE This is not a valid makefile. ");
        writer.write("To build this project using NMAKE,\r\n");
        writer.write("!MESSAGE use the Export Makefile command and run\r\n");
        writer.write("!MESSAGE \r\n");
        writer.write("!MESSAGE NMAKE /f \"");
        writer.write(projectName);
        writer.write(".mak\".\r\n");
        writer.write("!MESSAGE \r\n");
        writer.write("!MESSAGE You can specify a configuration when running NMAKE\r\n");
        writer.write("!MESSAGE by defining the macro CFG on the command line. ");
        writer.write("For example:\r\n");
        writer.write("!MESSAGE \r\n");
        writer.write("!MESSAGE NMAKE /f \"");
        writer.write(projectName);
        writer.write(".mak\" CFG=\"");
        writer.write(projectName);
        writer.write(" - Win32 Debug\"\r\n");
        writer.write("!MESSAGE \r\n");
        writer.write("!MESSAGE Possible choices for configuration are:\r\n");
        writer.write("!MESSAGE \r\n");
        String pattern = "!MESSAGE \"{0} - Win32 {1}\" (based on \"{2}\")\r\n";
        writer.write(MessageFormat.format(pattern, projectName, "Release", targtype));
        writer.write(MessageFormat.format(pattern, projectName, "Debug", targtype));
        writer.write("!MESSAGE \r\n");
        writer.write("\r\n");

    }

    /**
     * Gets the first recognized compiler from the
     * compilation targets.
     *
     * @param targets compilation targets
     * @return representative (hopefully) compiler configuration
     */
    private CommandLineCompilerConfiguration
    getBaseCompilerConfiguration(final Hashtable<String, TargetInfo> targets) {
        //
        //   find first target with an DevStudio C compilation
        //
        CommandLineCompilerConfiguration compilerConfig;
        //
        //   get the first target and assume that it is representative
        //
        for (TargetInfo targetInfo : targets.values()) {
            ProcessorConfiguration config = targetInfo.getConfiguration();
            //
            //   for the first cl compiler
            //
            if (config instanceof CommandLineCompilerConfiguration) {
                compilerConfig = (CommandLineCompilerConfiguration) config;
                if (compilerConfig.getCompiler() instanceof DevStudioCCompiler) {
                    return compilerConfig;
                }
            }
        }
        return null;
    }

    /**
     * Writes compiler options.
     *
     * @param writer         Writer writer
     * @param isDebug        true if debug.
     * @param baseDir        String base directory
     * @param compilerConfig compiler configuration
     * @throws IOException if error on writing project
     */
    private void writeCompileOptions(final Writer writer,
                                     final boolean isDebug,
                                     final String baseDir,
                                     final CommandLineCompilerConfiguration
                                             compilerConfig) throws IOException {
        StringBuilder baseOptions = new StringBuilder(50);
        baseOptions.append("# ADD BASE CPP");
        StringBuilder options = new StringBuilder(50);
        options.append("# ADD CPP");
        File[] includePath = compilerConfig.getIncludePath();
        for (File file : includePath) {
            options.append(" /I \"");
            String relPath = getRelativePath(baseDir, file);
            options.append(toWindowsPath(relPath));
            options.append('"');
        }
        Hashtable<String, String> optionMap = new Hashtable<String, String>();

        if (isDebug) {
            //
            //   release options that should be mapped to debug counterparts
            //
            optionMap.put("/MT", "/MTd");
            optionMap.put("/ML", "/MLd");
            optionMap.put("/MD", "/MDd");
            optionMap.put("/O2", "/Od");
            optionMap.put("/O3", "/Od");
        } else {
            //
            //   debug options that should be mapped to release counterparts
            //
            optionMap.put("/MTD", "/MT");
            optionMap.put("/MLD", "/ML");
            optionMap.put("/MDD", "/MD");
            optionMap.put("/GM", "");
            optionMap.put("/ZI", "");
            optionMap.put("/OD", "/O2");
            optionMap.put("/GZ", "");
        }


        String[] preArgs = compilerConfig.getPreArguments();
        for (String preArg : preArgs) {
            if (preArg.startsWith("/D")) {
                options.append(" /D ");
                baseOptions.append(" /D ");
                String body = preArg.substring(2);
                if (preArg.contains("=")) {
                    options.append(body);
                    baseOptions.append(body);
                } else {
                    StringBuilder buf = new StringBuilder("\"");
                    if ("NDEBUG".equals(body) || "_DEBUG".equals(body)) {
                        if (isDebug) {
                            buf.append("_DEBUG");
                        } else {
                            buf.append("NDEBUG");
                        }
                    } else {
                        buf.append(body);
                    }
                    buf.append("\"");
                    options.append(buf);
                    baseOptions.append(buf);
                }
            } else if (!preArg.startsWith("/I")) {
                String option = preArg;
                String key = option.toUpperCase(Locale.US);
                if (optionMap.containsKey(key)) {
                    option = optionMap.get(key);
                }
                options.append(" ");
                options.append(option);
                baseOptions.append(" ");
                baseOptions.append(option);
            }
        }
        baseOptions.append("\r\n");
        options.append("\r\n");
        writer.write(baseOptions.toString());
        writer.write(options.toString());

    }


    /**
     * Writes link options.
     *
     * @param writer       Writer writer
     * @param basePath     String base path
     * @param dependencies project dependencies, used to suppress explicit linking.
     * @param linkTarget   TargetInfo link target
     * @param targets      Hashtable all targets
     * @throws IOException if unable to write to project file
     */
    private void writeLinkOptions(final Writer writer,
                                  final boolean isDebug,
                                  final List<DependencyDef> dependencies,
                                  final String basePath,
                                  final TargetInfo linkTarget,
                                  final Hashtable<String, TargetInfo> targets) throws IOException {

        StringBuilder baseOptions = new StringBuilder(100);
        StringBuilder options = new StringBuilder(100);
        baseOptions.append("# ADD BASE LINK32");
        options.append("# ADD LINK32");

        ProcessorConfiguration config = linkTarget.getConfiguration();
        if (config instanceof CommandLineLinkerConfiguration) {
            CommandLineLinkerConfiguration linkConfig = (CommandLineLinkerConfiguration) config;

            File[] linkSources = linkTarget.getAllSources();
            for (File linkSource : linkSources) {
                //
                //   if file was not compiled or otherwise generated
                //
                if (targets.get(linkSource.getName()) == null) {
                    //
                    //   if source appears to be a system library or object file
                    //      just output the name of the file (advapi.lib for example)
                    //      otherwise construct a relative path.
                    //
                    String relPath = linkSource.getName();
                    //
                    //   check if file comes from a project dependency
                    //       if it does it should not be explicitly linked
                    boolean fromDependency = false;
                    int dotPos = relPath.indexOf(".");
                    if (dotPos > 0) {
                        String baseName = relPath.substring(0, dotPos);
                        for (DependencyDef depend : dependencies) {
                            if (baseName.compareToIgnoreCase(depend.getName()) == 0) {
                                fromDependency = true;
                            }
                        }
                    }
                    if (!fromDependency) {
                        if (!isSystemPath(linkSource)) {
                            relPath = getRelativePath(basePath, linkSource);
                        }
                        //
                        //   if path has an embedded space then
                        //      must quote
                        if (relPath.contains(" ")) {
                            options.append(" \"");
                            options.append(toWindowsPath(relPath));
                            options.append("\"");
                        } else {
                            options.append(' ');
                            options.append(toWindowsPath(relPath));
                        }
                    }
                }
            }
            String[] preArgs = linkConfig.getPreArguments();
            for (String preArg : preArgs) {
                if (isDebug || !preArg.equals("/DEBUG")) {
                    options.append(' ');
                    options.append(preArg);
                    baseOptions.append(' ');
                    baseOptions.append(preArg);
                }
            }
            String[] endArgs = linkConfig.getEndArguments();
            for (String endArg : endArgs) {
                options.append(' ');
                options.append(endArg);
                baseOptions.append(' ');
                baseOptions.append(endArg);
            }
        }
        baseOptions.append("\r\n");
        options.append("\r\n");
        writer.write(baseOptions.toString());
        writer.write(options.toString());
    }

    private static void writeComments(final Writer writer,
                                      final List<CommentDef> comments) throws IOException {
        for (CommentDef object : comments) {
            String comment = object.getText();
            if (comment != null) {
                int start = 0;
                for (int end = comment.indexOf('\n');
                     end != -1;
                     end = comment.indexOf('\n', start)) {
                    writer.write("#" + comment.substring(start, end) + "\r\n");
                    start = end + 1;
                }
            }
        }
    }
}
