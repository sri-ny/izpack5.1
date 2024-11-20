/*
 * IzPack - Copyright 2001-2017 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.licence;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.util.PanelHelper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Provides shared licence loading logic between console and GUI panels.
 * <p>
 *     A licence resource file is identified by its resource name plus a suffix.
 *     The resource name is built from the simple class name of the IzPanel class
 *     related to the given {@code panelClass}. The suffix is built from the
 *     identifier of the given {@code panel}, if available. Otherwise a default
 *     suffix of {@code "licence"} is used.
 * </p>
 *
 * @author Michael Aichler
 */
class LicenceLoader {

    private final static String DEFAULT_SUFFIX = ".licence";

    private final Panel panel;
    private final Class<?> panelClass;
    private final Resources resources;

    /**
     * Creates a new licence loader.
     *
     * @param panelClass The class of the concrete licence panel implementation.
     * @param panel The panel metadata (needed for the panel identifier).
     * @param resources The resource locator.
     */
    LicenceLoader(Class<?> panelClass, Panel panel, Resources resources) {

        this.panel = panel;
        this.panelClass = panelClass;
        this.resources = resources;
    }

    /**
     * Loads the licence as a URL.
     *
     * @return The URL to the resource.
     * @throws ResourceException If the related IzPanel or the licence resource
     *      cannot be found. The generated error message is ready to be logged.
     */
    URL asURL() throws ResourceException
    {
        Class<?> targetClass = findTargetClass(panelClass);

        String resourceNamePrefix = targetClass.getSimpleName();
        String defaultResourceName = resourceNamePrefix + DEFAULT_SUFFIX;
        String specificResourceName = buildSpecificResourceName(resourceNamePrefix, panel);

        try
        {
            if (null != specificResourceName)
            {
                return resources.getURL(specificResourceName);
            }
        }
        catch (ResourceNotFoundException ignored)
        {
        }

        try
        {
            return resources.getURL(defaultResourceName);
        }
        catch (ResourceNotFoundException ignored)
        {
        }

        String message = buildFinalErrorMessage(resourceNamePrefix, defaultResourceName,
                specificResourceName);

        throw new ResourceNotFoundException(message);
    }

    /**
     * Loads the licence into a string using UTF-8 as encoding.
     *
     * @return A string representation of the licence.
     * @throws ResourceException If the licence could not be found or if file
     *      content could not be converted to UTF-8.
     */
    String asString() throws ResourceException
    {
        return asString("UTF-8");
    }

    /**
     * Loads the licence into a string with the specified {@code encoding}.
     *
     * @param encoding The target character encoding.
     * @return A string representation of the licence in the specified encoding.
     * @throws ResourceException If the licence could not be found or if file
     *      content could not be converted to the specified {@code encoding}.
     */
    String asString(final String encoding) throws ResourceException
    {
        URL url = asURL();
        InputStream in = null;

        try
        {
            in = url.openStream();
            return IOUtils.toString(in, Charsets.toCharset(encoding));
        }
        catch (IOException e)
        {
            throw new ResourceNotFoundException("Cannot convert license document from resource " +
                    url.getFile() + " to text: " + e.getMessage());

        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Finds the IzPanel target class for the given {@code panelClass}.
     *
     * @param panelClass The panel class.
     * @return The related IzPanel class.
     * @throws ResourceException If a related IzPanel class could not be found.
     *
     * @see PanelHelper#getIzPanel(String)
     */
    static Class<?> findTargetClass(Class<?> panelClass) throws ResourceException
    {
        Class<?> targetClass = PanelHelper.getIzPanel(panelClass.getName());

        if (null == targetClass)
        {
            throw new ResourceNotFoundException("No IzPanel implementation found for " +
                    panelClass.getSimpleName());
        }

        return targetClass;
    }

    /**
     * Builds the resource name for a specific panel id.
     *
     * @param resourceNamePrefix The resource name prefix.
     * @param panel The panel providing a specific id.
     * @return The specific resource name or {@code null}, if the panel does not
     *      have an identifier.
     */
    static String buildSpecificResourceName(String resourceNamePrefix, Panel panel)
    {
        if (null != panel)
        {
            if (panel.hasPanelId())
            {
                return resourceNamePrefix + '.' + panel.getPanelId();
            }
        }

        return null;
    }

    /**
     * Builds an informative error message.
     *
     * @param panelType The panel type.
     * @param resourceNames The list of evaluated resource names
     * @return The built error message.
     */
    static String buildFinalErrorMessage(String panelType, String... resourceNames)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Cannot open any of the possible license document resources (");

        boolean isFirst = true;

        for (String resourceName : resourceNames)
        {
            if (null != resourceName)
            {
                if (!isFirst)
                {
                    sb.append(", ");
                }

                sb.append(resourceName);

                if (isFirst)
                {
                    isFirst = false;
                }
            }
        }

        sb.append(") for panel type '");
        sb.append(panelType);
        sb.append("'");

        return sb.toString();
    }
}
