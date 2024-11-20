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

package com.izforge.izpack.panels.userinput.console.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.file.FileField;
import com.izforge.izpack.panels.userinput.field.file.TestFileFieldConfig;


/**
 * Tests the {@link ConsoleFileField}.
 *
 * @author Tim Anderson
 */
public class ConsoleFileFieldTest extends AbstractConsoleFieldTest
{

    /**
     * Test file.
     */
    private File file;


    /**
     * Sets up the test.
     *
     * @throws IOException for any error
     */
    @Before
    public void aetUp() throws IOException
    {
        file = File.createTempFile("foo", "bar", FileUtils.getTempDirectory());
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown()
    {
        assertTrue(file.delete());
    }


    /**
     * Verifies that pressing return enters the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        ConsoleFileField field = createField(file.getPath());
        checkValid(field, "\n");

        assertEquals(file.getAbsolutePath(), installData.getVariable("file"));
    }

    @Test
    public void testSetValue()
    {
        ConsoleFileField field = createField(null);
        checkValid(field, file.getPath(), "\n");

        assertEquals(file.getAbsolutePath(), installData.getVariable("file"));
    }

    /**
     * Verify that validation fails if the entered file doesn't exist.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testFileNoExists() throws IOException
    {
        ConsoleFileField field = createField(null);
        checkInvalid(field, "badfile");
        assertNull(installData.getVariable("file"));
    }

    /**
     * Verify that validation fails if the entered path is a directory.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testInvalidDir() throws IOException
    {
        ConsoleFileField field = createField(null);

        File dir = File.createTempFile("foo", "bar", FileUtils.getTempDirectory());
        assertTrue(dir.delete());
        assertTrue(dir.mkdir());
        checkInvalid(field, dir.getPath());
        assertNull(installData.getVariable("file"));

        assertTrue(dir.delete());
    }

    /**
     * Helper to create a field that updates the 'file' variable.
     *
     * @param initialValue the initial value. May be {@code null}
     * @return a new field
     */
    private ConsoleFileField createField(String initialValue)
    {
        TestFileFieldConfig config = new TestFileFieldConfig("file");
        config.setLabel("Enter file: ");
        config.setInitialValue(initialValue);
        FileField model = new FileField(config, installData);
        return new ConsoleFileField(model, console, prompt);
    }


}
