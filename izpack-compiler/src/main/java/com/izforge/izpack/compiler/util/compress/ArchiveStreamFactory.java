/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

package com.izforge.izpack.compiler.util.compress;

import com.izforge.izpack.util.compress.SevenZArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArchiveStreamFactory extends org.apache.commons.compress.archivers.ArchiveStreamFactory
{
    private static final Logger logger = Logger.getLogger(ArchiveStreamFactory.class.getName());

    public ArchiveInputStream createArchiveInputStream(File file, InputStream in) throws ArchiveException
    {
        try
        {
            final String mimeType = new Tika().detect(file);
            if ("application/x-7z-compressed".equals(mimeType))
            {
                return new SevenZArchiveInputStream(file);
            }
        }
        catch (final IOException e)
        {
            logger.log(Level.WARNING, "Could not detect mime type of or open archive " + file + ": " + e.getMessage(), e);
        }

        return super.createArchiveInputStream(in);
    }
}
