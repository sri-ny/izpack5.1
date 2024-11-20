/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

package com.izforge.izpack.panels.userinput.console.staticText;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.util.Console;

/**
 * Console static text field.
 *
 * @author Tim Anderson
 */
public class ConsoleStaticText extends ConsoleField
{

    /**
     * Constructs a {@code ConsoleStaticText}.
     *
     * @param field   the field
     * @param console the console
     */
    public ConsoleStaticText(Field field, Console console, Prompt prompt)
    {
        super(field, console, prompt);
    }

    /**
     * Displays the field.
     *
     * @return {@code true}
     */
    @Override
    public boolean display()
    {
        String text = getField().getLabel(true);
        if (text != null)
        {
            println(text);
        }
        return true;
    }
}
