/*
 * $Id:$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Klaus Bartz
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

package com.izforge.izpack.compiler.packager.impl;

import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.merge.PanelMerge;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.compiler.util.graph.DependencyGraph;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * The packager base class. The packager interface <code>IPackager</code> is used by the compiler to put files into an installer, and
 * create the actual installer files. The packager implementation depends on different requirements (e.g. normal packager versus multi volume packager).
 * This class implements the common used method which can also be overload as needed.
 *
 * @author Klaus Bartz
 */
public abstract class PackagerBase implements IPackager
{

    /**
     * Path to resources in jar
     */
    public static final String RESOURCES_PATH = "resources/";

    protected static final String PACKSINFO_RESOURCE_PATH = RESOURCES_PATH + "packs.info";

    /**
     * Variables.
     */
    private final Properties properties;

    /**
     * The listeners.
     */
    private final PackagerListener listener;

    /**
     * Executable zipped output stream. First to open, last to close.
     */
    private final JarOutputStream installerJar;

    /**
     * The merge manager.
     */
    private final MergeManager mergeManager;

    /**
     * The path resolver.
     */
    private final CompilerPathResolver pathResolver;

    /**
     * The mergeable resolver.
     */
    private final MergeableResolver mergeableResolver;

    /**
     * The compiler data.
     */
    private final CompilerData compilerData;


    /**
     * The rules engine.
     */
    private final RulesEngine rulesEngine;

    /**
     * Installer requirements.
     */
    private List<InstallerRequirement> installerRequirements;

    /**
     * Basic installer info.
     */
    private Info info;

    /**
     * GUI preferences.
     */
    private GUIPrefs guiPrefs;

    /**
     * Console preferences.
     */
    private ConsolePrefs consolePrefs;

    /**
     * The ordered panels.
     */
    private final List<Panel> panelList = new ArrayList<Panel>();

    /**
     * The ordered pack information.
     */
    private final List<PackInfo> packsList = new ArrayList<PackInfo>();

    /**
     * The ordered language pack locale names.
     */
    private final List<String> langpackNameList = new ArrayList<String>();

    /**
     * The ordered custom actions information.
     */
    private final List<CustomData> customDataList = new ArrayList<CustomData>();

    /**
     * The language pack URLs keyed by locale name (e.g. de_CH).
     */
    private final Map<String, URL> installerResourceURLMap = new HashMap<String, URL>();

    /**
     * The conditions.
     */
    private final Map<String, Condition> rules = new HashMap<String, Condition>();

    /**
     * Dynamic variables.
     */
    private final Map<String, List<DynamicVariable>> dynamicVariables = new HashMap<String, List<DynamicVariable>>();

    /**
     * Dynamic conditions.
     */
    private final List<DynamicInstallerRequirementValidator> dynamicInstallerRequirements =
            new ArrayList<DynamicInstallerRequirementValidator>();

