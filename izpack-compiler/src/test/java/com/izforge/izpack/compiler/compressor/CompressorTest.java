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

package com.izforge.izpack.compiler.compressor;

import com.izforge.izpack.api.data.PackCompression;
import com.izforge.izpack.compiler.container.provider.JarOutputStreamProvider;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.packager.impl.AbstractPackagerTest;
import org.junit.Test;

import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Test compressor stream
 *
 * @author Anthonin Bonnefoy
 */
public class CompressorTest
{

    @Test
    public void testBzip2Compression() throws IOException//, CompressorException
    {
        String baseDir = AbstractPackagerTest.getBaseDir().getPath();
        CompilerData data = new CompilerData(
                "",
                baseDir,
                baseDir + "/target/output.jar",
                false);
        data.setComprFormat(PackCompression.BZIP2.toName());
        JarOutputStreamProvider jarOutputStreamProvider = new JarOutputStreamProvider();
        JarOutputStream jarOutputStream = jarOutputStreamProvider.provide(data);
        ZipEntry zipEntry = new ZipEntry("test");
        zipEntry.setComment("bzip2");
        jarOutputStream.putNextEntry(zipEntry);
    }
}
