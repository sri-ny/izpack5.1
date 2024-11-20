/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels.userinput.gui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

/**
 * Field component.
 *
 * @author Dennis Reil
 */
public class Component
{

    private final JComponent component;

    private final Object constraints;

    private boolean enabled = true;

    public Component(JComponent component, Object constraints)
    {
        this.component = component;
        this.constraints = constraints;
    }

    public JComponent getComponent()
    {
        return component;
    }

    public Object getConstraints()
    {
        return constraints;
    }

    public boolean setEnabled(boolean enabled)
    {
        if (component instanceof JLabel || component instanceof JPanel || component instanceof JTextPane)
        {
           return false;
        }
        if (component instanceof JTextComponent)
        {
            JTextComponent textComponent = ((JTextComponent)component);
            if (!textComponent.isFocusable() || !textComponent.isEditable())
            {
                return false;
            }
        }
        component.setEnabled(enabled);
        return true;
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}