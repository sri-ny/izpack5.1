/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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

package com.izforge.izpack.util.config;

import java.io.File;

public class OptionFileCopyTask extends ConfigurableFileCopyTask
{

    @Override
    protected void doFileOperation(File oldFile, File newFile, File toFile,
            boolean patchPreserveEntries, boolean patchPreserveValues, boolean patchResolveVariables)
            throws Exception
    {
        SingleOptionFileTask task = new SingleOptionFileTask();
        task.setOldFile(oldFile);
        task.setNewFile(newFile);
        task.setToFile(toFile);
        task.setCreate(true);
        task.setPatchPreserveEntries(patchPreserveEntries);
        task.setPatchPreserveValues(patchPreserveValues);
        task.setPatchResolveVariables(patchResolveVariables);
        task.execute();
    }
}
