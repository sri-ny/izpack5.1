/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

package com.izforge.izpack.api.handler;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Configurable;
import com.izforge.izpack.api.data.ConfigurationOption;
import com.izforge.izpack.api.rules.RulesEngine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public abstract class DefaultConfigurationHandler implements Configurable, Serializable
{
    private static final long serialVersionUID = 2814753202801899378L;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(DefaultConfigurationHandler.class.getName());

    /**
     * Contains configuration values for a panel.
     */
    private Map<String, ConfigurationOption> configuration = null;

    @Override
    public void addConfigurationOption(String name, ConfigurationOption option)
    {
        if (this.configuration == null)
        {
            this.configuration = new HashMap<String, ConfigurationOption>();
        }
        this.configuration.put(name, option);
    }

    @Override
    public ConfigurationOption getConfigurationOption(String name)
    {
        ConfigurationOption option = null;
        if (this.configuration != null)
        {
            option = this.configuration.get(name);
        }
        return option;
    }

    @Override
    public String getConfigurationOptionValue(String name, RulesEngine rules, String defaultValue)
    {
        String result;
        ConfigurationOption option = getConfigurationOption(name);
        if (option != null)
        {
            result = option.getValue(rules);
        }
        else
        {
            result = defaultValue;
        }
        return result;
    }

    @Override
    public String getConfigurationOptionValue(String name)
    {
        return getConfigurationOptionValue(name, null);
    }

    @Override
    public String getConfigurationOptionValue(String name, RulesEngine rules)
    {
        return getConfigurationOptionValue(name, rules, null);
    }

    @Override
    public Set<String> getNames()
    {
        return configuration != null ? configuration.keySet() : null;
    }

    /**
     * Returns the validation parameters.
     */
    public void readParameters(IXMLElement element)
    {
        IXMLElement configurationElement = element.getFirstChildNamed("configuration");
        if (configurationElement != null)
        {
            logger.fine("Found configuration section for '" + element.getName() + "' element");
            List<IXMLElement> params = configurationElement.getChildren();
            for (IXMLElement param : params)
            {
                String elementName = param.getName();
                String name;
                final String value;
                if (elementName.equals("param"))
                {
                    // Format: <param name="option_1" value="value_1" />
                    name = param.getAttribute("name");
                    value = param.getAttribute("value");
                } else
                {
                    // Format: <option_1>value_1</option_1>
                    name = param.getName();
                    value = param.getContent();
                }
                final ConfigurationOption option = new ConfigurationOption(value);
                logger.fine("-> Adding configuration option " + name + " (" + option + ")");
                addConfigurationOption(name, option);
            }
        }
        // Deprecated: compatibility
        List<IXMLElement> otherParams = element.getChildrenNamed("param");
        if (!otherParams.isEmpty())
        {
            logger.fine("Found deprecated nested <param> definition(s) for '" + element.getName() + "' element, please migrate them to the new <configuration> format");
            for (IXMLElement parameter : otherParams)
            {
                String name = parameter.getAttribute("name");
                String value = parameter.getAttribute("value");
                final ConfigurationOption option = new ConfigurationOption(value);
                logger.fine("-> Adding configuration option " + name + " (" + option + ")");
                addConfigurationOption(name, option);
            }
        }
    }
}
