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

package com.izforge.izpack.panels.userinput.field;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.DefaultConfigurationHandler;


/**
 * Reads an {@link FieldValidator} from an {@link Config}.
 *
 * @author Tim Anderson
 */
class FieldValidatorReader extends DefaultConfigurationHandler
{

    /**
     * The validator element.
     */
    private final IXMLElement validator;

    /**
     * The configuration.
     */
    private final Config config;


    /**
     * Constructs a {@code} FieldValidatorReader}.
     *
     * @param validator the validator element
     * @param config    the configuration
     */
    public FieldValidatorReader(IXMLElement validator, Config config)
    {
        readParameters(validator);
        this.validator = validator;
        this.config = config;
    }

    /**
     * Returns the validator class name.
     *
     * @return the validator class name
     */
    public String getClassName()
    {
        return config.getAttribute(validator, "class");
    }

    /**
     * Returns the validation error message.
     *
     * @return the validation error message. May be {@code null}
     */
    public String getMessage()
    {
        return config.getText(validator);
    }
}
