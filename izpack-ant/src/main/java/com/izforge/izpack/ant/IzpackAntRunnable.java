package com.izforge.izpack.ant;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import org.apache.tools.ant.BuildException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Handler;

/**
 * @author Anthonin Bonnefoy
 */
public class IzpackAntRunnable implements Runnable
{
    private final CompilerData compilerData;
    private final String input;
    private final Properties properties;
    private final Boolean inheritAll;
    private final Hashtable<String, Object> projectProps;
    private final Handler logHandler;

    public IzpackAntRunnable(String compression, String kind, String input, String configText, String basedir,
                             String output, boolean mkdirs, int compressionLevel, Properties properties,
                             Boolean inheritAll, Hashtable<String, Object> antProjectProperties, String izPackDir,
                             Handler logHandler)
    {
        this.compilerData = new CompilerData(compression, kind, input, configText, basedir, output, mkdirs, compressionLevel);
        this.input = input;
        this.properties = properties;
        this.inheritAll = inheritAll;
        this.projectProps = antProjectProperties;
        this.logHandler = logHandler;
        CompilerData.setIzpackHome(izPackDir);
    }


    @Override
    public void run()
    {
        CompilerContainer compilerContainer = new CompilerContainer();
		compilerContainer.addConfig("installFile", input == null ? "<config>" : input);
        compilerContainer.addComponent(CompilerData.class, compilerData);
        compilerContainer.addComponent(Handler.class, logHandler);

        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);
        PropertyManager propertyManager = compilerContainer.getComponent(PropertyManager.class);

        if (properties != null)
        {
            Enumeration<Object> e = properties.keys();
            while (e.hasMoreElements())
            {
                String name = (String) e.nextElement();
                String value = properties.getProperty(name);
                value = fixPathString(value);
                propertyManager.addProperty(name, value);
            }
        }

        if (inheritAll)
        {
            Enumeration<String> e = projectProps.keys();
            while (e.hasMoreElements())
            {
                String name = e.nextElement();
                String value = projectProps.get(name).toString();
                value = fixPathString(value);
                propertyManager.addProperty(name, value);
            }
        }

        try
        {
            compilerConfig.executeCompiler();
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }

    }

    private static String fixPathString(String path)
    {
        /*
        * The following code fixes a bug in in codehaus classworlds loader,
        * which can't handle mixed path strings like "c:\test\../lib/mylib.jar".
        * The bug is in org.codehaus.classworlds.UrlUtils.normalizeUrlPath().
        */
        StringBuilder fixpath = new StringBuilder(path);
        for (int q = 0; q < fixpath.length(); q++)
        {
            if (fixpath.charAt(q) == '\\')
            {
                fixpath.setCharAt(q, '/');
            }
        }
        return fixpath.toString();
    }

}