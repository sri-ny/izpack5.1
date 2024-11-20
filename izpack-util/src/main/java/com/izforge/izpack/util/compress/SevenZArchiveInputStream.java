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

package com.izforge.izpack.util.compress;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SevenZArchiveInputStream extends ArchiveInputStream
{
    private final SevenZFile zFile;

    private SevenZArchiveEntry currentEntry;
    private long currentBytesRead;


    public SevenZArchiveInputStream(final File file) throws IOException
    {
        this.zFile = new SevenZFile(file);
    }

    @SuppressWarnings("unused")
    public SevenZArchiveInputStream(final File file, final byte[] password) throws IOException
    {
        this.zFile = new SevenZFile(file, password);
    }


    @Override
    public ArchiveEntry getNextEntry() throws IOException
    {
        final SevenZArchiveEntry sevenZArchiveEntry = zFile.getNextEntry();

        currentEntry = sevenZArchiveEntry;
        currentBytesRead = 0;

        if (sevenZArchiveEntry != null)
        {
            return new ArchiveEntry()
            {
                @Override
                public String getName()
                {
                    return sevenZArchiveEntry.getName();
                }

                @Override
                public long getSize()
                {
                    return sevenZArchiveEntry.getSize();
                }

                @Override
                public boolean isDirectory()
                {
                    return sevenZArchiveEntry.isDirectory();
                }

                @Override
                public Date getLastModifiedDate()
                {
                    return sevenZArchiveEntry.getLastModifiedDate();
                }
            };
        }

        return null;
    }

    @Override
    public int read() throws IOException
    {
        if (currentEntry != null && currentEntry.hasStream() && !currentEntry.isAntiItem())
        {
            int totalRead = zFile.read();
            if (totalRead >= 0)
            {
                count(1);
                currentBytesRead++;
            }
            return totalRead;
        }

        return -1;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        if (currentEntry != null && currentEntry.hasStream() && !currentEntry.isAntiItem())
        {
            int totalRead = zFile.read(b);

            if (totalRead >= 0)
            {
                count(totalRead);
                currentBytesRead += totalRead;
            }

            return totalRead;
        }

        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (currentEntry != null && currentEntry.hasStream() && !currentEntry.isAntiItem())
        {
            int totalRead = zFile.read(b, off, len);

            if (totalRead >= 0)
            {
                count(totalRead);
                currentBytesRead += totalRead;
            }

            return totalRead;
        }

        return -1;
    }

    @Override
    public void close() throws IOException
    {
        zFile.close();
    }

    @Override
    public int available() throws IOException {
        if (currentEntry.isDirectory() || !currentEntry.hasStream() || currentEntry.isAntiItem()) {
            return 0;
        }
        final long currentSize = currentEntry.getSize();
        final long currentSizeDiff = currentSize - currentBytesRead;
        if (currentSizeDiff > Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }
        return (int)currentSizeDiff;
    }

    @Override
    public long skip(final long n) throws IOException {
        if (n <= 0 || currentEntry.isDirectory() || !currentEntry.hasStream() || currentEntry.isAntiItem()) {
            return 0;
        }

        final long available = currentEntry.getSize() - currentBytesRead;
        final long skipped = this.skip(Math.min(n, available));
        count(skipped);
        currentBytesRead += skipped;
        return skipped;
    }

    @Override
    public String toString()
    {
        return zFile.toString();
    }

    @Override
    public boolean canReadEntryData(ArchiveEntry archiveEntry)
    {
        if (archiveEntry instanceof SevenZArchiveEntry)
        {
            SevenZArchiveEntry entry = (SevenZArchiveEntry) archiveEntry;
            return entry.hasStream() && !entry.isAntiItem();
        }

        return false;
    }
}
