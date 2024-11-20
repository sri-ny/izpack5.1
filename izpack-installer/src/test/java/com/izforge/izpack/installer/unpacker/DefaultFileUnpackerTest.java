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

package com.izforge.izpack.installer.unpacker;


import com.izforge.izpack.util.os.FileQueue;
import org.apache.commons.io.IOUtils;

import java.io.*;


/**
 * Tests the {@link DefaultFileUnpacker} class.
 *
 * @author Tim Anderson
 */
public class DefaultFileUnpackerTest extends AbstractFileUnpackerTest
{

    /**
     * Creates a pack file stream.
     *
     * @param source the source
     * @return a new stream
     * @throws IOException for any I/O error
     */
    @Override
    protected InputStream createPackStream(File source) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(new FileInputStream(source), out);
        out.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Helper to create an unpacker.
     *
     * @param sourceDir the source directory
     * @param queue     the file queue. May be {@code null}
     * @return a new unpacker
     */
    @Override
    protected FileUnpacker createUnpacker(File sourceDir, FileQueue queue)
    {
        return new DefaultFileUnpacker(getCancellable(), queue);
    }

}
