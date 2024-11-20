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

package com.izforge.izpack.panels.userinput.field;

import com.izforge.izpack.api.data.Configurable;
import com.izforge.izpack.api.data.ConfigurationOption;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;
import com.izforge.izpack.panels.userinput.validator.Validator;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * FieldValidator is a wrapper around a {@link Validator}.
 *
 * @author Tim Anderson
 */
public class FieldValidator
{

    /**
     * The validator class name.
     */
    private final String className;

    /**
     * The validation message. May be {@code null}
     */
    private final String message;

    /**
     * The factory to create the validator.
     */
    private final ObjectFactory factory;

    /**
     * The validator.
     */
    private Validator validator;

    private Configurable configurable;

    private InstallData installData;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(FieldValidator.class.getName());

    /**
     * Constructs a {@code FieldValidator}.
     *
     * @param type    the validator class
     * @param message the validation error message. May be {@code null}
     * @param factory the factory for creating the validator
     */
    public FieldValidator(Class<? extends Validator> type, String message, ObjectFactory factory)
    {
        this.className = type.getName();
        this.message = message;
        this.factory = factory;
    }

    /**
     * Constructs a {@code FieldValidator}.
     *
     * @param className  the validator class name
     * @param configurable the validation parameters. May be {@code null}
     * @param message    the validation error message. May be {@code null}
     * @param factory    the factory for creating the validator
     */
    public FieldValidator(String className, Configurable configurable, String message, ObjectFactory factory)
    {
        this.className = className;
        this.configurable = configurable;
        this.message = message;
        this.factory = factory;
    }

    /**
     * Constructs a {@code FieldValidator}.
     *
     * @param validatorReader    the validator XML reader
     * @param factory    the factory for creating the validator
     */
    public FieldValidator(FieldValidatorReader validatorReader, ObjectFactory factory)
    {
        this.configurable = validatorReader;
        this.className = validatorReader.getClassName();
        this.message = validatorReader.getMessage();
        this.factory = factory;
    }

    /**
     * Returns the validation error message.
     *
     * @return the validation error message. May be {@code null}
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Validates field values.
     *
     * @param values the values to validate
     * @return {@code true} if the values are valid, otherwise {@code false}
     */
    public boolean validate(String[] values)
    {
        return validate(new ValuesProcessingClient(values));
    }

    public void setInstallData(InstallData installData)
    {
        this.installData = installData;
    }

    /**
     * Validates field values.
     *
     * @param values the values to validate
     * @return {@code true} if the values are valid, otherwise {@code false}
     */
    public boolean validate(ValuesProcessingClient values)
    {
        boolean result = false;
        try
        {
            if (validator == null)
            {
                validator = factory.create(className, Validator.class);
            }

            // Copy optional validator configuration parameters
            if (configurable != null)
            {
                Set<String> names = configurable.getNames();
                if (names != null)
                {
                    for (String key : names)
                    {
                        ConfigurationOption option = configurable.getConfigurationOption(key);
                        if (installData != null)
                        {
                            // Resolve variables in validator configuration parameters
                            String value = option.getValue(installData.getRules());
                            String newValue = installData.getVariables().replace(value);
                            if (value != null && !value.equals(newValue))
                            {
                                option = new ConfigurationOption(newValue);
                            }
                        }
                        values.addConfigurationOption(key, option);
                    }
                }
            }

            result = validator.validate(values);
            if (logger.isLoggable(Level.FINE))
            {
                logger.log(Level.FINE, "Validation " + (result ? "OK" : "FAILED") + " using "
                        + validator.getClass().getSimpleName());
            }
        }
        catch (Throwable exception)
        {
            logger.log(Level.WARNING, "Validation using " + className + " failed: " + exception.getMessage(),
                       exception);
        }
        return result;
    }
}
