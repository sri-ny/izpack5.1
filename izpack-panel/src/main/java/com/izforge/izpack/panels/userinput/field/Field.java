/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
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

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.process.ExistsCondition;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Describes a user input field.
 *
 * @author Tim Anderson
 */
public abstract class Field
{

    /**
     * The variable. May be {@code null}.
     */
    private String variable;

    /**
     * The variable. May be {@code null}.
     */
    private final String summaryKey;

    /**
     * Specifies the value to override the current variable value for the field.
     */
    private final String initialValue;

    /**
     * Specifies the default value for the field.
     */
    private final String defaultValue;

    /**
     * The field size.
     */
    private final int size;

    /**
     * The packs that the field applies to. May be {@code null} or empty to indicate all packs.
     */
    private final List<String> packs;

    /**
     * The the operating systems that the field applies to. An empty list indicates it applies to all operating systems
     */
    private final List<OsModel> models;

    /**
     * The field validators.
     */
    private final List<FieldValidator> validators;

    /**
     * The field processors. May be {@code null}
     */
    private final List<FieldProcessor> processors;

    /**
     * The field label. May be {@code null}
     */
    private final String label;

    /**
     * The field description. May be {@code null}
     */
    private final String description;

    /**
     * The field's tooltip. May be {@code null}
     */
    private final String tooltip;

    /**
     * Condition that determines if the field is displayed or not.
     */
    private final String condition;

    /**
     * Determines if the field should always be displayed on the panel regardless if its conditionid is true or false.
     * If the conditionid is false, display the field but disable it.
     */
    private final Boolean displayHidden;

    /**
     * Determines a condition for which the field should be displayed on the panel regardless if its conditionid is true or false.
     */
    private final String displayHiddenCondition;

    /**
     * Determines if the field should always be displayed read-only.
     */
    private final Boolean readonly;

    /**
     * Determines a condition for which the field should be displayed read-only.
     */
    private final String readonlyCondition;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * Determines if the 'value' of an entry will be included in the user input panel
     */
    private final boolean omitFromAuto;

    private String unprocessedValue;

