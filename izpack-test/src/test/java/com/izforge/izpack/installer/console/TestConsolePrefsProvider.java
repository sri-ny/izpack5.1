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

package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import org.picocontainer.injectors.Provider;


/**
 * Provides an {@link Messages} from the current locale.
 */
public class TestConsolePrefsProvider implements Provider
{

    /**
     * Provides an {@link ConsolePrefs}.
     *
     * @param installData the console installation data
     * @return the console preferences
     */
    public ConsolePrefs provide(ConsoleInstallData installData)
    {
        ConsolePrefs prefs = installData.consolePrefs;
        prefs.enableConsoleReader = false;
        return prefs;
    }
}
