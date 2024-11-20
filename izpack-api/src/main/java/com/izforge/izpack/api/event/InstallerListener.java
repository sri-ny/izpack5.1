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

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.IzPackException;

import java.io.File;
import java.util.List;

/**
 * <p>
 * Implementations of this class are used to handle customizing installation. The defined methods
 * are called from the unpacker at different, well defined points of installation.
 * </p>
 *
 * @author Klaus Bartz
 * @author Tim Anderson
 */
public interface InstallerListener extends InstallationListener
{
    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @throws IzPackException for any error
     */
    void beforePacks(List<Pack> packs);

    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    void beforePacks(List<Pack> packs, ProgressListener listener);

    /**
     * Invoked before a pack is installed.
     *
     * @param pack  the pack
     * @throws IzPackException for any error
     */
    void beforePack(Pack pack);

    /**
     * Invoked after a pack is installed.
     *
     * @param pack the pack
     * @throws IzPackException for any error
     */
    void afterPack(Pack pack);

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    void afterPacks(List<Pack> packs, ProgressListener listener);

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    void beforeDir(File dir, PackFile packFile, Pack pack);

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    void afterDir(File dir, PackFile packFile, Pack pack);

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    void beforeFile(File file, PackFile packFile, Pack pack);

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    void afterFile(File file, PackFile packFile, Pack pack);
}
