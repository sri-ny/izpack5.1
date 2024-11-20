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

package com.izforge.izpack.util;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.substitutor.VariableSubstitutorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Utility methods for logging
 */
public class LogUtils
{
    private static final String LOGGING_BASE_CONFIGURATION = "/com/izforge/izpack/installer/logging/logging.base.properties";
    private static final String LOGGING_CONFIGURATION = "/com/izforge/izpack/installer/logging/logging.properties";

    private static final String FILEHANDLER_CLASSNAME = FileHandler.class.getName();
    private static final String CONSOLEHANDLER_CLASSNAME = ConsoleHandler.class.getName();


    private static final boolean OVERRIDE =
            System.getProperty("java.util.logging.config.class") == null
            && System.getProperty("java.util.logging.config.file") == null;

    public static void loadConfiguration() throws IOException
    {
        if (OVERRIDE)
        {
            loadConfiguration(LOGGING_CONFIGURATION, null);
        }
    }

    public static void loadConfiguration(final String resource, Variables variables) throws IOException
    {
        if (OVERRIDE)
        {
            InputStream resourceStream = null;
            try
            {
                resourceStream = LogUtils.class.getResourceAsStream(resource);
                loadLoggingResource(resourceStream, variables);
            }
            finally
            {
                IOUtils.closeQuietly(resourceStream);
            }
        }
    }

    public static void loadConfiguration(final Properties configuration) throws IOException
    {
        if (OVERRIDE)
        {
            LogManager manager = LogManager.getLogManager();

            // Merge global logging properties
            InputStream baseResourceStream = null;
            try
            {
                baseResourceStream = LogUtils.class.getResourceAsStream(LOGGING_BASE_CONFIGURATION);
                final Properties baseProps = new Properties();
                baseProps.load(baseResourceStream);
                mergeLoggingConfiguration(configuration, baseProps);
            }
            finally
            {
                IOUtils.closeQuietly(baseResourceStream);
            }

            boolean mkdirs = false;
            String pattern = null;
            if (configuration.getProperty("handlers") != null && configuration.getProperty("handlers").contains(FILEHANDLER_CLASSNAME) && manager.getProperty("handlers").contains(FILEHANDLER_CLASSNAME))
            {
                // IzPack maintains just one log file, don't override the existing handler type of it.
                // Special use case: Command line argument -logfile "wins" over the <log-file> tag.
                // Assumption at the moment for optimization: Just FileHandler is used for configurations from install.xml.
                return;
            }
            for (String key : configuration.stringPropertyNames())
            {
                if (key.equals(FILEHANDLER_CLASSNAME + ".pattern"))
                {
                    // Workaround for not normalized file paths, for example ${INSTALL_PATH}/../install_log/name.log
                    // to get them working before creating ${INSTALL_PATH} in the
                    // com.izforge.izpack.installer.unpacker.UnpackerBase.preUnpack phase
                    // otherwise the FileHandler will fail when opening files already in constructor and not recover from that.
                    pattern = FilenameUtils.normalize(configuration.getProperty(key));
                    configuration.setProperty(key, pattern);
                }
                else if (key.equals(FILEHANDLER_CLASSNAME + ".mkdirs"))
                {
                    // This key goes beyond the capabilities of java.util.logging.FileHandler
                    mkdirs = Boolean.parseBoolean(configuration.getProperty(key));
                    configuration.remove(key);
                }
            }
            if (mkdirs && pattern != null)
            {
                FileUtils.forceMkdirParent(new File(pattern));
            }

            // Merge user settings compiled in
            final Properties userProps = new Properties();
            InputStream userPropsStream = LogUtils.class.getResourceAsStream(ResourceManager.getInstallLoggingConfigurationResourceName());
            try
            {
                if (userPropsStream != null)
                {
                    userProps.load(userPropsStream);
                    for (String userPropName : userProps.stringPropertyNames())
                    {
                        if (userPropName.endsWith(".level") && !userPropName.startsWith(FILEHANDLER_CLASSNAME))
                        {
                            String level = userProps.getProperty(userPropName);
                            if (level != null)
                            {
                                configuration.setProperty(userPropName, level);
                            }
                        }
                    }
                }
            }
            finally
            {
                IOUtils.closeQuietly(userPropsStream);
            }

            InputStream defaultResourceStream = null;
            try
            {
                defaultResourceStream = LogUtils.class.getResourceAsStream(LOGGING_CONFIGURATION);
                final Properties defaultProps = new Properties();
                defaultProps.load(defaultResourceStream);
                mergeLoggingConfiguration(configuration, defaultProps);
            }
            finally
            {
                IOUtils.closeQuietly(defaultResourceStream);
            }

            if (Debug.isDEBUG())
            {
                configuration.setProperty(FILEHANDLER_CLASSNAME + ".level", Level.FINE.toString());
                configuration.setProperty(ConsoleHandler.class.getName() + ".level", Level.FINE.toString());
            }

            // Set general log level which acts as filter in front of all handlers
            String fileLevelName = configuration.getProperty(FILEHANDLER_CLASSNAME + ".level", Level.ALL.toString());
            Level fileLevel = Level.ALL;
            if (fileLevelName != null)
            {
                fileLevel = Level.parse(fileLevelName);
            }
            
            String consoleLevelName = configuration.getProperty(CONSOLEHANDLER_CLASSNAME + ".level", Level.INFO.toString());
            Level consoleLevel = Level.INFO;
            if (consoleLevelName != null)
            {
                consoleLevel = Level.parse(consoleLevelName);
            }
            
            configuration.setProperty(".level", (fileLevel.intValue() < consoleLevel.intValue()) ? fileLevelName : consoleLevelName);

            final PipedOutputStream out = new PipedOutputStream();
            final PipedInputStream in = new PipedInputStream(out);
            try
            {
                new Thread(
                        new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    configuration.store(out, null);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                                finally
                                {
                                    IOUtils.closeQuietly(out);
                                }
                            }
                        }
                ).start();

                manager.readConfiguration(in);
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }
        }
    }

    private static void loadLoggingResource(InputStream resourceStream, Variables variables) throws IOException
    {
        if (resourceStream != null)
        {
            InputStream is = variables != null
                    ? new VariableSubstitutorInputStream(
                    resourceStream, null,
                    variables, SubstitutionType.TYPE_JAVA_PROPERTIES, false)
                    : resourceStream;
            final Properties props = new Properties();
            props.load(is);

            loadConfiguration(props);
        }
    }

    private static void mergeLoggingConfiguration(Properties to, Properties from)
    {
        for (String fromName : from.stringPropertyNames())
        {
            String fromValue = from.getProperty(fromName);
            if (fromName.matches("\\.?handlers") && to.containsKey(fromName))
            {
                String oldValue = to.getProperty(fromName);
                if (!fromValue.equals(oldValue))
                {
                    to.setProperty(fromName, oldValue + ", " + fromValue);
                }
                continue;
            }
            if (!to.containsKey(fromName))
            {
                to.setProperty(fromName, fromValue);
            }
        }
    }
}
