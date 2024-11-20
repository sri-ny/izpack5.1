/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
 * Copyright 2004 Thomas Guenter
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

package com.izforge.izpack.event;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.helper.SpecHelper;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Installer listener for performing ANT actions. The definition what should be done will be made in
 * a specification file which is referenced by the resource id "AntActionsSpec.xml". There should be
 * an entry in the install.xml file in the sub ELEMENT "res" of ELEMENT "resources" which references
 * it. The specification of the xml file is done in the DTD antaction.dtd. The xml file specifies,
 * for what pack what ant call should be performed at what time of installation.
 *
 * @author Thomas Guenter
 * @author Klaus Bartz
 */
public class AntActionInstallerListener extends AbstractProgressInstallerListener
{
    /**
     * Name of the specification file.
     */
    public static final String SPEC_FILE_NAME = "AntActionsSpec.xml";

    /**
     * The actions.
     */
    private final Map<String, Map<Object, List<AntAction>>> actions
            = new HashMap<String, Map<Object, List<AntAction>>>();

    /**
     * The uninstall actions.
     */
    private final List<AntAction> uninstActions = new ArrayList<AntAction>();

    /**
     * The variable replacer.
     */
    private final VariableSubstitutor replacer;

    /**
     * The uninstallation data.
     */
    private final UninstallData uninstallData;

