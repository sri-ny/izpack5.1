/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.panels.userinput;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.TwoColumnLayout;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.ElementReader;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldHelper;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.gui.Component;
import com.izforge.izpack.panels.userinput.gui.GUIField;
import com.izforge.izpack.panels.userinput.gui.GUIFieldFactory;
import com.izforge.izpack.panels.userinput.gui.UpdateListener;
import com.izforge.izpack.panels.userinput.gui.custom.GUICustomField;
import com.izforge.izpack.util.PlatformModelMatcher;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User input panel.
 *
 * @author Anthonin Bonnefoy
 */
public class UserInputPanel extends IzPanel
{
    private static final long serialVersionUID = 8667949467748997226L;

    private static final String SUMMARY_KEY = "summaryKey";
    private static final String TOPBUFFER = "topBuffer";
    private static final String RIGID = "rigid";
    private static final String DISPLAY_HIDDEN = "displayHidden";
    private static final String DISPLAY_HIDDEN_CONDITION = "displayHiddenCondition";
    private static final String READONLY = "readonly";
    private static final String READONLY_CONDITION = "readonlyCondition";

    /**
     * The parsed result from reading the XML specification from the file
     */
    private final IXMLElement spec;

    private boolean eventsActivated = false;

    private boolean saving = false;

    private final List<GUIField> views = new ArrayList<GUIField>();

    private JPanel panel;

    private JComponent firstFocusedComponent;

    private final RulesEngine rules;

    /**
     * The factory for creating validators.
     */
    private final ObjectFactory factory;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The delegating prompt. This is used to switch between the above prompt and a no-op prompt when performing
     * updates.
     */
    private final DelegatingPrompt delegatingPrompt;

    private UserInputPanelSpec userInputModel;

    /*--------------------------------------------------------------------------*/
    // This method can be used to search for layout problems. If this class is
    // compiled with this method uncommented, the layout guides will be shown
    // on the panel, making it possible to see if all components are placed
    // correctly.
    /*--------------------------------------------------------------------------*/
    // public void paint (Graphics graphics)
    // {
    // super.paint (graphics);
    // layout.showRules ((Graphics2D)graphics, Color.red);
    // }
    /*--------------------------------------------------------------------------*/

