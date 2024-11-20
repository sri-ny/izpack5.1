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

package com.izforge.izpack.installer.container.impl;


import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.ConsolePanelAutomationHelper;
import com.izforge.izpack.installer.container.provider.*;
import com.izforge.izpack.installer.language.LanguageConsoleDialog;
import com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpackerAutomationHelper;
import com.izforge.izpack.installer.unpacker.ConsolePackResources;
import com.izforge.izpack.util.Console;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Installer container for console installation mode.
 *
 * @author Tim Anderson
 */
public class ConsoleInstallerContainer extends InstallerContainer
{

    /**
     * Constructs a <tt>ConsoleInstallerContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public ConsoleInstallerContainer()
    {
        initialise();
    }

    /**
     * Constructs a <tt>ConsoleInstallerContainer</tt>.
     * <p/>
     * This constructor is provided for testing purposes.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     */
    protected ConsoleInstallerContainer(MutablePicoContainer container)
    {
        initialise(container);
    }

    /**
     * Registers components with the container.
     *
     * @param container the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer container)
    {
        super.registerComponents(container);

        container
                .addAdapter(new ProviderAdapter(new ConsoleInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new ConsolePanelsProvider()))
                .addAdapter(new ProviderAdapter(new MessagesProvider())) // required by ConsolePrompt and Console
                .addAdapter(new ProviderAdapter(new ConsolePrefsProvider())); // required by Console

        container
                .addComponent(Console.class)
                .addComponent(ConsolePrompt.class)
                .addComponent(ConsoleInstaller.class)
                .addComponent(ConsolePanelAutomationHelper.class)
                .addComponent(ConsolePackResources.class)
                .addComponent(MultiVolumeUnpackerAutomationHelper.class)
                .addComponent(LanguageConsoleDialog.class);
    }
}
