/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 JBoss Inc
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

package com.izforge.izpack.panels.installationgroup;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.installer.automation.PanelAutomation;

import java.util.List;
import java.util.logging.Logger;

/**
 * An automation helper for the InstallationGroupPanel
 */
public class InstallationGroupPanelAutomationHelper implements PanelAutomation
{
    private static final Logger logger = Logger.getLogger(InstallationGroupPanelAutomationHelper.class.getName());

    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement rootElement)
    {
        GroupData[] rows = (GroupData[]) installData.getAttribute("GroupData");
        String selectedInstallGroup = installData.getVariable("INSTALL_GROUP");

        // Write out the group to pack mappings
        for (GroupData groupData : rows)
        {
            if (groupData.name.equals(selectedInstallGroup))
            {
              IXMLElement xgroup = new XMLElementImpl("group", rootElement);
              xgroup.setAttribute("name", groupData.name);
              for (String name : groupData.packNames)
              {
                  IXMLElement xpack = new XMLElementImpl("pack", xgroup);
                  xpack.setContent(name);
                  xgroup.addChild(xpack);
              }
              rootElement.addChild(xgroup);
            }
        }
    }

    @Override
    public void runAutomated(InstallData idata, IXMLElement panelRoot)
    {
        IXMLElement group = panelRoot.getFirstChildNamed("group");
        if (group != null)
        {
            String name = group.getAttribute("name");
            idata.getSelectedPacks().clear();
            List<IXMLElement> packs = group.getChildrenNamed("pack");
            logger.fine(name + " pack count: " + packs.size());
            logger.fine("Available pack count: " + idata.getAvailablePacks().size());
            for (IXMLElement xpack : packs)
            {
                String packName = xpack.getContent();
                for (Pack pack: idata.getAvailablePacks())
                {
                    if (pack.getName().equals(packName))
                    {
                        idata.getSelectedPacks().add(pack);
                        logger.fine("Added pack: " + pack.getName());
                        break;
                    }
                }
            }
            logger.fine("Set selectedPacks to: " + idata.getSelectedPacks());
        }
    }

    @Override
    public void processOptions(InstallData installData, Overrides overrides) {}
}
