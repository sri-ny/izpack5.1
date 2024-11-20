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

package com.izforge.izpack.compiler.container.provider;

import com.izforge.izpack.compiler.data.CompilerData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.picocontainer.injectors.Provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;

/**
 * Provides the Jar output stream  for the final installer jar
 *
 * @author Anthonin Bonnefoy
 */
public class JarOutputStreamProvider implements Provider
{

    public JarOutputStream provide(CompilerData compilerData)
    {
        File file = new File(compilerData.getOutput());
        JarOutputStream jarOutputStream = null;
        FileOutputStream fileOutputStream = null;
        FileUtils.deleteQuietly(file);
        try
        {
            if (compilerData.isMkdirs())
            {
                FileUtils.forceMkdirParent(file);
            }
            fileOutputStream = new FileOutputStream(file);
            jarOutputStream = new JarOutputStream(fileOutputStream);
            int level = compilerData.getComprLevel();
            if (level >= 0 && level < 10)
            {
                jarOutputStream.setLevel(level);
            } else
            {
                jarOutputStream.setLevel(Deflater.BEST_COMPRESSION);
            }
        }
        catch (IOException e)
        {
            IOUtils.closeQuietly(fileOutputStream);
        }

        return jarOutputStream;
    }
}
