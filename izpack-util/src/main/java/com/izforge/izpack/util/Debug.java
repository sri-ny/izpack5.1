/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

package com.izforge.izpack.util;


/**
 * Static debug mode flags available during installation
 */
public class Debug
{
    /**
     * initial TRACE flag
     */
    private static boolean TRACE;

    /**
     * initial STACKTRACE flag
     */
    private static boolean STACKTRACE;

    /**
     * initial DEBUG flag
     */
    private static boolean DEBUG;

    static
    {
        STACKTRACE = Boolean.getBoolean("STACKTRACE");
        TRACE = Boolean.getBoolean("TRACE");
        DEBUG = Boolean.getBoolean("DEBUG");
    }

    /**
     * Sets whether to trace variables and conditions at runtime interactively.
     * @param TRACE whether variables and conditions are to be traced at runtime interactively
     */
    public static void setTRACE(boolean TRACE)
    {
        Debug.TRACE = TRACE;
    }

    /**
     * Gets whether to trace variables and conditions at runtime interactively.
     * @return whether to trace variables and conditions at runtime interactively
     */
    public static boolean isTRACE()
    {
        return TRACE;
    }

    /**
     * Sets the current TRACE flag.
     * @param STACKTRACE desired state
     */
    public static void setSTACKTRACE(boolean STACKTRACE)
    {
        Debug.STACKTRACE = STACKTRACE;
    }

    /**
     * Set whether to show stacktraces at runtime.
     * @return whether to show stacktraces at runtime
     */
    public static boolean isSTACKTRACE()
    {
        return STACKTRACE;
    }

    /**
     * Sets whether to run in debug mode.
     * @param DEBUG whether the installer is run in debug mode
     */
    public static void setDEBUG(boolean DEBUG)
    {
        Debug.DEBUG = DEBUG;
    }

    /**
     * Gets whether the installer run in debug mode.
     * @return whether the installer run in debug mode.
     */
    public static boolean isDEBUG()
    {
        return DEBUG;
    }
}
