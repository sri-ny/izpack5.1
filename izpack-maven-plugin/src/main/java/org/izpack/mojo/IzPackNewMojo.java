/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.izpack.mojo;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.logging.MavenStyleLogFormatter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Mojo for izpack
 *
 * @author Anthonin Bonnefoy
 */
@Mojo( name = "izpack", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true,
       requiresDependencyResolution = ResolutionScope.TEST)
public class IzPackNewMojo extends AbstractMojo
{
    /**
     * The Maven Session Object
     */
    @Parameter( property = "session", required = true, readonly = true, defaultValue = "${session}" )
    private MavenSession session;
	
    /**
     * The Maven Project Object
     */
    @Parameter( property = "project", required = true, readonly = true, defaultValue = "${project}" )
    private MavenProject project;

    /**
     * Maven ProjectHelper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Format compression. Choices are default (no compression), gzip, bzip2, xz, lzma, deflate
     */
    @Parameter( defaultValue = "default" )
    private String comprFormat;

    /**
     * Kind of installation. Choices are standard (default - file installer) or web
     */
    @Parameter( defaultValue = "standard" )
    private String kind;

    /**
     * Location of the IzPack installation file
     */
    @Parameter( required = true, defaultValue = "${basedir}/src/main/izpack/install.xml" )
    private File installFile;

    /**
     * Base directory of compilation process
     */
    @Parameter( defaultValue = "${project.build.directory}/staging" )
    private File baseDir;

    /**
     * Output where compilation result will be situate
     */
    @Deprecated
    @Parameter
    private File output;

    /**
     * Whether to automatically create parent directories of the output file
     */
    @Parameter( defaultValue = "false" )
    private boolean mkdirs;

    /**
     * Compression level of the installation. Deactivated by default (-1)
     */
    @Parameter( defaultValue = "-1" )
    private int comprLevel;

    /**
     * Whether to automatically include project.url from Maven into
     * IzPack info header
     */
    @Parameter( defaultValue = "false" )
    private boolean autoIncludeUrl;

    /**
     * Whether to automatically include developer list from Maven into
     * IzPack info header
     */
    @Parameter( defaultValue = "false" )
    private boolean autoIncludeDevelopers;

    /**
     * Directory containing the generated JAR.
     */
    @Parameter( defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * Name of the generated JAR.
     */
    @Parameter( alias = "jarName", property = "jar.finalName", defaultValue = "${project.build.finalName}")
    private String finalName;

    /**
     * Classifier to add to the artifact generated. If given, the artifact is attachable.
     * Furthermore, the output file name gets -<i>classifier</i> as suffix.
     * If this is not given,it will merely be written to the output directory
     * according to the finalName.
     */
    @Parameter
    private String classifier;

    /**
     * Whether to attach the generated installer jar to the project
     * as artifact if a classifier is specified.
     * This has no effect if no classifier was specified.
     */
    @Parameter( defaultValue = "true")
    private boolean enableAttachArtifact;

    private PropertyManager propertyManager;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File jarFile = getJarFile();

        CompilerData compilerData = initCompilerData(jarFile);
        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.addConfig("installFile", installFile.getPath());
        compilerContainer.getComponent(IzpackProjectInstaller.class);
        compilerContainer.addComponent(CompilerData.class, compilerData);
        compilerContainer.addComponent(Handler.class, createLogHandler());

        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);

        propertyManager = compilerContainer.getComponent(PropertyManager.class);
        initMavenProperties(propertyManager);

        try
        {
            compilerConfig.executeCompiler();
        }
        catch ( CompilerException e )
        {
            //TODO: This might be enhanced with other exceptions which
            // should be handled like CompilerException
            throw new MojoFailureException( "Failure during compilation process", e );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failure", e );
        }

        if (enableAttachArtifact)
        {
            projectHelper.attachArtifact(project, "jar", classifier, jarFile);
        }
    }

    private File getJarFile()
    {
        File file;

        if (output != null)
        {
            file = output;
        }
        else
        {
            String localClassifier = classifier;
            if (classifier == null || classifier.trim().isEmpty())
            {
                localClassifier = "";
            }
            else if (!classifier.startsWith("-"))
            {
                localClassifier = "-" + classifier;
            }
            file = new File(outputDirectory, finalName + localClassifier + ".jar");
        }

        return file;
    }

    private void initMavenProperties(PropertyManager propertyManager)
    {
    	//TODO - project is annotated as @required, so the check project!=null should be useless!?!
        if (project != null)
        {
            Properties properties = project.getProperties();
            Properties userProps  = session.getUserProperties();
            for (String propertyName : properties.stringPropertyNames())
            {
                String value;
                // TODO: should all user properties be provided as property?
                // Intentionally user properties are searched for properties defined in pom.xml only
                // see https://izpack.atlassian.net/browse/IZPACK-1402 for discussion
                if (userProps.containsKey(propertyName))
                {
                    value = userProps.getProperty(propertyName);
                } else {
                    value = properties.getProperty(propertyName);
                }
                if (propertyManager.addProperty(propertyName, value))
                {
                    getLog().debug("Maven property added: " + propertyName + "=" + value);
                }
                else
                {
                    getLog().warn("Maven property " + propertyName + " could not be overridden");
                }
            }
        }
    }

    private CompilerData initCompilerData(File jarFile)
    {
        Info info = new Info();

        if (project != null)
        {
            if (autoIncludeDevelopers)
            {
                if (project.getDevelopers() != null)
                {
                    //noinspection unchecked
                    for (Developer dev : project.getDevelopers())
                    {
                        info.addAuthor(new Info.Author(dev.getName(), dev.getEmail()));
                    }
                }
            }
            if (autoIncludeUrl)
            {
                info.setAppURL(project.getUrl());
            }
        }
        return new CompilerData(comprFormat, kind, installFile.getPath(), null, baseDir.getPath(), jarFile.getPath(),
                                mkdirs, comprLevel, info);
    }

    private Handler createLogHandler()
    {
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new MavenStyleLogFormatter());
        Log log = getLog();
        Level level = Level.OFF;
        if (log.isDebugEnabled())
        {
            level = Level.FINE;
        }
        else if (log.isInfoEnabled())
        {
            level = Level.INFO;
        }
        else if (log.isWarnEnabled())
        {
            level = Level.WARNING;
        }
        else if (log.isErrorEnabled())
        {
            level = Level.SEVERE;
        }
        consoleHandler.setLevel(level);
        return consoleHandler;
    }

}
