package com.izforge.izpack.panels.htmlhello;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.installer.automation.PanelAutomation;

public class HTMLHelloPanelAutomationHelper implements PanelAutomation
{

    public HTMLHelloPanelAutomationHelper()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement panelRoot)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processOptions(InstallData installData, Overrides overrides) {}
}