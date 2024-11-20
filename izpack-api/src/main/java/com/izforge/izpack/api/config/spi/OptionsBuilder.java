/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2014 René Krell
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

package com.izforge.izpack.api.config.spi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.izforge.izpack.api.config.Config;
import com.izforge.izpack.api.config.Options;

public class OptionsBuilder implements OptionsHandler
{
    private List<String> lastComments = new ArrayList<String>();
    private Options _options;

    public static OptionsBuilder newInstance(Options opts)
    {
        OptionsBuilder instance = newInstance();

        instance.setOptions(opts);

        return instance;
    }

    public void setOptions(Options value)
    {
        _options = value;
    }

    @Override public void endOptions()
    {
        setFooterComment();
    }

    @Override public void handleComment(List<String> comment)
    {
        lastComments.addAll(comment);
    }

    @Override public void handleEmptyLine()  {}

    @Override public void handleOption(String name, String value)
    {
        String newName = name;
        if (getConfig().isAutoNumbering() && name.matches("(.+\\.)+[\\d]+(\\.+.*)*"))
        {
            String[] parts = name.split("\\.");
            StringBuilder sb = new StringBuilder();
            int pos = -1;
            for (String part : parts)
            {
                if (sb.length() > 0)
                {
                    sb.append(".");
                }
                if (pos != -1)
                {
                    sb.append(part);
                }
                else
                {
                    try
                    {
                        pos = Integer.parseInt(part);
                        // The first \.\d+\. "wins"
                    }
                    catch (NumberFormatException nfe)
                    {
                        sb.append(part);
                    }
                }
            }
            newName = sb.toString();

            // check whether key has been added before
            if (!_options.containsKey(newName))
            {
                _options.add(newName, null);
            }

            // resize list for key if it is too small
            for (int i = _options.getAll(newName).size(); i <= pos; i++)
            {
                _options.add(newName, null);
            }

            _options.put(newName, value, pos);
        }
        else
        {
            if (getConfig().isMultiOption())
            {
                _options.add(newName, value);
            }
            else
            {
                _options.put(newName, value);
            }
        }

        putComment(newName);
    }

    @Override public void startOptions()
    {
        lastComments.clear();
    }

    protected static OptionsBuilder newInstance()
    {
        return ServiceFinder.findService(OptionsBuilder.class);
    }

    private Config getConfig()
    {
        return _options.getConfig();
    }

    private void setFooterComment()
    {
        if (getConfig().isComment() &&  !lastComments.isEmpty())
        {
            _options.setFooterComment((List<String>)lastComments);
        }
    }

    private void putComment(String key)
    {
        if (getConfig().isComment() &&  !lastComments.isEmpty())
        {
            // TODO Handle comments between multi-options
            // (currently, the last one appeared replaces the others)
            _options.putComment(key, (List<String>)lastComments);
            lastComments = new LinkedList<String>();
        }
    }
}
