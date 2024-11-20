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
package com.izforge.izpack.panels.test;

import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.installer.container.provider.MessagesProvider;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.test.provider.ConsoleInstallDataMockProvider;
import com.izforge.izpack.test.util.TestConsole;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Container for testing console panels.
 *
 * @author Tim Anderson
 */
public class TestConsolePanelContainer extends AbstractTestPanelContainer
{

    public TestConsolePanelContainer()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        super.fillContainer(container);
        container.addAdapter(new ProviderAdapter(new MessagesProvider()));
        container.addAdapter(new ProviderAdapter(new ConsoleInstallDataMockProvider()));
        ConsoleInstallData installData = container.getComponent(ConsoleInstallData.class);
        addComponent(ConsolePrefs.class, installData.consolePrefs);
        addComponent(TestConsole.class);
        addComponent(ConsolePrompt.class);

        getComponent(RulesEngine.class); // force creation of the rules
    }
}
