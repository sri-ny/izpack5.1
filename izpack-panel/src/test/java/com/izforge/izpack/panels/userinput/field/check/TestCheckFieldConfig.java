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

package com.izforge.izpack.panels.userinput.field.check;

import com.izforge.izpack.panels.userinput.field.TestFieldConfig;


/**
 * Implementation of {@link CheckFieldConfig} for testing purposes.
 *
 * @author Tim Anderson
 */
public class TestCheckFieldConfig extends TestFieldConfig implements CheckFieldConfig
{

    /**
     * The value to assign to the variable when the checkbox is selected.
     */
    private String trueValue;

    /**
     * The value to assign to the variable when the checkbox is not selected.
     */
    private String falseValue;


    /**
     * Constructs a {@code TestCheckFieldConfig}.
     *
     * @param variable   the variable
     * @param trueValue  the value to assign to the variable when the checkbox is selected
     * @param falseValue the value to assign to the variable when the checkbox is not selected
     */
    public TestCheckFieldConfig(String variable, String trueValue, String falseValue)
    {
        super(variable);
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    /**
     * Returns the value to assign to the associated variable when the checkbox is selected (i.e. is 'true').
     *
     * @return the 'true' value. May be {@code null}
     */
    @Override
    public String getTrueValue()
    {
        return trueValue != null ? trueValue : Boolean.TRUE.toString();
    }

    /**
     * Returns the value to assign to the associated variable when the checkbox is not selected (i.e. is 'false').
     *
     * @return the 'false' value
     */
    @Override
    public String getFalseValue()
    {
        return falseValue != null ? falseValue : Boolean.FALSE.toString();
    }
}
