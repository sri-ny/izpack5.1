package com.izforge.izpack.panels.checkedhello;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.installer.automation.PanelAutomation;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckedHelloPanelAutomationHelper implements PanelAutomation
{
    /**
     * The registry helper.
     */
    private final RegistryHelper registryHelper;

    /**
     * Determines if the application is already installed.
     */
    private final boolean registered;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(CheckedHelloPanelAutomationHelper.class.getName());

    public CheckedHelloPanelAutomationHelper(RegistryDefaultHandler handler, InstallData installData) throws NativeLibException
    {
        registryHelper = new RegistryHelper(handler, installData);
        registered = registryHelper.isRegistered();
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
        if (registered)
        {
            try
            {
                registryHelper.updateUninstallName();
            }
            catch (NativeLibException ex)
            {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processOptions(InstallData installData, Overrides overrides)
    {
    }
}