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

package com.izforge.izpack.panels.userinput.processorclient;

import com.izforge.izpack.api.handler.DefaultConfigurationHandler;

import java.text.MessageFormat;


/**
 * Simple implementation of {@link ProcessingClient} that wraps an array of string values.
 *
 * @author Tim Anderson
 */
public class ValuesProcessingClient extends DefaultConfigurationHandler implements ProcessingClient
{
    /**
     * The values.
     */
    private final String[] values;

    private final MessageFormat format;

    /**
     * Constructs a {@code ValuesProcessingClient}.
     *
     * @param values the values to process
     */
    public ValuesProcessingClient(String[] values)
    {
        this(null, values);
    }

    /**
     * Constructs a {@code ValuesProcessingClient}.
     *
     * @param format     the formatter
     * @param values     the values to process
     */
    public ValuesProcessingClient(MessageFormat format, String[] values)
    {
        this.format = format;
        this.values = values;
    }

    /**
     * Returns the values.
     *
     * @return the values
     */
    @Override
    public String[] getValues()
    {
        return values;
    }

    /**
     * Returns the number of sub-fields.
     *
     * @return the number of sub-fields
     */
    @Override
    public int getNumFields()
    {
        return values.length;
    }

    /**
     * Returns the contents of the field indicated by <code>index</code>.
     *
     * @param index the index of the sub-field from which the contents is requested.
     * @return the contents of the indicated sub-field.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    @Override
    public String getFieldContents(int index)
    {
        return values[index];
    }

    /**
     * Returns the field contents as one formatted or concatenated text.
     *
     * @return the field contents
     */
    @Override
    public String getText()
    {
        if (format != null)
        {
            return format.format(values);
        }
        else
        {
           StringBuilder result = new StringBuilder();
            for (String value : values)
            {
                result.append(value);
            }
            return result.toString();
        }
    }
}
