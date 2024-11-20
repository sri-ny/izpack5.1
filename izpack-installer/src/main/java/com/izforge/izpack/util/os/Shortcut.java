/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
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

package com.izforge.izpack.util.os;

import com.izforge.izpack.installer.data.UninstallData;

import java.io.UnsupportedEncodingException;
import java.util.List;

/*---------------------------------------------------------------------------*/

/**
 * This class represents a shortcut in a operating system independent way. OS specific subclasses
 * are used to implement the necessary mapping from this generic API to the classes that reflect the
 * system dependent AIP.
 *
 * @author Elmar Grom
 * @version 0.0.1 / 3/4/02
 * @see com.izforge.izpack.util.TargetFactory
 */
public abstract class Shortcut
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------
    /**
     * APPLICATIONS = 1
     */
    public static final int APPLICATIONS = 1;

    /**
     * START_MENU = 2
     */
    public static final int START_MENU = 2;

    /**
     * DESKTOP = 3
     */
    public static final int DESKTOP = 3;

    /**
     * START_UP = 4
     */
    public static final int START_UP = 4;

    /**
     * HIDE = 0 (Hide the window when starting.)
     */
    public static final int HIDE = 0;

    /**
     * NORMAL = 1 Show the window 'normal' when starting. Usually restores the window properties at
     * the last shut-down.
     */
    public static final int NORMAL = 1;

    /**
     * MINIMIZED = 2
     */
    public static final int MINIMIZED = 2;

    /**
     * MAXIMIZED = 3 (Show the window maximized when starting.)
     */
    public static final int MAXIMIZED = 3;

    /**
     * CURRENT_USER = 1 (identifies the user type as the current user)
     */
    public static final int CURRENT_USER = 1;

    /**
     * ALL_USERS = 2 (identifies the user type as valid for all users)
     */
    public static final int ALL_USERS = 2;

    /**
     * indicates that this shortcut should be created for all users or only me *
     */
    private Boolean createForAll;

    /**
     * internal field UninstallData uninstaller
     */
    protected UninstallData uninstaller;

    /*--------------------------------------------------------------------------*/

    /**
     * This method initializes the object. It is used as a replacement for the contructor because of
     * the way it is instantiated through the <code>TargetFactory</code>.
     *
     * @param type the type or classification of the program group in which the link should exist.
     * @param name the name of the shortcut.
     */
    public abstract void initialize(int type, String name) throws Exception;

    /*--------------------------------------------------------------------------*/

    /**
     * Returns the base path of the shortcut depending on type. The base path is the directory that
     * the short cut, (or its program group) will be created in. For instance, on Windows NT, a
     * shortcut with user-type ALL_USERS, and link-type DESKTOP might have the base path
     * "C:\Program&nbsp;Files\All&nbsp;Users\Desktop"
     *
     * @see #setLinkType(int)
     * @see #setUserType(int)
     */
    public String getBasePath() throws Exception
    {
        return ("");
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Returns a list of currently existing program groups, based on the requested type. For example
     * if the type is <code>APPLICATIONS</code> then all the names of the program groups in the
     * Start Menu\Programs menu would be returned.
     *
     * @param userType the type of user for the program group set.
     * @return a <code>Vector</code> of <code>String</code> objects that represent the names of the
     * existing program groups. It is theoretically possible that this list is empty.
     * @see #APPLICATIONS
     * @see #START_MENU
     */
    public abstract List<String> getProgramGroups(int userType);

    /*--------------------------------------------------------------------------*/

    /**
     * Subclass implementations return the fully qualified file name under which the link is saved
     * on disk. <b>Note:</b> this method returns valid results only if the instance was created from
     * a file on disk or after a successful save operation. An instance of this class returns an
     * empty string.
     *
     * @return an empty <code>String</code>
     */
    public abstract String getFileName();

    /*--------------------------------------------------------------------------*/

    /**
     * Returns <code>true</code> if the target OS supports current user and all users.
     *
     * @return <code>true</code> if the target OS supports current and all users.
     */
    public abstract boolean multipleUsers();

    /*--------------------------------------------------------------------------*/

    /**
     * Determines if a specific instance of this class supports the creation of shortcuts. The use
     * of this method might seem odd, since one would not implement a flavor of this class that does
     * not actually support the creation of shortcuts. In other words all flavors will in all
     * probability return true. The only version that can be expected to return false is this class
     * itself, since it has no actual implementation for shortcut creation. This is left to OS
     * specific flavors. If the installer is launched on a unsupported OS there will be no
     * appropriate flavor of this class, which will cause this class itself to be instantiated. The
     * client code can now determine by calling this method if the active OS is supported and take
     * appropriate action.
     *
     * @return <code>true</code> if the creation of shortcuts is supported, <code>false</code> if
     * this is not supported.
     */
    public abstract boolean supported();

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the command line arguments that will be passed to the target when the link is activated.
     *
     * @param arguments the command line arguments
     */
    public abstract void setArguments(String arguments);

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the description string that is used to identify the link in a menu or on the desktop.
     *
     * @param description the descriptiojn string
     */
    public abstract void setDescription(String description);

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the path of the shortcut icon that will be shown on the desktop.
     *
     * @param path a fully qualified file name of a file that contains the icon.
     * @param index the index of the specific icon to use in the file. If there is only one icon in
     * the file, use an index of 0.
     */
    public abstract void setIconLocation(String path, int index);

    /*--------------------------------------------------------------------------*/

    /**
     * Returns the path of the shortcut icon that will be shown on the desktop.
     *
     * @return icon path
     */
    public abstract String getIconLocation();

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the name of the program group this ShellLinbk should be placed in.
     *
     * @param groupName the name of the program group
     */
    public abstract void setProgramGroup(String groupName);

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the show command that is passed to the target application when the link is activated.
     * The show command determines if the the window will be restored to the previous size,
     * minimized, maximized or visible at all. <br>
     * <br>
     * <b>Note:</b><br>
     * Using <code>HIDE</code> will cause the target window not to show at all. There is not even a
     * button on the taskbar. This is a very useful setting when batch files are used to launch a
     * Java application as it will then appear to run just like any native Windows application.<br>
     *
     * @param show the show command. Valid settings are: <br>
     * <ul>
     * <li>{@link com.izforge.izpack.util.os.Shortcut#HIDE}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#NORMAL}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#MINIMIZED}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#MAXIMIZED}
     * </ul>
     * @see #getShowCommand
     */
    public abstract void setShowCommand(int show);

    /*
     * retrieves showCommand from the OS. Translates it into Shortcut.XXX terms.
     */

    public int getShowCommand()
    {
        return Shortcut.NORMAL;
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the absolute path to the shortcut target.
     *
     * @param path the fully qualified file name of the target
     */
    public abstract void setTargetPath(String path);

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the working directory for the link target.
     *
     * @param dir the working directory
     */
    public abstract void setWorkingDirectory(String dir);

    /*--------------------------------------------------------------------------*/

    /**
     * Gets the working directory for the link target.
     *
     * @return the working directory.
     */
    public String getWorkingDirectory()
    {
        return "";
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the name shown in a menu or on the desktop for the link.
     *
     * @param name The name that the link should display on a menu or on the desktop. Do not include
     * a file extension.
     */
    public abstract void setLinkName(String name);

    /*--------------------------------------------------------------------------*/

    /**
     * Gets the type of link types are: <br>
     * <ul>
     * <li>{@link com.izforge.izpack.util.os.Shortcut#DESKTOP}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#APPLICATIONS}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_MENU}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_UP}
     * </ul>
     */
    public abstract int getLinkType();

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the type of link
     *
     * @param type The type of link desired. The following values can be set:<br>
     * <ul>
     * <li>{@link com.izforge.izpack.util.os.Shortcut#DESKTOP}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#APPLICATIONS}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_MENU}
     * <li>{@link com.izforge.izpack.util.os.Shortcut#START_UP}
     * </ul>
     * @throws IllegalArgumentException if an an invalid type is passed
     * @throws UnsupportedEncodingException
     */
    public abstract void setLinkType(int type)
            throws IllegalArgumentException, UnsupportedEncodingException;

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the user type for the link
     *
     * @param type the type of user for the link.
     * @see #CURRENT_USER
     * @see #ALL_USERS
     */
    public abstract void setUserType(int type);

    /*--------------------------------------------------------------------------*/

    /**
     * Gets the user type for the link
     *
     * @return userType
     * @see #CURRENT_USER
     * @see #ALL_USERS
     */
    public abstract int getUserType();

    /**
     * Determines if the shortcut target should be run with administrator privileges.
     *
     * @param runAsAdministrator if {@code true}, run the target with administrator privileges.
     */
    public void setRunAsAdministrator(boolean runAsAdministrator)
    {
    }

    /**
     * Determines if the shortcut target should be run with administrator privileges.
     *
     * @return {@code true}, if the target will run with administrator privileges
     */
    public boolean getRunAsAdministrator()
    {
        return false;
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Saves this link.
     *
     * @throws Exception if problems are encountered
     */
    public abstract void save() throws Exception;

    /*--------------------------------------------------------------------------*/

    /**
     * Gets the link hotKey
     *
     * @return int hotKey
     */
    public int getHotkey()
    {
        return 0;
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Sets the link hotKey
     *
     * @param hotkey
     */
    public void setHotkey(int hotkey)
    {
    }

    /**
     * Sets the Encoding
     *
     * @param string
     */
    public void setEncoding(String string)
    {
    }

    /**
     * This sets the Mimetype
     *
     * @param string
     */
    public void setMimetype(String string)
    {
    }

    /**
     * Sets the terminal
     *
     * @param string
     */
    public void setTerminal(String string)
    {
    }

    /**
     * This sets the terminals-options
     *
     * @param string
     */
    public void setTerminalOptions(String string)
    {
    }

    /**
     * This sets the shortcut type
     *
     * @param string
     */
    public void setType(String string)
    {
    }

    /**
     * This sets the KdeUserName
     *
     * @param string The UserName
     */
    public void setKdeUserName(String string)
    {
    }

    /**
     * This sets the setKdeSubstUID
     *
     * @param string exactly &quot;true&quot; or &quot;false&quot; or nothing
     */
    public void setKdeSubstUID(String string)
    {
    }

    /**
     * This sets the URL
     *
     * @param string
     */
    public void setURL(String string)
    {
    }

    /**
     * Gets the Programs Folder for the given User. This is where to create subfolders or to place
     * or create shortcuts.
     *
     * @param current_user one of current or all
     * @return The Foldername or null on unsupported platforms.
     */
    public abstract String getProgramsFolder(int current_user);

    /**
     * Sets the flag which indicates, that this should created for all.
     *
     * @param aCreateForAll A Flag - Set to true, if to create for All.
     */
    public void setCreateForAll(Boolean aCreateForAll)
    {
        this.createForAll = aCreateForAll;
    }

    /**
     * Gets the create for All Flag
     *
     * @return Returns True if this should be for all.
     */
    public Boolean getCreateForAll()
    {
        return createForAll;
    }

    /**
     * Sets the Categories Field On Unixes
     *
     * @param theCategories the categories
     */
    public void setCategories(String theCategories)
    {
    }

    /**
     * Sets the TryExecField on Unixes.
     *
     * @param aTryExec the try exec command
     */
    public void setTryExec(String aTryExec)
    {
    }

    /**
     * Sets the Uninstaller field with the unique Uninstaller Instance.
     *
     * @param theUninstaller the unique instance
     */
    public void setUninstaller(UninstallData theUninstaller)
    {
        uninstaller = theUninstaller;
    }

    /**
     * Dummy Method especially for the Unix Root User.
     */
    public void execPostAction()
    {
        // Debug.log("Call of unused execPostAction Method in " + this.getClass().getName() );
    }

    /**
     * Clean Up Method to do some cleanups after Shortcut Creation. <br>
     * currently unused.
     */
    public void cleanUp()
    {
        // Debug.log("Call of unused cleanUp Method in " + this.getClass().getName() );
    }

}
/*---------------------------------------------------------------------------*/
