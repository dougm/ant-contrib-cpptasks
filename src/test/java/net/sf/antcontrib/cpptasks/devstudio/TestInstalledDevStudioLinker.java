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
package net.sf.antcontrib.cpptasks.devstudio;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Test for Microsoft Developer Studio linker
 * <p>
 * Override create to test concrete compiler implementations
 * </p>
 */
public class TestInstalledDevStudioLinker extends TestDevStudioLinker {
    @Test
    public void testGetLibraryPath() {
        File[] libpath = DevStudioLinker.getInstance().getLibraryPath();
        //
        //  unless you tweak the library path
        //       it should have more than three entries
        assertTrue(libpath.length >= 2);
        //
        //   check if these files can be found         
        //
        String[] libnames = new String[]{"kernel32.lib",
                "advapi32.lib", "msvcrt.lib", "mfc42.lib", "mfc70.lib"};
        boolean[] libfound = new boolean[libnames.length];
        for (File file : libpath) {
            for (int j = 0; j < libnames.length; j++) {
                File libfile = new File(file, libnames[j]);
                if (libfile.exists()) {
                    libfound[j] = true;
                }
            }
        }
        assertTrue("kernel32 not found", libfound[0]);
        assertTrue("advapi32 not found", libfound[1]);
        assertTrue("msvcrt not found", libfound[2]);
        assertTrue("mfc42.lib or mfc70.lib not found", libfound[3] || libfound[4]);
    }
}