    /**
     * The specification helper.
     */
    private final SpecHelper spec;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AntActionInstallerListener.class.getName());


    /**
     * Constructs an <tt>AntActionInstallerListener</tt>.
     *
     * @param replacer      the variable substituter
     * @param resources     the resources
     * @param installData   the installation data
     * @param uninstallData the uninstallation data
     * @param notifiers     the progress notifiers
     */
    public AntActionInstallerListener(VariableSubstitutor replacer, Resources resources, InstallData installData,
                                      UninstallData uninstallData, ProgressNotifiers notifiers)
    {
        super(installData, notifiers);
        this.replacer = replacer;
        this.uninstallData = uninstallData;
        spec = new SpecHelper(resources);
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @throws IzPackException for any error
     */
    @Override
    public void beforePacks(List<Pack> packs) throws InstallerException
    {
        try
        {
            // Read it here and not in initialize, for getting the
            // current variable values substituted
            spec.readSpec(SPEC_FILE_NAME);
        }
        catch (Exception exception)
        {
            throw new IzPackException("Failed to read: " + SPEC_FILE_NAME, exception);
        }

        if (spec.getSpec() == null)
        {
            return;
        }

        for (Pack pack : packs)
        {
            // Resolve data for current pack.
            IXMLElement packElement = spec.getPackForName(pack.getName());
            if (packElement == null)
            {
                continue;
            }

            // Prepare the action cache
            Map<Object, List<AntAction>> packActions = new HashMap<Object, List<AntAction>>();
            packActions.put(ActionBase.BEFOREPACK, new ArrayList<AntAction>());
            packActions.put(ActionBase.AFTERPACK, new ArrayList<AntAction>());
            packActions.put(ActionBase.BEFOREPACKS, new ArrayList<AntAction>());
            packActions.put(ActionBase.AFTERPACKS, new ArrayList<AntAction>());

            // Get all entries for antcalls.
            List<IXMLElement> antCallEntries = packElement.getChildrenNamed(AntAction.ANTCALL);
            if (antCallEntries != null && antCallEntries.size() >= 1)
            {
                for (IXMLElement antCallEntry : antCallEntries)
                {
                    AntAction act = readAntCall(antCallEntry);
                    if (act != null)
                    {
                        List<AntAction> antActions = packActions.get(act.getOrder());
                        antActions.add(act);
                    }
                }
                // Set for progress bar interaction.
                if (!packActions.get(ActionBase.AFTERPACKS).isEmpty())
                {
                    setProgressNotifier();
                }
            }

            actions.put(pack.getName(), packActions);
        }
        for (Pack pack : packs)
        {
            String currentPack = pack.getName();
            performAllActions(currentPack, ActionBase.BEFOREPACKS, null);
        }
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack the pack
     * @throws IzPackException for any error
     */
    @Override
    public void beforePack(Pack pack) throws InstallerException
    {
        performAllActions(pack.getName(), ActionBase.BEFOREPACK, null);
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack the pack
     * @throws IzPackException for any error
     */
    @Override
    public void afterPack(Pack pack) throws InstallerException
    {
        performAllActions(pack.getName(), ActionBase.AFTERPACK, null);
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener) throws InstallerException
    {
        if (notifyProgress())
        {
            int count = getActionCount(packs, ActionBase.AFTERPACKS);
            listener.nextStep(getMessage("AntAction.pack"), getProgressNotifierId(), count);
        }
        for (Pack pack : packs)
        {
            String currentPack = pack.getName();
            performAllActions(currentPack, ActionBase.AFTERPACKS, listener);
        }
        if (!uninstActions.isEmpty())
        {
            uninstallData.addAdditionalData("antActions", uninstActions);
        }
    }

    private int getActionCount(List<Pack> packs, String order)
    {
        int result = 0;
        for (Pack pack : packs)
        {
            String currentPack = pack.getName();
            List<AntAction> actList = getActions(currentPack, order);
            if (actList != null)
            {
                result += actList.size();
            }
        }
        return result;
    }

    /**
     * Returns the defined actions for the given pack in the requested order.
     *
     * @param packName name of the pack for which the actions should be returned
     * @param order    order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @return a list which contains all defined actions for the given pack and order
     */
    // -------------------------------------------------------
    private List<AntAction> getActions(String packName, String order)
    {
        Map<Object, List<AntAction>> packActions = actions.get(packName);
        if (packActions == null || packActions.isEmpty())
        {
            return null;
        }

        return packActions.get(order);
    }

    /**
     * Performs all actions which are defined for the given pack and order.
     *
     * @param packName name of the pack for which the actions should be performed
     * @param order    order to be used; valid are <i>beforepack</i> and <i>afterpack</i>
     * @param listener the progress listener. May be {@code null}
     * @throws InstallerException
     */
    private void performAllActions(String packName, String order, ProgressListener listener)
            throws InstallerException
    {
        List<AntAction> actList = getActions(packName, order);
        if (actList == null || actList.isEmpty())
        {
            return;
        }

        // Inform progress bar if needed. Works only on AFTER_PACKS
        boolean notifyProgress = notifyProgress() && order.equals(ActionBase.AFTERPACKS);

        logger.fine("Executing all " + order + " Ant actions of pack " + packName + " ...");
        RulesEngine rules = getInstallData().getRules();
        for (AntAction act : actList)
        {
            if (notifyProgress)
            {
                String message = (act.getMessageID() != null) ? getMessage(act.getMessageID()) : "";
                listener.progress(message);
            }
            try
            {
                String conditionId = act.getConditionId();
                if (conditionId == null || rules.isConditionTrue(conditionId))
                {
                    act.performInstallAction();
                }
            }
            catch (IzPackException e)
            {
                act.throwBuildException(e);
            }
            if (!act.getUninstallTargets().isEmpty())
            {
                uninstActions.add(act);
            }
        }
    }

    /**
     * Returns an ant call which is defined in the given XML element.
     *
     * @param el XML element which contains the description of an ant call
     * @return an ant call which is defined in the given XML element
     * @throws InstallerException
     */
    private AntAction readAntCall(IXMLElement el)
    {
        String buildDir;
        String buildFile;

        if (el == null)
        {
            return null;
        }
        AntAction act = new AntAction();
        try
        {
            act.setOrder(spec.getRequiredAttribute(el, ActionBase.ORDER));
            act.setUninstallOrder(el.getAttribute(ActionBase.UNINSTALL_ORDER, ActionBase.BEFOREDELETION));
        }
        catch (Exception e)
        {
            throw new InstallerException(e);
        }

        act.setQuiet(spec.isAttributeYes(el, "quiet", false));
        act.setVerbose(spec.isAttributeYes(el, "verbose", false));
        if (!(act.isQuiet() || act.isVerbose()))
        {
            String logLevelAttrValue = el.getAttribute("loglevel");
            AntLogLevel logLevel = AntLogLevel.fromName(logLevelAttrValue);
            if (logLevel == null)
            {
                if (logLevelAttrValue != null)
                {
                    throw new InstallerException("Bad value for attribute " + "loglevel");
                }
                logLevel = AntLogLevel.INFO;
            }
            act.setLogLevel(logLevel);
        }

        String severityAttrValue = el.getAttribute("severity");
        AntSeverity severity = AntSeverity.fromName(severityAttrValue);
        if (severity == null)
        {
            if (severityAttrValue != null)
            {
                throw new InstallerException("Bad value for attribute " + "severity");
            }
            severity = AntSeverity.ERROR;
        }
        act.setSeverity(severity.getLevel());

        buildDir = el.getAttribute("dir");
        if (buildDir != null)
        {
            buildDir = resolveVariables(buildDir);
            act.setBuildDir(new File(resolveVariables(buildDir)));
        }
        buildFile = el.getAttribute("buildfile");
        act.setConditionId(el.getAttribute(AntAction.CONDITIONID_ATTR));
        File buildResourceFile = getBuildFileFromResource(spec, el);
        if (null == buildFile && null == buildResourceFile)
        {
            throw new InstallerException(
                    "Invalid " + SPEC_FILE_NAME + ": either buildfile or buildresource must be specified");
        }
        if (null != buildFile && null != buildResourceFile)
        {
            throw new InstallerException(
                    "Invalid " + SPEC_FILE_NAME + ": cannot specify both buildfile and buildresource");
        }
        InstallData installData = getInstallData();
        File effectiveBuildFile;
        String effectiveBaseDir = buildDir!=null ? buildDir : installData.getInstallPath();
        if (null != buildFile)
        {
            try
            {
                effectiveBuildFile = FileUtil.getAbsoluteFile(resolveVariables(buildFile), effectiveBaseDir);
            }
            catch (Exception e)
            {
                effectiveBuildFile = FileUtil.getAbsoluteFile(buildFile, effectiveBaseDir);
            }
        }
        else
        {
            effectiveBuildFile = buildResourceFile;
        }
        act.setBuildFile(effectiveBuildFile);
        String str = el.getAttribute("logfile");
        if (str != null)
        {
            String logAppendValue = el.getAttribute("logfile_append");
            boolean logAppend = false;
            if (logAppendValue != null)
            {
                logAppend = Boolean.parseBoolean(logAppendValue);
            }
            str = resolveVariables(str);
            try
            {
                act.setLogFile(FileUtil.getAbsoluteFile(str, installData.getInstallPath()), logAppend);
            }
            catch (Exception e)
            {
                act.setLogFile(FileUtil.getAbsoluteFile(str, installData.getInstallPath()), logAppend);
            }
        }
        String msgId = el.getAttribute("messageid");
        if (msgId != null && msgId.length() > 0)
        {
            act.setMessageID(msgId);
        }

        // read propertyfiles
        for (IXMLElement propEl : el.getChildrenNamed("propertyfile"))
        {
            act.addPropertyFile(resolveVariables(spec.getRequiredAttribute(propEl, "path")));
        }

        // read properties
        for (IXMLElement propEl : el.getChildrenNamed("property"))
        {
            act.setProperty(
                    spec.getRequiredAttribute(propEl, "name"),
                    resolveVariables(spec.getRequiredAttribute(propEl, "value")));
        }

        // read targets
        for (IXMLElement targEl : el.getChildrenNamed("target"))
        {
            act.addTarget(resolveVariables(spec.getRequiredAttribute(targEl, "name")));
        }

        // read uninstall rules
        for (IXMLElement utargEl : el.getChildrenNamed("uninstall_target"))
        {
            act.addUninstallTarget(resolveVariables(spec.getRequiredAttribute(utargEl, "name")));
        }

        // see if this was an build_resource and there were uninstall actions
        if (null != buildResourceFile && act.getUninstallTargets().size() > 0)
        {
            // We need to add the build_resource file to the uninstaller
            addBuildResourceToUninstallerData(buildResourceFile);
        }

        return act;
    }

    private File getBuildFileFromResource(SpecHelper spec, IXMLElement el)
    {
        File buildResourceFile = null;

        // See if the build file is a resource
        String attr = el.getAttribute("buildresource");
        if (null != attr)
        {
            // Get the resource
            BufferedInputStream bis = new BufferedInputStream(spec.getResource(attr));
            BufferedOutputStream bos = null;
            try
            {
                // Write the resource to a temporary file
                File tempFile = File.createTempFile("resource_"+attr, ".xml");
                tempFile.deleteOnExit();
                bos = new BufferedOutputStream(new FileOutputStream(tempFile));
                int aByte;
                while (-1 != (aByte = bis.read()))
                {
                    bos.write(aByte);
                }
                buildResourceFile = tempFile;
            }
            catch (IOException x)
            {
                throw new InstallerException("I/O error during writing resource " + attr + " to a temporary buildfile", x);
            }
            finally
            {
                IOUtils.closeQuietly(bos);
                IOUtils.closeQuietly(bis);
            }
        }
        return buildResourceFile;
    }

    private void addBuildResourceToUninstallerData(File buildFile) throws InstallerException
    {
        byte[] content;
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) buildFile.length());
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(new FileInputStream(buildFile));
            int aByte;
            while (-1 != (aByte = bis.read()))
            {
                bos.write(aByte);
            }
            content = bos.toByteArray();
            uninstallData.addAdditionalData("build_resource", content);
        }
        catch (Exception x)
        {
            throw new InstallerException("Failed to add buildfile_resource to uninstaller", x);
        }
        finally
        {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(bos);
        }
    }

    private String resolveVariables(String value)
    {
        return replacer.substitute(value);
    }
}
