/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.jdkpath;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.path.PathInputBase;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.api.config.Options;

import java.util.Arrays;
import java.util.Properties;

/**
 * The JDKPathPanel panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class JDKPathConsolePanel extends AbstractConsolePanel
{
    private InstallData installData;
    private final VariableSubstitutor variableSubstitutor;
    private final RegistryDefaultHandler handler;

    /**
     * Constructs a <tt>JDKPathConsolePanelHelper</tt>.
     *
     * @param variableSubstitutor the variable substituter
     * @param handler             the registry handler
     * @param panel               the parent panel/view. May be {@code null}
     */
    public JDKPathConsolePanel(VariableSubstitutor variableSubstitutor, RegistryDefaultHandler handler,
                               PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.handler = handler;
        this.installData = installData;
        this.variableSubstitutor = variableSubstitutor;
        JDKPathPanelHelper.initialize(installData);
    }

    public boolean run(InstallData installData, Properties properties)
    {
        String strJDKPath = properties.getProperty(JDKPathPanelHelper.JDK_PATH);
        if (strJDKPath == null || "".equals(strJDKPath.trim()))
        {
            System.err.println("Missing mandatory JDK path!");
            return false;
        }
        else
        {
            try
            {
            	strJDKPath = variableSubstitutor.substitute(strJDKPath);
            }
            catch (Exception e)
            {
                // ignore
            }
            installData.setVariable(JDKPathPanelHelper.JDK_PATH, strJDKPath);
            return true;
        }
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);

        String detectedJavaVersion = "";
        String defaultValue = JDKPathPanelHelper.getDefaultJavaPath(installData, handler);

        if(JDKPathPanelHelper.skipPanel(installData, defaultValue))
        {
            return true;
        }
        String strPath;
        boolean bKeepAsking = true;
        while (bKeepAsking)
        {
            Messages messages = installData.getMessages();
            strPath = console.promptLocation("Select JDK path [" + defaultValue + "] ", defaultValue);
            if (strPath == null)
            {
                return false;
            }
            strPath = strPath.trim();

            strPath = PathInputBase.normalizePath(strPath);
            detectedJavaVersion = JDKPathPanelHelper.getCurrentJavaVersion(strPath, installData.getPlatform());

            String errorMessage = JDKPathPanelHelper.validate(strPath, detectedJavaVersion, messages);
            if (!errorMessage.isEmpty())
            {
                if (errorMessage.endsWith("?"))
                {
                    errorMessage += "\n" + messages.get("JDKPathPanel.badVersion4");
                    String strIn = console.prompt(errorMessage, (String)null);
                    if (strIn == null)
                    {
                        return false;
                    }
                    if (strIn != null && (strIn.equalsIgnoreCase("y") || strIn.equalsIgnoreCase("yes")))
                    {
                        bKeepAsking = false;
                    }
                }
                else
                {
                    console.println(messages.get("PathInputPanel.notValid"));
                }
            }
            else
            {
                bKeepAsking = false;
            }
            installData.setVariable(JDKPathPanelHelper.JDK_PATH, strPath);
        }

        return promptEndPanel(installData, console);
    }

    @Override
    public boolean generateOptions(InstallData installData, Options options)
    {
        final String name =JDKPathPanelHelper.JDK_PATH;
        options.add(name, installData.getVariable(name));
        options.addEmptyLine(name);
        options.putComment(name, Arrays.asList(getPanel().getPanelId()));
        return true;
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new JDKPathPanelAutomationHelper().createInstallationRecord(installData, panelRoot);
    }
}
