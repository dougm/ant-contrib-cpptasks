/*
 *
 * Copyright 2002-2004 The Ant-Contrib project
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
package net.sf.antcontrib.cpptasks.openwatcom;

import net.sf.antcontrib.cpptasks.CCTask;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinker;
import net.sf.antcontrib.cpptasks.compiler.CommandLineLinkerConfiguration;
import net.sf.antcontrib.cpptasks.compiler.LinkType;
import net.sf.antcontrib.cpptasks.compiler.Linker;
import net.sf.antcontrib.cpptasks.types.LibraryTypeEnum;

import java.io.File;
import java.util.Vector;

import static net.sf.antcontrib.cpptasks.CUtil.getPathFromEnvironment;

/**
 * Adapter for the OpenWatcom Librarian.
 *
 * @author Curt Arnold
 */
public final class OpenWatcomLibrarian extends CommandLineLinker {
    /**
     * Singleton.
     */
    private static final OpenWatcomLibrarian INSTANCE = new OpenWatcomLibrarian();

    /**
     * Singleton accessor.
     *
     * @return OpenWatcomLibrarian librarian instance
     */
    public static OpenWatcomLibrarian getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor.
     */
    private OpenWatcomLibrarian() {
        super("wlib", null, new String[]{".obj"}, new String[0], ".lib",
                false, null);
    }

    /**
     * Add base address.
     *
     * @param base long base address
     * @param args Vector command line arguments
     */
    protected void addBase(final long base, final Vector<String> args) {
    }

    /**
     * Add alternative entry point.
     *
     * @param entry String entry point
     * @param args  Vector command line arguments
     */
    protected void addEntry(final String entry, final Vector<String> args) {
    }

    /**
     * Add fixed parameter.
     *
     * @param fixed Boolean true if fixed
     * @param args  Vector command line arguments
     */
    protected void addFixed(final Boolean fixed, final Vector<String> args) {
    }

    /**
     * Add implied arguments.
     *
     * @param debug    boolean true if debugging
     * @param linkType LinkType link type
     * @param args     Vector command line arguments
     */
    protected void addImpliedArgs(final boolean debug,
                                  final LinkType linkType,
                                  final Vector<String> args) {
    }

    /**
     * Add incremental option.
     *
     * @param incremental boolean true if incremental
     * @param args        Vector command line arguments
     */
    protected void addIncremental(final boolean incremental,
                                  final Vector<String> args) {
    }

    /**
     * Add map option.
     *
     * @param map  boolean true to create map file
     * @param args Vector command line argument
     */
    protected void addMap(final boolean map,
                          final Vector<String> args) {
    }

    /**
     * Add stack size option.
     *
     * @param stack int stack size
     * @param args  Vector command line arguments
     */
    protected void addStack(final int stack,
                            final Vector<String> args) {
    }

    /**
     * Get command file switch.
     *
     * @param cmdFile String command file
     * @return String command file switch
     */
    protected String getCommandFileSwitch(final String cmdFile) {
        return OpenWatcomProcessor.getCommandFileSwitch(cmdFile);
    }


    /**
     * Get library search path.
     *
     * @return File[] library search path
     */
    public File[] getLibraryPath() {
        return getPathFromEnvironment("LIB", ";");
    }

    /**
     * Get file selectors for specified library names.
     *
     * @param libnames String[] library names
     * @param libType  LibraryTypeEnum library type enum
     * @return String[] file selection patterns
     */
    public String[] getLibraryPatterns(final String[] libnames,
                                       final LibraryTypeEnum libType) {
        return OpenWatcomProcessor.getLibraryPatterns(libnames, libType);
    }

    /**
     * Get linker.
     *
     * @param type LinkType link type
     * @return Linker linker
     */
    public Linker getLinker(final LinkType type) {
        return OpenWatcomCLinker.getInstance().getLinker(type);
    }

    /**
     * Gets maximum command line.
     *
     * @return int maximum command line
     */
    public int getMaximumCommandLength() {
        return 1024;
    }

    /**
     * Create output file switch.
     *
     * @param outFile String output file switch
     * @return String[] output file switch
     */
    public String[] getOutputFileSwitch(final String outFile) {
        return OpenWatcomProcessor.getOutputFileSwitch(outFile);
    }

    /**
     * Gets case-sensitivity of processor.
     *
     * @return boolean true if case sensitive
     */
    public boolean isCaseSensitive() {
        return OpenWatcomProcessor.isCaseSensitive();
    }

    /**
     * Builds a library.
     *
     * @param task        task
     * @param outputFile  generated library
     * @param sourceFiles object files
     * @param config      linker configuration
     */
    public void link(final CCTask task,
                     final File outputFile,
                     final String[] sourceFiles,
                     final CommandLineLinkerConfiguration config) {
        //
        //  delete any existing library
        outputFile.delete();
        //
        //  build a new library
        super.link(task, outputFile, sourceFiles, config);
    }

    /**
     * Prepares argument list for exec command.
     *
     * @param task        task
     * @param outputDir   output directory
     * @param outputName  output file name
     * @param sourceFiles object files
     * @param config      linker configuration
     * @return arguments for runTask
     */
    protected String[] prepareArguments(final CCTask task,
                                        final String outputDir,
                                        final String outputName,
                                        final String[] sourceFiles,
                                        final CommandLineLinkerConfiguration config) {
        String[] preargs = config.getPreArguments();
        String[] endargs = config.getEndArguments();
        StringBuilder buf = new StringBuilder();
        Vector<String> execArgs = new Vector<String>();

        execArgs.addElement(this.getCommand());
        String outputFileName = new File(outputDir, outputName).toString();
        execArgs.addElement(quoteFilename(buf, outputFileName));

        for (String prearg : preargs) {
            execArgs.addElement(prearg);
        }

        int objBytes = 0;

        for (String sourceFile : sourceFiles) {
            String last4 = sourceFile.substring(sourceFile.length() - 4).toLowerCase();
            if (!last4.equals(".def") && !last4.equals(".res") && !last4.equals(".lib")) {
                execArgs.addElement("+" + quoteFilename(buf, sourceFile));
                objBytes += new File(sourceFile).length();
            }
        }

        for (String endarg : endargs) {
            execArgs.addElement(endarg);
        }

        return execArgs.toArray(new String[0]);
    }
}
