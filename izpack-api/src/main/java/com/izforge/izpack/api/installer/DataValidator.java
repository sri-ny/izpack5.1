/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2008 Patrick Zbinden.
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

package com.izforge.izpack.api.installer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.data.InstallData;

public interface DataValidator
{

    public enum Status
    {
        OK("success"), WARNING("warning"), ERROR("error");

        private static Map<String, Status> lookup;

        private String attribute;

        Status(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, Status>();
            for (Status severity : EnumSet.allOf(Status.class))
            {
                lookup.put(severity.getAttribute(), severity);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static Status getStatusFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }

    }

    /**
     * tag-name of the datavalidator
     */
    public static final String DATA_VALIDATOR_TAG = "validator";

    /**
     * attribute for class to use
     */
    public static final String DATA_VALIDATOR_CLASSNAME_ATTR = "classname";

    /**
     * attribute for validator condition to apply
     */
    public static final String DATA_VALIDATOR_CONDITION_ATTR = "condition";

    /**
     * Method to validate complex variable settings read from {@link InstallData} after a panel change.
     *
     * @param installData@return {@link Status} the result of the validation
     */
    public Status validateData(final InstallData installData);

    /**
     * Returns the string with either a message if from translations or the message itself in case {@code validateData}
     * returns {@code Status.ERROR}.
     *
     * @return String Should be the message id or the untranslated error message.
     */
    public String getErrorMessageId();

    /**
     * Returns the string with either a message if from translations or the message itself in case {@code validateData}
     * returns {@code Status.WARNING}.
     *
     * @return String Should be the message id or the untranslated warning message.
     */
    public String getWarningMessageId();

    /**
     * If the installer is run in automated mode, and {@code validateData}
     * returns {@code Status.WARNING}, this method is asked how to go on instead of an user answer.
     *
     * @return boolean true - ignore warning and continue to the next panel, false - don't change to the next panel, fail
     */

    public boolean getDefaultAnswer();
}
