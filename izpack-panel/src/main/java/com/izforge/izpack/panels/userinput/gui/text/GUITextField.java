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

package com.izforge.izpack.panels.userinput.gui.text;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


/**
 * Text field view.
 *
 * @author Tim Anderson
 */
public class GUITextField extends GUIField implements FocusListener, DocumentListener
{

    /**
     * The component.
     */
    private final JTextField text;

    private transient boolean changed = false;


    /**
     * Constructs a {@code GUITextField}.
     *
     * @param field the field
     */
    public GUITextField(TextField field)
    {
        super(field);

        text = new JTextField(field.getInitialValue(), field.getSize());
        text.setName(field.getVariable());
        text.setCaretPosition(0);
        text.getDocument().addDocumentListener(this);
        text.addFocusListener(this);
        addField(text);
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
        String text = this.text.getText();
        Field field = getField();
        ValidationStatus status = field.validate(text);
        if (skipValidation || status.isValid())
        {
            field.setValue(text);
            result = true;
        }
        else
        {
            String message = status.getMessage();
            if (message == null)
            {
                message = "Text entered did not pass validation.";
            }
            warning(message, prompt);
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
        Field f = getField();
        String value = f.getInitialValue();

        if (value != null)
        {
            replaceValue(value);
            result = true;
        }
        else
        {
            // Set default value here for getting current variable values replaced
            String defaultValue = f.getDefaultValue();
            if (defaultValue != null)
            {
                replaceValue(defaultValue);
            }
        }

        return result;
    }

    private void replaceValue(String value)
    {
        boolean changed = false;
        String oldValue = text.getText();
        if (!(oldValue == null ? value == null : oldValue.equals(value)))
        {
            text.getDocument().removeDocumentListener(this);
            text.setText(replaceVariables(value));
            text.getDocument().addDocumentListener(this);
            changed = true;
        }
        setChanged(changed);
    }

    public synchronized void setChanged(boolean changed)
    {
        this.changed = changed;
    }

    private synchronized boolean isChanged()
    {
        return changed;
    }


    // FocusListener interface

    @Override
    public void focusGained(FocusEvent event)
    {
        text.selectAll();
    }
    @Override
    public void focusLost(FocusEvent event)
    {
        if (isChanged())
        {
            notifyUpdateListener();
            setChanged(false);
        }
        text.select(0, 0);
    }


    // DocumentListener interface

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        setChanged(true);
    }
    @Override
    public void removeUpdate(DocumentEvent e)
    {
        setChanged(true);
    }
    @Override
    public void changedUpdate(DocumentEvent e)
    {
        setChanged(true);
    }

    @Override
    public JComponent getFirstFocusableComponent()
    {
        return text;
    }
}
