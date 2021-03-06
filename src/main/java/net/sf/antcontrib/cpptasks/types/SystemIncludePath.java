/*
 *
 * Copyright 2001-2004 The Ant-Contrib project
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
package net.sf.antcontrib.cpptasks.types;

import net.sf.antcontrib.cpptasks.CUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * <p>
 * A system include path.
 * </p>
 * <p>
 * Files located using a system include path will not participate in dependency
 * analysis.
 * </p>
 * <p>
 * Standard include paths for a compiler should not be specified since these
 * should be determined from environment variables or configuration files by
 * the compiler adapter.
 * </p>
 * <p>
 * Works like other paths in Ant with with the addition of "if" and "unless"
 * conditions.
 * </p>
 *
 * @author Curt Arnold
 */
public class SystemIncludePath extends ConditionalPath {
    public SystemIncludePath(Project project) {
        super(project);
    }

    public SystemIncludePath(Project p, String path) {
        super(p, path);
    }

    public void execute() throws BuildException {
        throw new BuildException(CUtil.STANDARD_EXCUSE);
    }
}
