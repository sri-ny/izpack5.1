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
package com.izforge.izpack.test.provider;

import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.util.Platforms;

import java.io.IOException;

/**
 * Test provider for {@link ConsoleInstallData}.
 *
 * @author Tim Anderson
 */
public class ConsoleInstallDataMockProvider extends AbstractInstallDataMockProvider
{

    /**
     * Provides an {@link ConsoleInstallData}.
     *
     * @param variables the variables
     * @param locales   the locales
     * @return an {@link ConsoleInstallData}
     * @throws IOException if the default messages cannot be found
     */
    public ConsoleInstallData provide(Variables variables, Locales locales) throws IOException
    {
        ConsoleInstallData result = createInstallData(variables);
        populate(result, locales);
        return result;
    }

    /**
     * Creates a new {@link ConsoleInstallData}.
     *
     * @param variables the variables
     * @return a new {@link ConsoleInstallData}
     */
    @Override
    protected ConsoleInstallData createInstallData(Variables variables)
    {
        ConsoleInstallData result = new ConsoleInstallData(variables, Platforms.MAC_OSX);

        ConsolePrefs consolePrefs = new ConsolePrefs();
        consolePrefs.enableConsoleReader = false;
        result.consolePrefs = consolePrefs;

        return result;
    }

}
