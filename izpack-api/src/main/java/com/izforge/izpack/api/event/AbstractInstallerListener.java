/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
 * Abstract implementation of {@link InstallerListener}.
 * <p/>
 * This provides no-op versions of each of the methods, to simplify implementation of listeners that only need
 * some methods.
 *
 * @author Tim Anderson
 */
public abstract class AbstractInstallerListener implements InstallerListener
{
    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @throws IzPackException for any error
     */
    @Override
    public void beforePacks(List<Pack> packs)
    {
    }

    /**
     * Invoked before packs are installed.
     * <p>
     *     The default implementation calls {@link #beforePacks(List)} for
     *     backwards compatibility.
     * </p>
     *
     * @param packs the packs to be installed
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void beforePacks(List<Pack> packs, ProgressListener listener)
    {
        beforePacks(packs);
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack  the pack
     * @throws IzPackException for any error
     */
    @Override
    public void beforePack(Pack pack)
    {
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack  the pack
     * @throws IzPackException for any error
     */
    @Override
    public void afterPack(Pack pack)
    {
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
    }

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDir(File dir, PackFile packFile, Pack pack)
    {
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void afterDir(File dir, PackFile packFile, Pack pack)
    {
    }

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void beforeFile(File file, PackFile packFile, Pack pack)
    {
    }

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void afterFile(File file, PackFile packFile, Pack pack)
    {
    }

    /**
     * Determines if the listener should be notified of every file and directory installation.
     *
     * @return <tt>false</tt>
     */
    @Override
    public boolean isFileListener()
    {
        return false;
    }
}
