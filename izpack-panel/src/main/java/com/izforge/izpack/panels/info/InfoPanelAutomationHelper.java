package com.izforge.izpack.panels.info;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.installer.automation.PanelAutomation;

public class InfoPanelAutomationHelper implements PanelAutomation
{

    public InfoPanelAutomationHelper() {}

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