    /**
     * Constructs a <tt>PackagerBase</tt>.
     *
     * @param properties        the properties
     * @param listener          the packager listener
     * @param installerJar      the installer jar output stream
     * @param mergeManager      the merge manager
     * @param pathResolver      the path resolver
     * @param mergeableResolver the mergeable resolver
     * @param compilerData      the compiler data
     */
    public PackagerBase(Properties properties, PackagerListener listener, JarOutputStream installerJar,
                        MergeManager mergeManager, CompilerPathResolver pathResolver,
                        MergeableResolver mergeableResolver, CompilerData compilerData,
                        RulesEngine rulesEngine)
    {
        this.properties = properties;
        this.listener = listener;
        this.installerJar = installerJar;
        this.mergeManager = mergeManager;
        this.pathResolver = pathResolver;
        this.mergeableResolver = mergeableResolver;
        this.compilerData = compilerData;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public void addCustomJar(CustomData ca, URL url)
    {
        if (ca != null)
        {
            customDataList.add(ca); // serialized to keep order/variables correct
        }

        if (url != null)
        {
            addJarContent(url); // each included once, no matter how many times added
        }
    }

    @Override
    public void addJarContent(URL jarURL)
    {
        sendMsg("Adding content of jar: " + jarURL.getFile(), PackagerListener.MSG_VERBOSE);
        mergeManager.addResourceToMerge(mergeableResolver.getMergeableFromURL(jarURL));
    }

    @Override
    public void addLangPack(String iso3, URL xmlURL, URL flagURL)
    {
        sendMsg("Adding langpack: " + iso3, PackagerListener.MSG_VERBOSE);
        // put data & flag as entries in installer, and keep array of iso3's
        // names
        langpackNameList.add(iso3);
        addResource("flag." + iso3, flagURL);
        installerResourceURLMap.put("langpacks/" + iso3 + ".xml", xmlURL);
    }

    @Override
    public void addNativeLibrary(String name, URL url)
    {
        sendMsg("Adding native library: " + name, PackagerListener.MSG_VERBOSE);
        installerResourceURLMap.put("native/" + name, url);
    }


    @Override
    public void addNativeUninstallerLibrary(CustomData data)
    {
        customDataList.add(data); // serialized to keep order/variables
        // correct

    }

    @Override
    public void addPack(PackInfo pack)
    {
        packsList.add(pack);
    }

    @Override
    public void addPanel(Panel panel)
    {
        sendMsg("Adding panel: " + panel.getPanelId() + " :: Classname : " + panel.getClassName());
        panelList.add(panel); // serialized to keep order/variables correct
        PanelMerge mergeable = pathResolver.getPanelMerge(panel.getClassName());
        mergeManager.addResourceToMerge(mergeable);
    }

    @Override
    public void addResource(String resId, URL url)
    {
        sendMsg("Adding resource: " + resId, PackagerListener.MSG_VERBOSE);
        URL oldUrl = installerResourceURLMap.put(resId, url);
        if (oldUrl != null)
        {
            throw new CompilerException("Resource '" + resId + "' has been already defined at URL '" + oldUrl + "'"
            + " and going to be overridden by URL '" + url +  "'");
        }
    }

    @Override
    public List<PackInfo> getPacksList()
    {
        return packsList;
    }

    @Override
    public List<Panel> getPanelList()
    {
        return panelList;
    }
    
    @Override
    public Properties getVariables()
    {
        return properties;
    }

    @Override
    public void setGUIPrefs(GUIPrefs prefs)
    {
        sendMsg("Setting the GUI preferences", PackagerListener.MSG_VERBOSE);
        guiPrefs = prefs;
    }

    @Override
    public void setConsolePrefs(ConsolePrefs prefs)
    {
        sendMsg("Setting the console preferences", PackagerListener.MSG_VERBOSE);
        consolePrefs = prefs;
    }

    @Override
    public void setInfo(Info info)
    {
        sendMsg("Setting the installer information", PackagerListener.MSG_VERBOSE);
        this.info = info;
    }

    public Info getInfo()
    {
        return info;
    }

    /**
     * @return the rules
     */
    @Override
    public Map<String, Condition> getRules()
    {
        return this.rules;
    }

    /**
     * @return the dynamic variables
     */
    @Override
    public Map<String, List<DynamicVariable>> getDynamicVariables()
    {
        return dynamicVariables;
    }

    /**
     * @return the dynamic conditions
     */
    @Override
    public List<DynamicInstallerRequirementValidator> getDynamicInstallerRequirements()
    {
        return dynamicInstallerRequirements;
    }

    @Override
    public void addInstallerRequirements(List<InstallerRequirement> conditions)
    {
        this.installerRequirements = conditions;
    }

    @Override
    public void createInstaller() throws Exception
    {
        info.setInstallerBase(compilerData.getOutput().replaceAll(".jar", ""));
        JarOutputStream jarOutputStream = getInstallerJar();
        try
        {
            sendStart();
            writeInstaller();
            sendStop();
            jarOutputStream.flush();
        }
        finally
        {
            IOUtils.closeQuietly(jarOutputStream);
        }
    }

    /**
     * Determines if each pack is to be included in a separate jar.
     *
     * @return <tt>true</tt> if {@link Info#getWebDirURL()} is non-null
     */
    protected boolean packSeparateJars()
    {
        return info != null && info.getWebDirURL() != null;
    }

    private List<DynamicVariable> buildVariableList()
    {
        DependencyGraph<DynamicVariable> graph = new DependencyGraph<DynamicVariable>();
        for (List<DynamicVariable> dynVariables : dynamicVariables.values())
        {
            for (DynamicVariable var : dynVariables)
            {
                graph.addVertex(var);
                for (String childName : var.getVarRefs(rulesEngine))
                {
                    List<DynamicVariable> childVars = dynamicVariables.get(childName);
                    if (childVars != null)
                    {
                        for (DynamicVariable childVar : childVars)
                        {
                            graph.addEdge(var, childVar);
                        }
                    }
                }
            }
        }
        return graph.getOrderedList();
    }

    /**
     * Writes the installer.
     *
     * @throws IOException for any I/O error
     */
    protected void writeInstaller() throws IOException
    {
        // write the installer jar. MUST be first so manifest is not overwritten by an included jar
        writeManifest();
        writeSkeletonInstaller();

        writeInstallerObject("info", info);
        writeInstallerObject("vars", properties);
        writeInstallerObject("ConsolePrefs", consolePrefs);
        writeInstallerObject("GUIPrefs", guiPrefs);
        writeInstallerObject("panelsOrder", panelList);
        writeInstallerObject("customData", customDataList);
        writeInstallerObject("langpacks.info", langpackNameList);
        writeInstallerObject("rules", rules);
        writeInstallerObject("dynvariables", buildVariableList());
        writeInstallerObject("dynconditions", dynamicInstallerRequirements);
        writeInstallerObject("installerrequirements", installerRequirements);

        writeInstallerResources();

        // Pack File Data may be written to separate jars
        writePacks();
    }

    /**
     * Write manifest in the install jar.
     *
     * @throws IOException for any I/O error
     */
    protected void writeManifest() throws IOException
    {
        Manifest manifest = new Manifest(PackagerBase.class.getResourceAsStream("MANIFEST.MF"));
        File tempManifest = File.createTempFile("MANIFEST", ".MF", FileUtils.getTempDirectory());
        tempManifest.deleteOnExit();
        FileOutputStream manifestOutputStream = FileUtils.openOutputStream(tempManifest);
        try
        {
            manifest.write(manifestOutputStream);
            mergeManager.addResourceToMerge(tempManifest.getAbsolutePath(), "META-INF/MANIFEST.MF");
        }
        finally
        {
            IOUtils.closeQuietly(manifestOutputStream);
        }
    }

    /**
     * Write skeleton installer to the installer jar.
     */
    protected void writeSkeletonInstaller()
    {
        sendMsg("Copying the skeleton installer", PackagerListener.MSG_VERBOSE);
        mergeManager.addResourceToMerge("com/izforge/izpack/installer/");
        mergeManager.addResourceToMerge("org/picocontainer/");
        mergeManager.addResourceToMerge("com/izforge/izpack/img/");
        mergeManager.addResourceToMerge("com/izforge/izpack/bin/icons/");
        mergeManager.addResourceToMerge("com/izforge/izpack/api/");
        mergeManager.addResourceToMerge("com/izforge/izpack/event/");
        mergeManager.addResourceToMerge("com/izforge/izpack/core/");
        mergeManager.addResourceToMerge("com/izforge/izpack/data/");
        mergeManager.addResourceToMerge("com/izforge/izpack/gui/");
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/");
        mergeManager.addResourceToMerge("com/izforge/izpack/util/");
        mergeManager.addResourceToMerge("com/izforge/izpack/logging/");
        mergeManager.addResourceToMerge("com/coi/tools/");
        mergeManager.addResourceToMerge("org/apache/commons/io/");
        mergeManager.addResourceToMerge("jline/");
        mergeManager.addResourceToMerge("org/fusesource/");
        PackCompression comprFormat = info.getCompressionFormat();
        switch (comprFormat)
        {
            case DEFAULT:
                break;
            default:
                mergeManager.addResourceToMerge("org/apache/commons/compress");
        }
        if (comprFormat == PackCompression.XZ || comprFormat == PackCompression.LZMA)
        {
            mergeManager.addResourceToMerge("org/tukaani/xz");
        }
        mergeManager.addResourceToMerge("META-INF/native/");
        mergeManager.merge(installerJar);
    }

    /**
     * Write an arbitrary object to installer jar.
     *
     * @throws IOException for any I/O error
     */
    protected void writeInstallerObject(String entryName, Object object) throws IOException
    {
        installerJar.putNextEntry(new ZipEntry(RESOURCES_PATH + entryName));
        ObjectOutputStream out = new ObjectOutputStream(installerJar);
        try
        {
            out.writeObject(object);
        }
        catch (IOException e)
        {
            throw new IOException("Error serializing instance of " + object.getClass().getName()
                                          + " as entry \"" + entryName + "\"", e);
        }
        finally
        {
            out.flush();
            installerJar.closeEntry();
        }
    }

    /**
     * Write the data referenced by URL to installer jar.
     *
     * @throws IOException for any I/O error
     */
    protected void writeInstallerResources() throws IOException
    {
        sendMsg("Copying " + installerResourceURLMap.size() + " files into installer");

        for (Map.Entry<String, URL> stringURLEntry : installerResourceURLMap.entrySet())
        {
            URL url = stringURLEntry.getValue();
            InputStream in = null;
            try
            {
                in = url.openStream();
                ZipEntry newEntry = new ZipEntry(RESOURCES_PATH + stringURLEntry.getKey());
                long dateTime = FileUtil.getFileDateTime(url);
                if (dateTime != -1)
                {
                    newEntry.setTime(dateTime);
                }

                try
                {
                    installerJar.putNextEntry(newEntry);
                    IOUtils.copy(in, installerJar);
                }
                finally
                {
                    installerJar.closeEntry();
                }
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }
        }
    }

    /**
     * Write packs to the installer jar, or each to a separate jar.
     *
     * @throws IOException for any I/O error
     */
    protected abstract void writePacks() throws IOException;

    /**
     * Returns the installer jar stream.
     *
     * @return the installer jar stream
     */
    protected JarOutputStream getInstallerJar()
    {
        return installerJar;
    }

    /**
     * Dispatches a message to the listeners.
     *
     * @param job the job description.
     */
    protected void sendMsg(String job)
    {
        sendMsg(job, PackagerListener.MSG_INFO);
    }

    /**
     * Dispatches a message to the listeners at specified priority.
     *
     * @param job      the job description.
     * @param priority the message priority.
     */
    protected void sendMsg(String job, int priority)
    {
        if (listener != null)
        {
            listener.packagerMsg(job, priority);
        }
    }

    /**
     * Dispatches a start event to the listeners.
     */
    protected void sendStart()
    {
        if (listener != null)
        {
            listener.packagerStart();
        }
    }

    /**
     * Dispatches a stop event to the listeners.
     */
    protected void sendStop()
    {
        if (listener != null)
        {
            listener.packagerStop();
        }
    }

}