    private boolean saving = false;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Field.class.getName());

    /**
     * Constructs a {@code Field}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @throws IzPackException if the configuration is invalid
     */
    public Field(FieldConfig config, InstallData installData)
    {
        variable = config.getVariable();
        summaryKey = config.getSummaryKey();
        initialValue = config.getInitialValue();
        defaultValue = config.getDefaultValue();
        size = config.getSize();
        packs = config.getPacks();
        models = config.getOsModels();
        validators = config.getValidators();
        processors = config.getProcessors();
        label = config.getLabel();
        description = config.getDescription();
        displayHidden = config.isDisplayHidden();
        displayHiddenCondition = config.getDisplayHiddenCondition();
        readonly = config.isReadonly();
        readonlyCondition = config.getReadonlyCondition();
        tooltip = config.getTooltip();
        omitFromAuto = config.getOmitFromAuto();
        this.condition = config.getCondition();
        this.installData = installData;


        if (variable != null)
        {
            addExistsCondition();
        }
    }

    /**
     * Returns the variable.
     *
     * @return the variable. May be {@code null}
     */
    public String getVariable()
    {
        return variable;
    }

    /**
     * Returns the value of 'omitFromAuto' from fields spec
     *
     * @return the 'omitFromAuto' attribute
     */
    public boolean getOmitFromAuto()
    {
        return omitFromAuto;
    }

    /**
     * Returns the summaryKey.
     *
     * @return the summaryKey. May be {@code null}
     */
    public String getSummaryKey()
    {
        return summaryKey;
    }

    /**
     * Returns an effective value whether a field should be currently displayed read-only.
     *
     * @return true if field should be shown read-only, or {@code false}
     */
    public boolean isEffectiveReadonly(boolean defaultFlag, RulesEngine rules)
    {
        boolean result;

        if (readonly != null)
        {
            result = readonly;
        }
        else if (readonlyCondition != null && rules.isConditionTrue(readonlyCondition))
        {
            result = rules.isConditionTrue(readonlyCondition);
        }
        else
        {
            result = defaultFlag;
        }
        return result;
    }

    /**
     * Returns an effective value whether a field should be currently displayed read-only if hidden.
     *
     * @return true if field should be shown read-only if hidden, or {@code false}
     */
    public boolean isEffectiveDisplayHidden(boolean defaultFlag, RulesEngine rules)
    {
        boolean result;

        if (displayHidden != null)
        {
            result = displayHidden;
        }
        else if (displayHiddenCondition != null && rules.isConditionTrue(displayHiddenCondition))
        {
            result = rules.isConditionTrue(displayHiddenCondition);
        }
        else
        {
            result = defaultFlag;
        }
        return result;
    }

    /**
     * Returns the packs that the field applies to.
     *
     * @return the pack names
     */
    public List<String> getPacks()
    {
        return packs;
    }

    /**
     * Returns the operating systems that the field applies to.
     *
     * @return the OS family names
     */
    public List<OsModel> getOsModels()
    {
        return models;
    }

    /**
     * Returns the default value of the field with resolved variables.
     *
     * @return the default value. May be {@code null}
     */
    public String getDefaultValue()
    {
        return getDefaultValue(true);
    }

    /**
     * Returns the default value of the field.
     *
     * @param translated true if variable references in the text should be resolved
     * @return the default value. May be {@code null}
     */
    private String getDefaultValue(boolean translated)
    {
        String value = wrapDefaultValue(defaultValue);
        if (translated && value != null)
        {
            return replaceVariables(value);
        }
        return value;
    }

    /**
     * Returns the forced value of the field.
     *
     * @param translated true if variable references in the text should be resolved
     * @return the forced value. May be {@code null}
     */
    private String getForcedValue(boolean translated)
    {
        String value = wrapInitialValue(initialValue);
        if (translated && value != null)
        {
            return replaceVariables(value);
        }
        return value;
    }

    /**
     * Returns the initial value to use for this field with resolved variables.
     * <p/>
     * The following non-null value is used from the following search order
     * <ul>
     * <li>initial value (substituting variables)
     * <li>current variable value
     * <li>default value (substituting variables)
     * </ul>
     *
     * @return The initial value to use for this field
     */
    public String getInitialValue()
    {
        return getInitialValue(true);
    }

    /**
     * Returns the initial value to use for this field.
     * <p/>
     * The following non-null value is used from the following search order
     * <ul>
     * <li>initial value (substituting variables)
     * <li>current variable value
     * <li>default value (substituting variables)
     * </ul>
     *
     * @param resolve true if variable references in the text should be resolved
     * @return The initial value to use for this field
     */
    private String getInitialValue(boolean resolve)
    {
        String result = null;
        if (!installData.getVariables().isBlockedVariableName(variable))
        {
            result = getForcedValue(resolve);
        }
        if (result == null)
        {
            result = getValue();
            if (result != null)
            {
                if (resolve)
                {
                    result = replaceVariables(result);
                }
            }
            else
            {
                result = getDefaultValue(resolve);
            }
        }
        return result;
    }

    /**
     * Returns the variable value.
     *
     * @return the variable value. May be {@code null}
     */
    public String getValue()
    {
        final String savedValue = installData.getVariable(variable);
        if (savedValue == null && unprocessedValue != null)
        {
            unprocessedValue = null;
        }

        return (unprocessedValue==null ? savedValue : unprocessedValue);
    }

    public void setSaving(boolean flag)
    {
        this.saving = flag;
    }

    /**
     * Sets the variable value.
     *
     * @param value the variable value. May be {@code null}
     */
    public void setValue(String value)
    {
        unprocessedValue = value;
        if (saving)
        {
            value = process(value);
        }

        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Field setting variable=" + variable + " to value=" + value);
        }
        installData.setVariable(variable, value);
        saving = false;
    }


    /**
     * Wrap the initial value of a field, which is the value of the <code>set</code> attribute in the field's spec to
     * the effective value to be assigned to the variable. This can be used for enumeration type conversions.
     * <br><br>
     * This method can be optionally overridden by several user input field types.
     * <br><br>
     * Example: The <code>set</code> attribute in the checkbox user input field has a boolean value, the value should
     * be wrapped to the value of the <code>true</code> attribute in case of <code>set="true"</code> or to the value of
     * the <code>false</code> attribute in case of <code>set="false"</code>.
     *
     * @param originalValue the original value of the <code>set</code> attribute
     * @return the wrapped value
     * @see com.izforge.izpack.panels.userinput.field.check.CheckField
     */
    public String wrapInitialValue(String originalValue)
    {
        return originalValue;
    }

    /**
     * Wrap the default value of a field, which is the value of the <code>default</code> attribute in the field's spec
     * to the effective value to be assigned to the variable. This can be used for enumeration type conversions.
     * To be overridden by several user input field types.
     * <br><br>
     * This method can be optionally overridden by several user input field types.
     * <br><br>
     * Example: The <code>set</code> attribute in the checkbox user input field has a boolean value, the value should
     * be wrapped to the value of the <code>true</code> attribute in case of <code>default="true"</code> or to the
     * value of the <code>false</code> attribute in case of <code>default="false"</code>.
     * @see com.izforge.izpack.panels.userinput.field.check.CheckField
     *
     * @param originalValue the original value of the <code>default</code> attribute
     * @return the wrapped value
     * @see com.izforge.izpack.panels.userinput.field.check.CheckField
     */
    public String wrapDefaultValue(String originalValue)
    {
        return originalValue;
    }

    /**
     * Returns the field size.
     *
     * @return the field size, or {@code -1} if no size is defined
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Validates values using any validators associated with the field.
     *
     * @param values the values to validate
     * @return the status of the validation
     */
    public ValidationStatus validate(String... values)
    {
        return validate(new ValuesProcessingClient(values));
    }

    /**
     * Validates values using any validators associated with the field.
     *
     * @param values the values to validate
     * @return the status of the validation
     */
    public ValidationStatus validate(ValuesProcessingClient values)
    {
        try
        {
            for (FieldValidator validator : validators)
            {
                validator.setInstallData(installData);
                if (!validator.validate(values))
                {
                    return ValidationStatus.failed(validator.getMessage());
                }
            }
        }
        catch (Throwable exception)
        {
            return ValidationStatus.failed(exception.getMessage());
        }
        return ValidationStatus.success(values.getValues());
    }

    /**
     * Processes a initialValue of values.
     *
     * @param values the values to process
     * @return the result of processing
     * @throws IzPackException if processing fails
     */
    private String process(String... values)
    {
        String result = null;
        if (processors != null && !processors.isEmpty())
        {
            for (FieldProcessor processor : processors)
            {
                processor.setInstallData(installData);
                String processorResult;
                if (result == null) {
                    processorResult = processor.process(values);
                } else {
                    processorResult = processor.process(result);
                }

                String backupVariable = processor.getBackupVariable();
                if (backupVariable != null)
                {
                    installData.setVariable(backupVariable, processor.getOriginalValue());
                }
                String toVariable = processor.getToVariable();
                if (toVariable != null)
                {
                    installData.setVariable(toVariable, processorResult);
                    processorResult = processor.getOriginalValue();
                }
                result = processorResult;
            }
        }
        else if (values.length > 0)
        {
            result = values[0];
        }
        return result;
    }

    /**
     * Returns the field processors.
     *
     * @return the field processors. May be {@code null}
     */
    public List<FieldProcessor> getProcessors()
    {
        return processors;
    }

    /**
     * Returns the field label with resolved variable values.
     *
     * @return the field label. May be {@code null}
     */
    public String getLabel()
    {
        return getLabel(false);
    }

    /**
     * Returns the field label.
     *
     * @param resolve whether the label should be returned with resolved variables
     * @return the field label. May be {@code null}
     */
    public String getLabel(boolean resolve)
    {
        return (resolve && label != null)?replaceVariables(label):label;
    }

    /**
     * Returns the field description with resolved variable values.
     *
     * @return the field description. May be {@code null}
     */
    public String getDescription()
    {
        return getDescription(false);
    }

    /**
     * Returns the field description.
     *
     * @param resolve whether the description should be returned with resolved variables
     * @return the field label. May be {@code null}
     */
    public String getDescription(boolean resolve)
    {
        return (resolve && description != null)?replaceVariables(description):description;
    }

    /**
     * Returns the field tooltip with resolved variable values.
     *
     * @return the field tooltip. May be {@code null}
     */
    public String getTooltip()
    {
        return getTooltip(false);
    }

    /**
     * Returns the field tooltip.
     *
     * @param resolve whether the tooltip should be returned with resolved variables
     * @return the field tooltip. May be {@code null}
     */
    private String getTooltip(boolean resolve)
    {
        return (resolve && tooltip != null)?replaceVariables(tooltip):tooltip;
    }

    /**
     * Determines if the condition associated with the field is true.
     *
     * @return {@code true} if the condition evaluates {true} or if the field has no condition
     */
    public boolean isConditionTrue()
    {
        RulesEngine rules = getRules();
        return (condition == null || rules.isConditionTrue(condition, installData));
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    public InstallData getInstallData()
    {
        return installData;
    }

    /**
     * Returns the rules.
     *
     * @return the rules
     */
    private RulesEngine getRules()
    {
        return installData.getRules();
    }

    /**
     * Replaces any variables in the supplied value.
     *
     * @param value the value
     * @return the value with variables replaced
     */
    protected String replaceVariables(String value)
    {
        return installData.getVariables().replace(value);
    }

    /**
     * Adds an 'exists' condition for the variable.
     */
    private void addExistsCondition()
    {
        RulesEngine rules = getRules();
        final String conditionId = "izpack.input." + variable;
        if (rules != null)
        {
            if (rules.getCondition(conditionId) == null)
            {
                ExistsCondition existsCondition = new ExistsCondition();
                existsCondition.setContentType(ExistsCondition.ContentType.VARIABLE);
                existsCondition.setContent(variable);
                existsCondition.setId(conditionId);
                existsCondition.setInstallData(installData);
                rules.addCondition(existsCondition);
            }
            else
            {
                logger.fine("Condition '" + conditionId + "' for variable '" + variable + "' already exists");
            }
        }
        else
        {
            logger.fine("Cannot add  condition '" + conditionId + "' for variable '" + variable + "'. Rules not supplied");
        }
    }

    //TODO: Scary thought to have variable not final
    //TODO: Need to check that variable doesn't already exist
    public void setVariable(String newVariableName)
    {
        this.variable = newVariableName;
    }
}