    /**
     * Constructs an {@code UserInputPanel}.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent IzPack installer frame
     * @param installData the installation data
     * @param resources   the resources
     * @param rules       the rules engine
     * @param factory     factory
     * @param matcher     the platform-model matcher
     * @param prompt      the prompt
     */
    public UserInputPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                          RulesEngine rules, ObjectFactory factory, final PlatformModelMatcher matcher, Prompt prompt)
    {
        super(panel, parent, installData, resources);

        this.rules = rules;
        this.factory = factory;
        this.matcher = matcher;
        this.prompt = prompt;
        this.delegatingPrompt = new DelegatingPrompt(prompt);

        this.spec = readSpec();
        boolean isDisplayingHidden;
        try
        {
            isDisplayingHidden = Boolean.parseBoolean(spec.getAttribute(DISPLAY_HIDDEN));
        }
        catch (Exception ignore)
        {
            isDisplayingHidden = false;
        }
        panel.setDisplayHidden(isDisplayingHidden);

        String condition = spec.getAttribute(DISPLAY_HIDDEN_CONDITION);
        if (condition != null && !condition.isEmpty())
        {
            panel.setDisplayHiddenCondition(condition);
        }

        // Prevent activating on certain global conditions
        ElementReader reader = new ElementReader(userInputModel.getConfig());
        Condition globalConstraint = reader.getComplexPanelCondition(spec, matcher, installData, rules);
        if (globalConstraint != null)
        {
            rules.addPanelCondition(panel, globalConstraint);
        }

        boolean readonly;
        try
        {
            readonly = Boolean.parseBoolean(spec.getAttribute(READONLY));
        }
        catch (Exception ignore)
        {
            readonly = false;
        }
        panel.setReadonly(readonly);

        condition = spec.getAttribute(READONLY_CONDITION);
        if (condition != null && !condition.isEmpty())
        {
            panel.setReadonlyCondition(condition);
        }

        init();
        addScrollPane();
        Dimension size = getMaximumSize();
        setSize(size.width, size.height);
        buildUI();
        updateUIElements();
        validate();
    }

    /**
     * Indicates whether the panel has been validated or not. The installer won't let the user go
     * further through the installation process until the panel is validated.
     *
     * @return a boolean stating whether the panel has been validated or not.
     */
    @Override
    public boolean isValidated()
    {
        return readInput(prompt);
    }

    /**
     * Save visible contents of the this panel into install data.
     */
    @Override
    public void saveData()
    {
        saving = true;
        readInput(prompt, true);
    }

    /**
     * This method is called when the panel becomes active.
     */
    @Override
    public void panelActivate()
    {
        if (spec == null)
        {
            // TODO: translate
            emitError("User input specification could not be found.",
                      "The specification for the user input panel could not be found. Please contact the packager.");
            parent.skipPanel();
        }
        else
        {
            eventsActivated = false;
            buildUI();
            updateUIElements();
            eventsActivated = true;
        }

        updateDialog();

        if (firstFocusedComponent != null)
        {
            setInitialFocus(firstFocusedComponent);
            firstFocusedComponent.requestFocusInWindow();
        }
    }

    /**
     * Creates an installation record for unattended installations on {@link UserInputPanel},
     * created during GUI installations.
     */
    @Override
    public void createInstallationRecord(IXMLElement rootElement)
    {
        new UserInputPanelAutomationHelper(views).createInstallationRecord(installData, rootElement);
    }

    /**
     * Initialize the panel.
     */
    private void init()
    {
        eventsActivated = false;
        super.removeAll();
        views.clear();

        setLayout(new BorderLayout());

        panel = new JPanel();

        if (spec == null)
        {
            // return if we could not read the spec. further
            // processing will only lead to problems. In this
            // case we must skip the panel when it gets activated.
            return;
        }

        // clear button mnemonics map
        ButtonFactory.clearPanelButtonMnemonics();

        // ----------------------------------------------------
        // process all field nodes. Each field node is analyzed
        // for its type, then an appropriate member function
        // is called that will create the correct UI elements.
        // ----------------------------------------------------
        GUIFieldFactory viewFactory = new GUIFieldFactory(installData, this, parent, delegatingPrompt);
        UpdateListener listener = new UpdateListener()
        {
            @Override
            public void updated()
            {
                updateDialog();
            }
        };

        List<Field> fields = userInputModel.createFields(spec);
        for (Field field : fields)
        {
            GUIField view = viewFactory.create(field, userInputModel, spec);
            view.setUpdateListener(listener);
            views.add(view);
        }
        eventsActivated = true;
    }

    /**
     * Set elements to be visible or not depending on field conditions - dynamic update of field visibility.
     */
    private void updateUIElements()
    {
        boolean updated = false;
        firstFocusedComponent = null;

        for (GUIField view : views)
        {
            Field field = view.getField();
            if (field.isConditionTrue())
            {
                view.setDisplayed(true);

                if (firstFocusedComponent == null)
                {
                    firstFocusedComponent = view.getFirstFocusableComponent();
                }
            }
            else
            {
                view.setDisplayed(false);
            }

            updated |= view.updateView();
        }

        if (updated)
        {
            super.invalidate();
        }
    }

    /**
     * Builds the UI and makes it ready for display.
     */
    private void buildUI()
    {
        Set<String> affectedVariables = new HashSet<String>();

        // need to recreate the panel as TwoColumnLayout doesn't correctly support component removal
        panel.removeAll();
        panel.setLayout(createPanelLayout());

        for (GUIField view : views)
        {
            boolean enabled;
            boolean addToPanel;

            Field fieldDefinition = view.getField();
            Panel metadata = getMetadata();
            boolean required = FieldHelper.isRequired(fieldDefinition, installData, matcher);

            if (required && fieldDefinition.isConditionTrue())
            {
                enabled = !(fieldDefinition.isEffectiveReadonly(
                        metadata.isReadonly()
                        || (metadata.getReadonlyCondition() != null && rules.isConditionTrue(metadata.getReadonlyCondition())),
                        rules));
                addToPanel = true;
                view.setDisplayed(true);
            }
            else if (required
                    && (
                            fieldDefinition.isEffectiveDisplayHidden(
                                    metadata.isDisplayHidden()
                                    || (metadata.getDisplayHiddenCondition() != null && rules.isConditionTrue(metadata.getDisplayHiddenCondition())),
                                    rules)
                       )
                    )
            {
                enabled = false;
                addToPanel = true;
                view.setDisplayed(true);
            }
            else
            {
                enabled = false;
                addToPanel = false;
                view.setDisplayed(false);
            }

            if (addToPanel)
            {
                for (Component component : view.getComponents())
                {
                    component.setEnabled(enabled);
                    panel.add(component.getComponent(), component.getConstraints());
                }
                String var = view.getVariable();
                if (var != null)
                {
                    affectedVariables.add(var);
                }
            }
        }
        getMetadata().setAffectedVariableNames(affectedVariables);
    }

    /**
     * Reads the input installDataGUI from all UI elements and sets the associated variables.
     *
     * @param prompt the prompt to display messages
     * @param skipValidation set to true when wanting to save field data without validating
     * @return {@code true} if the operation is successful, otherwise {@code false}.
     */
    private boolean readInput(Prompt prompt, boolean skipValidation)
    {
        delegatingPrompt.setPrompt(prompt);

        try
        {
            for (GUIField view : views)
            {
                Field field = view.getField();
                if (view.isDisplayed() && field.isConditionTrue())
                {
                    if (saving)
                    {
                        field.setSaving(true);
                    }
                    if (skipValidation)
                    {
                        view.updateField(prompt, true);
                    } else if (!view.updateField(prompt))
                    {
                        return false;
                    }
                }
            }
        }
        finally
        {
            saving = false;
        }
        return true;
    }

    /**
     * Reads the input installDataGUI from all UI elements and sets the associated variables.
     *
     * @param prompt the prompt to display messages
     * @return {@code true} if the operation is successful, otherwise {@code false}.
     */
    private boolean readInput(Prompt prompt)
    {
        return readInput(prompt, false);
    }

    /**
     * Reads the XML specification for the panel layout.
     *
     * @return the panel specification
     * @throws IzPackException for any problems in reading the specification
     */
    private IXMLElement readSpec()
    {
        userInputModel = new UserInputPanelSpec(getResources(), installData, factory, /*rules,*/ matcher);
        return userInputModel.getPanelSpec(getMetadata());
    }

    /**
     * Called by fields that allow revalidation.
     * No validation is required since we do not progress through the installer.
     */
    private void updateDialog()
    {
        if (this.eventsActivated)
        {
            this.eventsActivated = false;
            readInput(LoggingPrompt.INSTANCE, true); // read from the input fields, but don't display a prompt for errors
            updateUIElements();
            buildUI();
            revalidate();
            repaint();
            Panel metadata = getMetadata();
            if (metadata.isVisited())
            {
                Set<String> blockedNames = metadata.getAffectedVariableNames();
                Set<String> originalBlockedNames = installData.getVariables().getBlockedVariableNames(metadata);
                Set<String> addedBlockedNames = new HashSet<String>();
                Set<String> removedBlockedNames = new HashSet<String>();
                if (blockedNames != null)
                {
                    for (String blockedName : blockedNames)
                    {
                        if (!installData.getVariables().isBlockedVariableName(blockedName))
                        {
                            addedBlockedNames.add(blockedName);
                        }
                    }
                }
                if (originalBlockedNames != null)
                {
                    for (String blockedName : originalBlockedNames)
                    {
                        if (!blockedNames.contains(blockedName))
                        {
                            removedBlockedNames.add(blockedName);
                        }
                    }
                }
                if (!addedBlockedNames.isEmpty())
                {
                    installData.getVariables().registerBlockedVariableNames(addedBlockedNames, metadata);
                }
                if (!removedBlockedNames.isEmpty())
                {
                    installData.getVariables().unregisterBlockedVariableNames(removedBlockedNames, metadata);
                }
            }
            this.eventsActivated = true;
        }
    }

    /**
     * Creates the panel layout.
     *
     * @return a new layout
     */
    private TwoColumnLayout createPanelLayout()
    {
        TwoColumnLayout layout;
        // ----------------------------------------------------
        // Set the topBuffer from the attribute. topBuffer=0 is useful
        // if you don't want your panel to be moved up and down during
        // dynamic validation (showing and hiding components within the
        // same panel)
        // Alternativley set the attribute rigid to true and topBuffer will be treated as pixel space
        // rather than the percentage of the screen
        // ----------------------------------------------------
        int topbuff = 25;
        boolean rigid = false;

        try
        {
            topbuff = Integer.parseInt(spec.getAttribute(TOPBUFFER));
        }
        catch (Exception ignore)
        {
            // do nothing
        }
        try
        {
            rigid = Boolean.parseBoolean(spec.getAttribute(RIGID));
        }
        catch (Exception ignore)
        {
            // do nothing
        }
        finally
        {
            layout = new TwoColumnLayout(10, 5, 30, topbuff, rigid, TwoColumnLayout.LEFT);
        }
        return layout;
    }

    /**
     * Adds a scroll pane to the panel.
     */
    private void addScrollPane()
    {
        JScrollPane scroller = new JScrollPane(panel);
        Border emptyBorder = BorderFactory.createEmptyBorder();
        scroller.setBorder(emptyBorder);
        scroller.setViewportBorder(emptyBorder);
        scroller.getVerticalScrollBar().setBorder(emptyBorder);
        scroller.getHorizontalScrollBar().setBorder(emptyBorder);
        add(scroller, BorderLayout.CENTER);
    }

    /**
     * @return Caption for the summary panel. Returns null if summaryKey is not specified.
     */
    @Override
    public String getSummaryCaption()
    {
        String associatedLabel;
        try
        {
            associatedLabel = spec.getAttribute(SUMMARY_KEY);
        }
        catch (Exception setToNull)
        {
            associatedLabel = null;
        }
        return installData.getMessages().get(associatedLabel);
    }

    /**
     * Summarize all the visible views in the panel.
     * @return summary body
     */
    @Override
    public String getSummaryBody()
    {
        if (getMetadata().hasCondition() && !rules.isConditionTrue(getMetadata().getCondition()))
        {
            return null;
        }
        else
        {
            StringBuilder entries = new StringBuilder();

            for (GUIField view : views)
            {
                if (view.isDisplayed() && view.getVariable() != null)
                {
                    if (view instanceof GUICustomField)
                    {
                        entries.append(getCustomSummary((GUICustomField) view));
                    }
                    else
                    {
                        entries.append(getViewSummary(view));
                    }
                }

            }
            return entries.toString();
        }
    }

    /**
     * Extract summary information from regular fields
     *
     * @param view GUI field view
     * @return summary information for a field
     */
    private String getViewSummary(GUIField view)
    {
        String  associatedVariable, associatedLabel, key, value;
        associatedVariable = view.getVariable();
        associatedLabel = view.getSummaryKey();

        if (associatedLabel != null)
        {
            key = installData.getMessages().get(associatedLabel);
            value = installData.getVariable(associatedVariable);
            return (key + " " + value + "<br>");
        }
        return "";
    }

    /**
     * Extract summary information from custom fields.
     *
     * @param customField GUI custom field
     * @return summary information for a custom field
     */
    private String getCustomSummary(GUICustomField customField)
    {
        List<String> labels = customField.getLabels();
        List<String> variables = customField.getVariables();
        int numberOfColumns = labels.size();

        int column = 0;
        int row = 0;
        String tab;
        StringBuilder entry = new StringBuilder();
        String key;
        String value;


        for(String variable : variables)
        {
            boolean firstColumn = (column % numberOfColumns == 0);

            if(!firstColumn)
            {
                tab = "&nbsp;&nbsp;&nbsp;&nbsp;";
                column++;
            }
            else
            {
                tab = "";
                column=1; //Reset to first column
            }

            key = installData.getMessages().get(installData.getMessages().get(labels.get(column-1)));
            value = installData.getVariable(variable);

            if (key != null)
            {
                if (firstColumn)
                {
                    row++;
                    entry.append(String.format("%1$-3s", row + ". "));
                }
                entry.append(tab).append(key);
                entry.append(" ").append(value);
                entry.append("<br>");
            }

        }

        return entry.toString();
    }
}
