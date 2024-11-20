/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.packager;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.api.data.PackInfo;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Interface for all packager implementations
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public interface IPackager
{

    /**
     * Create the installer, beginning with the specified jar. If the name specified does not end in
     * ".jar", it is appended. If secondary jars are created for packs (if the Info object added has
     * a webDirURL set), they are created in the same directory, named sequentially by inserting
     * ".pack#" (where '#' is the pack number) ".jar" suffix: e.g. "foo.pack1.jar". If any file
     * exists, it is overwritten.
     */
    void createInstaller() throws Exception;

    /**
     * Sets the informations related to this installation.
     *
     * @param info The info section.
     */
    void setInfo(Info info);

    /**
     * Sets the GUI preferences.
     *
     * @param prefs The new gUIPrefs value
     */
    void setGUIPrefs(GUIPrefs prefs);

    /**
     * Sets the console preferences.
     *
     * @param prefs The new console preferences
     */
    void setConsolePrefs(ConsolePrefs prefs);

    /**
     * Allows access to add, remove and update the variables for the project, which are maintained
     * in the packager.
     *
     * @return map of variable names to values
     */
    Properties getVariables();

    /**
     * Add a custom data like custom actions, where order is important. Only one copy of the class
     * files neeed are inserted in the installer.
     *
     * @param ca  custom action object
     * @param url the URL to include once
     */
    void addCustomJar(CustomData ca, URL url);

    /**
     * Adds a pack, order is mostly irrelevant.
     *
     * @param pack contains all the files and items that go with a pack
     */
    void addPack(PackInfo pack);

    /**
     * Gets the packages list
     */
    List<PackInfo> getPacksList();

    /**
     * Adds a language pack.
     *
     * @param iso3    The ISO3 code.
     * @param xmlURL  The location of the xml local info
     * @param flagURL The location of the flag image resource
     */
    void addLangPack(String iso3, URL xmlURL, URL flagURL);

    /**
     * Adds a resource.
     *
     * @param resId The resource Id.
     * @param url   The location of the data
     */
    void addResource(String resId, URL url);

    /**
     * Adds a native library.
     *
     * @param name The native library name.
     * @param url  The url to get the data from.
     */
    void addNativeLibrary(String name, URL url);

    /**
     * Adds a jar file content to the installer. Package structure is maintained. Need mechanism to
     * copy over signed entry information.
     *
     * @param jarURL The url of the jar to add to the installer. We use a URL so the jar may be
     *               nested within another.
     */
    void addJarContent(URL jarURL);

    /**
     * Marks a native library to be added to the uninstaller.
     *
     * @param data the describing custom action data object
     */
    void addNativeUninstallerLibrary(CustomData data);

    void addInstallerRequirements(List<InstallerRequirement> conditions);

    /**
     * Adds configuration information to the packager.
     *
     * @param data - the xml-element packaging from the install.xml
     */
    void addConfigurationInformation(IXMLElement data);

    /**
     * @return the rules
     */
    Map<String, Condition> getRules();

    /**
     * Returns a map of dynamically refreshed variables
     *
     * @return the map
     */
    Map<String, List<DynamicVariable>> getDynamicVariables();

    /**
     * Returns a list of dynamically checked conditions
     *
     * @return the list
     */
    List<DynamicInstallerRequirementValidator> getDynamicInstallerRequirements();

    /**
     * Add a panel, where order is important. Only one copy of the class files needed are inserted in
     * the installer. The panel class is automatically searched in the classpath.
     */
    void addPanel(Panel panel);
    
    /**
     * Returns the list of panels.
     * 
     * @return the panels
     */
    List<Panel> getPanelList();
}
