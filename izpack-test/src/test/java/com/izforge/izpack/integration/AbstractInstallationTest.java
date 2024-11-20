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

package com.izforge.izpack.integration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.izforge.izpack.api.data.InstallData;


/**
 * Base class for installation integration test cases.
 *
 * @author Tim Anderson
 */
public class AbstractInstallationTest
{
    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public static final String LOGGING_CONFIGURATION = "/com/izforge/izpack/installer/logging/logging.properties";

    private static Logger logger;


    /**
     * The installation data.
     */
    private InstallData installData;


    /**
     * Constructs an <tt>AbstractInstallationTest</tt>.
     *
     * @param installData the installation data
     */
    public AbstractInstallationTest(InstallData installData)
    {
        this.installData = installData;
        initializeLogging();
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());
        installData.setDefaultInstallPath(installPath.getAbsolutePath());
    }

    /**
     * Returns the install path.
     *
     * @return the install path
     */
    protected String getInstallPath()
    {
        return installData.getInstallPath();
    }

    /**
     * Returns the install data.
     *
     * @return the install data
     */
    protected InstallData getInstallData()
    {
        return installData;
    }

    private static void initializeLogging()
    {
        LogManager manager = LogManager.getLogManager();
        InputStream stream;
        try
        {
            stream = AbstractInstallationTest.class.getResourceAsStream(LOGGING_CONFIGURATION);
            if (stream != null)
            {
                manager.readConfiguration(stream);
            }
        }
        catch (IOException e) {}

        Logger rootLogger = Logger.getLogger("com.izforge.izpack");
        rootLogger.setUseParentHandlers(false);
        rootLogger.setLevel(Level.INFO);

        logger = Logger.getLogger(AbstractInstallationTest.class.getName());
        logger.info("Logging initialized at level '" + rootLogger.getLevel() + "'");
    }
}
