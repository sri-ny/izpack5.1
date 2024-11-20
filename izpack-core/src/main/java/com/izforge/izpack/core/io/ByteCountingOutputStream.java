/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2001 Johannes Lehtinen
 * Copyright 2002 Paul Wilkinson
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

package com.izforge.izpack.core.io;

import org.apache.commons.io.output.CountingOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream which counts the bytes written through it. Be sure to flush before checking size.
 */
public class ByteCountingOutputStream extends OutputStream
{
    private CountingOutputStream os;

    ByteCountingOutputStream(OutputStream out)
    {
        setOutputStream(out);
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        os.write(b, off, len);
    }

    public void write(byte[] b) throws IOException
    {
        os.write(b);
    }

    public void write(int b) throws IOException
    {
        os.write(b);
    }

    public void close() throws IOException
    {
        os.close();
    }

    public void flush() throws IOException
    {
        os.flush();
    }

    long getByteCount()
    {
        return os.getByteCount();
    }

    void setOutputStream(OutputStream out)
    {
        os = new CountingOutputStream(out);
    }
}
