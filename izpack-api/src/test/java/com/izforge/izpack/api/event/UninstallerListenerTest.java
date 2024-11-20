/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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
package com.izforge.izpack.api.event;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class UninstallerListenerTest {

    @Test
    public void beforeDeleteWithProgressListenerShouldForwardCallToBeforeDelete() {

        final List<File> files = Collections.emptyList();
        final ProgressListener progressListener = Mockito.mock(ProgressListener.class);

        RecordingUninstallerListener listener = new RecordingUninstallerListener();
        listener.beforeDelete(files, progressListener);

        assertThat(listener.beforeDeleteMethodCalled, equalTo(true));
    }

    /*
     * Records whether #beforeDelete(List<File>) was called.
     */
    public static class RecordingUninstallerListener extends AbstractUninstallerListener {

        boolean beforeDeleteMethodCalled = false;

        @Override
        public void beforeDelete(List<File> files) {
            beforeDeleteMethodCalled = true;
        }
    }
}