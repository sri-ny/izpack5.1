/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010, 2012 Rene Krell
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

package com.izforge.izpack.api.data;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;

import java.util.Set;

public interface Value
{
    void validate() throws Exception;

    String resolve() throws Exception;

    String resolve(VariableSubstitutor... substitutors) throws Exception;

    Set<String> getVarRefs();

    InstallData getInstallData();

    void setInstallData(InstallData installData);
}
