/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005 Klaus Bartz
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

import com.coi.tools.os.izpack.Registry;
import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.util.Librarian;

import java.util.List;
import java.util.Properties;

/**
 * This is the Microsoft Windows specific implementation of <code>RegistryHandler</code>.
 *
 * @author bartzkau
 */
public class Win_RegistryHandler extends RegistryHandler
{

    /**
     * The librarian.
     */
    private final Librarian librarian;

    /**
     * The registry. Lazily constructed, so instances of this can be created on other platforms for testing purposes.
     */
    private Registry registry;


    /**
     * Constructs a <tt>Win_RegistryHandler</tt>.
     *
     * @param librarian the librarian
     */
    public Win_RegistryHandler(Librarian librarian)
    {
        this.librarian = librarian;
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The return value is a String array which contains the names of
     * the keys and values which are created. REG_SZ is used as registry value type.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, String contents) throws NativeLibException
    {
        if (contents.contains("OLD_KEY_VALUE") && getRegistry().valueExist(key, value))
        {
            Object ob = getRegistry().getValueAsObject(key, value);
            if (ob instanceof String)
            {
                Properties props = new Properties();
                props.put("OLD_KEY_VALUE", ob);
                VariableSubstitutor variableSubstitutor = new VariableSubstitutorImpl(new DefaultVariables(props));
                try
                {
                    contents = variableSubstitutor.substitute(contents);
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        }
        getRegistry().setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The return value is a String array which contains the names of
     * the keys and values which are created. REG_MULTI_SZ is used as registry value type.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, String[] contents) throws NativeLibException
    {
        getRegistry().setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The return value is a String array which contains the names of
     * the keys and values which are created. REG_BINARY is used as registry value type.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, byte[] contents) throws NativeLibException
    {
        getRegistry().setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The return value is a String array which contains the names of
     * the keys and values which are created. REG_DWORD is used as registry value type.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, long contents) throws NativeLibException
    {
        getRegistry().setValue(key, value, contents);
    }

    /**
     * Returns the contents of the key/value pair if value exist, else the given default value.
     *
     * @param key        the registry key which should be used
     * @param value      the registry value from which the contents should be requested
     * @param defaultVal value to be used if no value exist in the registry
     * @return requested value if exist, else the default value
     * @throws NativeLibException
     */
    public RegDataContainer getValue(String key, String value, RegDataContainer defaultVal) throws NativeLibException
    {
        if (valueExist(key, value))
        {
            return (getValue(key, value));
        }
        return (defaultVal);
    }

    /**
     * Returns whether a key exist or not.
     *
     * @param key key to be evaluated
     * @return whether a key exist or not
     * @throws NativeLibException
     */
    public boolean keyExist(String key) throws NativeLibException
    {
        return (getRegistry().keyExist(key));
    }

    /**
     * Returns whether a the given value under the given key exist or not.
     *
     * @param key   key to be used as path for the value
     * @param value value name to be evaluated
     * @return whether a the given value under the given key exist or not
     * @throws NativeLibException
     */
    public boolean valueExist(String key, String value) throws NativeLibException
    {
        return (getRegistry().valueExist(key, value));
    }

    /**
     * Returns all keys which are defined under the given key.
     *
     * @param key key to be used as path for the sub keys
     * @return all keys which are defined under the given key
     * @throws NativeLibException
     */
    public String[] getSubkeys(String key) throws NativeLibException
    {
        return (getRegistry().getSubkeys(key));
    }

    /**
     * Returns all value names which are defined under the given key.
     *
     * @param key key to be used as path for the value names
     * @return all value names which are defined under the given key
     * @throws NativeLibException
     */
    public String[] getValueNames(String key) throws NativeLibException
    {
        return (getRegistry().getValueNames(key));
    }

    /**
     * Returns the contents of the key/value pair if value exist, else an exception is raised.
     *
     * @param key   the registry key which should be used
     * @param value the registry value from which the contents should be requested
     * @return requested value if exist, else an exception
     * @throws NativeLibException
     */
    public RegDataContainer getValue(String key, String value) throws NativeLibException
    {
        return (getRegistry().getValue(key, value));
    }

    /**
     * Creates the given key in the registry.
     *
     * @param key key to be created
     * @throws NativeLibException
     */
    public void createKey(String key) throws NativeLibException
    {
        getRegistry().createKey(key);
    }

    /**
     * Deletes the given key if exist, else throws an exception.
     *
     * @param key key to be deleted
     * @throws NativeLibException
     */
    public void deleteKey(String key) throws NativeLibException
    {
        getRegistry().deleteKey(key);
    }

    /**
     * Deletes a key under the current root if it is empty, else do nothing.
     *
     * @param key key to be deleted
     * @throws NativeLibException
     */
    public void deleteKeyIfEmpty(String key) throws NativeLibException
    {
        getRegistry().deleteKeyIfEmpty(key);
    }

    /**
     * Deletes a value.
     *
     * @param key   key of the value which should be deleted
     * @param value value name to be deleted
     * @throws com.izforge.izpack.api.exception.NativeLibException
     *
     */
    public void deleteValue(String key, String value) throws NativeLibException
    {
        getRegistry().deleteValue(key, value);
    }

    /**
     * Sets the root for the next registry access.
     *
     * @param i an integer which refers to a HKEY
     * @throws NativeLibException
     */
    public void setRoot(int i) throws NativeLibException
    {
        getRegistry().setRoot(i);
    }

    /**
     * Return the root as integer (HKEY_xxx).
     *
     * @return the root as integer
     * @throws NativeLibException
     */
    public int getRoot() throws NativeLibException
    {
        return (getRegistry().getRoot());
    }

    /**
     * Sets up whether or not previous contents of registry values will
     * be logged by the 'setValue()' method.  When registry values are
     * overwritten by repeated installations, the desired behavior can
     * be to have the registry value removed rather than rewound to the
     * last-set contents (acheived via 'false').  If this method is not
     * called then the flag wll default to 'true'.
     *
     * @param flagVal true to have the previous contents of registry
     *                values logged by the 'setValue()' method.
     */
    public void setLogPrevSetValueFlag(boolean flagVal) throws NativeLibException
    {
        getRegistry().setLogPrevSetValueFlag(flagVal);
    }

    /**
     * Determines whether or not previous contents of registry values
     * will be logged by the 'setValue()' method.
     *
     * @return true if the previous contents of registry values will be
     *         logged by the 'setValue()' method.
     */
    public boolean getLogPrevSetValueFlag() throws NativeLibException
    {
        return (getRegistry().getLogPrevSetValueFlag());
    }

    /**
     * Activates logging of registry changes.
     *
     * @throws NativeLibException
     */
    public void activateLogging() throws NativeLibException
    {
        getRegistry().activateLogging();
    }

    /**
     * Suspends logging of registry changes.
     *
     * @throws NativeLibException
     */
    public void suspendLogging() throws NativeLibException
    {
        getRegistry().suspendLogging();
    }

    /**
     * Resets logging of registry changes.
     *
     * @throws NativeLibException
     */
    public void resetLogging() throws NativeLibException
    {
        getRegistry().resetLogging();
    }

    public List<Object> getLoggingInfo() throws NativeLibException
    {
        return (getRegistry().getLoggingInfo());
    }

    public void setLoggingInfo(List info) throws NativeLibException
    {
        getRegistry().setLoggingInfo(info);
    }

    public void addLoggingInfo(List info) throws NativeLibException
    {
        getRegistry().addLoggingInfo(info);
    }

    public void rewind() throws NativeLibException
    {
        getRegistry().rewind();
    }

    /**
     * Returns the registry, creating it if necessary.
     *
     * @return the registry
     * @throws NativeLibException if the registry cannot be created
     */
    private synchronized Registry getRegistry() throws NativeLibException
    {
        if (registry == null)
        {
            try
            {
                registry = new Registry(librarian);
            }
            catch (Throwable exception)
            {
                throw new NativeLibException("Failed to create Registry: " + exception.getMessage(), exception);
            }
        }
        return registry;
    }

}
