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

package com.izforge.izpack.panels.userinput.gui.password;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.password.PasswordField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The password group field view.
 *
 * @author Tim Anderson
 */
public class GUIPasswordGroupField extends GUIField
{

    /**
     * The passwords.
     */
    private final List<JPasswordField> passwords = new ArrayList<JPasswordField>();

    /**
     * Constructs a {@code GUIPasswordGroupField}.
     *
     * @param field the field
     */
    public GUIPasswordGroupField(PasswordGroupField field)
    {
        super(field);
        addDescription();

        int id = 1;
        for (PasswordField f : field.getPasswordFields())
        {
            JPasswordField component = new JPasswordField(f.getSet(), f.getSize());
            component.setName(field.getVariable() + "." + id++);
            component.setCaretPosition(0);

            addLabel(f.getLabel());

            passwords.add(component);
            addComponent(component, new TwoColumnConstraints(TwoColumnConstraints.EAST));
        }
        addTooltip();
    }

    /**
     * Updates the field from the view.
     *
     * @param prompt the prompt to display messages
     * @param skipValidation set to true when wanting to save field data without validating
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField(Prompt prompt, boolean skipValidation)
    {
        boolean result = false;
        Field field = getField();
        String[] values = getPasswords();
        ValidationStatus status = field.validate(new PasswordGroup(values));
        if (skipValidation || status.isValid())
        {
            try
            {
                field.setValue(values[0]);
                result = true;
            }
            catch (Throwable exception)
            {
                warning(exception.getMessage(), prompt);
            }
        }
        else
        {
            warning(status.getMessage(), prompt);
        }
        return result;
    }

    /**
     * Updates the view from the field.
     *
     * @return {@code true} if the view was updated
     */
    @Override
    public boolean updateView()
    {
        boolean result = super.updateView();
        String value = getField().getInitialValue();

        if (value != null)
        {
            passwords.get(0).setText(replaceVariables(value));
            result = true;
        }
        else
        {
            // Set default value here for getting current variable values replaced
            Field field = getField();
            String defaultValue = field.getDefaultValue();
            if (defaultValue != null)
            {
                passwords.get(0).setText(defaultValue);
            }
        }

        return result;
    }

    /**
     * Returns the passwords.
     *
     * @return the passwords
     */
    private String[] getPasswords()
    {
        String[] result = new String[passwords.size()];
        for (int i = 0; i < result.length; ++i)
        {
            result[i] = new String(passwords.get(i).getPassword());
        }
        return result;
    }

    @Override
    public JComponent getFirstFocusableComponent()
    {
        return passwords.get(0);
    }

}
