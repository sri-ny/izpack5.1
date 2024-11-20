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

package com.izforge.izpack.panels.defaulttarget;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.target.TargetPanelHelper;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.api.config.Options;

import java.util.Properties;

/**
 * Console implementation of the {@link DefaultTargetPanel}.
 *
 * @author Tim Anderson
 */
public class DefaultTargetConsolePanel extends AbstractConsolePanel
{

    private final InstallData installData;
    /**
     * Constructs an {@code DefaultTargetConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public DefaultTargetConsolePanel(PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.installData = installData;
    }

    @Override
    public boolean generateOptions(InstallData installData, Options options)
    {
        options.add(InstallData.INSTALL_PATH, installData.getInstallPath());
        return true;
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return {@code true} if the installation is successful, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        String path = properties.getProperty(InstallData.INSTALL_PATH);
        path = installData.getVariables().replace(path);
        installData.setInstallPath(path);
        return true;
    }

    /**
     * Runs the panel in an interactive console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return {@code true} if the panel ran successfully, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);

        String path = TargetPanelHelper.getPath(installData);
        installData.setInstallPath(path);
        return true;
    }

    @Override
    public void createInstallationRecord(IXMLElement rootElement)
    {
        new DefaultTargetPanelAutomationHelper().createInstallationRecord(installData, rootElement);
    }
}
