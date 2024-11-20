/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
 * Copyright 2012 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.api.event;

import com.izforge.izpack.api.exception.IzPackException;

import java.io.File;
import java.util.List;

/**
 * Implementations of this class are used to handle customizing uninstallation. The defined methods
 * are called from the destroyer at different, well defined points of uninstallation.
 *
 * @author Klaus Bartz
 * @author Tim Anderson
 */
public interface UninstallerListener extends InstallationListener
{
    /**
     * Invoked before files are deleted.
     *
     * @param files all files which should be deleted
     * @throws IzPackException for any error
     */
    void beforeDelete(List<File> files);

    /**
     * Invoked before files are deleted.
     *
     * @param files all files which should be deleted
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    void beforeDelete(List<File> files, ProgressListener listener);

    /**
     * Invoked before a file is deleted.
     *
     * @param file the file which will be deleted
     * @throws IzPackException for any error
     */
    void beforeDelete(File file);

    /**
     * Invoked after a file is deleted.
     *
     * @param file the file which was deleted
     * @throws IzPackException for any error
     */
    void afterDelete(File file);

    /**
     * Invoked after files are deleted.
     *
     * @param files    the files which where deleted
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    void afterDelete(List<File> files, ProgressListener listener);
}
