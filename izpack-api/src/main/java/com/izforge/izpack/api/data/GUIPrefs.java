/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.api.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class holds the GUI preferences for an installer.
 *
 * @author Julien Ponge
 */
public class GUIPrefs implements Serializable
{

    static final long serialVersionUID = -9081878949718963824L;

    /**
     * Specifies wether the window will be resizable.
     */
    public boolean resizable;

    /**
     * Specifies the starting window width, in pixels.
     */
    public int width;

    /**
     * Specifies the starting window height, in pixels.
     */
    public int height;

    /**
     * Specifies the OS Look and Feels mappings.
     */
    public Map<String, LookAndFeel> lookAndFeelMapping = new TreeMap<String, LookAndFeel>();

    /**
     * Specifies the modifier.
     */
    public Map<String, String> modifier = new TreeMap<String, String>();

    public static class LookAndFeel implements Serializable
    {
        private final String name;
        private final HashMap<String, String> parameters;

        public LookAndFeel(final String name)
        {
            this.name = name;
            this.parameters = new HashMap<String, String>();
        }

        public String getName()
        {
            return name;
        }

        public String getVariantName()
        {
            return parameters.get("variant");
        }

        public void setParameter(final String name, final String value)
        {
            parameters.put(name, value);
        }

        public boolean is(final LookAndFeels laf)
        {
            return laf.getName().equals(name);
        }
    }
}
