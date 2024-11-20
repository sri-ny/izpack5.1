/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
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

package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceInterruptedException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;

import java.io.InputStream;


/**
 * Abstract implementation of the {@link PackResources} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPackResources implements PackResources
{
    /**
     * Temporary directory for web installers.
     */
    protected static final String WEB_TEMP_SUB_PATH = "/IzpackWebTemp";

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * Constructs an {@code AbstractPackResources}.
     *
     * @param resources   the resources
     * @param installData the installation data
     */
    public AbstractPackResources(Resources resources, InstallData installData)
    {
        this.installData = installData;
        this.resources = resources;
    }

    /**
     * Returns the stream to a resource.
     *
     * @param name the resource name
     * @return a stream to the resource
     * @throws ResourceNotFoundException    if the resource cannot be found
     * @throws ResourceInterruptedException if resource retrieval is interrupted
     * @throws ResourceException            for any other resource error
     */
    @Override
    public InputStream getPackStream(String name)
    {
        InputStream result;
        String webDirURL = installData.getInfo().getWebDirURL();

        if (webDirURL == null)
        {
            result = getLocalPackStream(name);
        }
        else
        {
            result = getWebPackStream(name, webDirURL);
        }

        return result;
    }

    /**
     * Returns the stream to a resource.
     *
     * @param name the resource name
     * @return a stream to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws ResourceException         for any other resource error
     */
    @Override
    public InputStream getInputStream(String name)
    {
        // TODO - this is invoked to get multi-volume info, so should check on web dir.
        return resources.getInputStream(name);
    }

    /**
     * Returns a stream to a local pack.
     *
     * @param name the pack name
     * @return the pack stream
     */
    private InputStream getLocalPackStream(String name)
    {
        return resources.getInputStream("packs/pack-" + name);
    }

    /**
     * Returns the stream to a web-based pack resource.
     *
     * @param name      the resource name
     * @param webDirURL the web URL to load the resource from
     * @return a stream to the resource
     * @throws ResourceNotFoundException    if the resource cannot be found
     * @throws ResourceInterruptedException if resource retrieval is interrupted
     */
    protected abstract InputStream getWebPackStream(String name, String webDirURL);

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return installData;
    }

}
