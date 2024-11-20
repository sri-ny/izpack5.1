/*
 * IzPack - Copyright 2001-2017 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.licence;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.resource.ResourceManager;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.net.URL;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Michael Aichler
 */
public class LicenceLoaderTest {

    @Rule
    public final ExpectedException thrownException = ExpectedException.none();

    private URL defaultUrl;
    private URL specificUrl;
    private Resources resources;

    @Before
    public void setUp() throws Exception
    {
        defaultUrl = new URL("file://default");
        specificUrl = new URL("file://specific");
        resources = Mockito.mock(Resources.class);
    }

    @Test
    public void asUrlShouldThrowExceptionIfPanelNotFound() throws Exception
    {
        LicenceLoader loader = createFor(Object.class, null);

        thrownException.expect(ResourceException.class);
        thrownException.expectMessage(containsString("No IzPanel implementation found for"));

        loader.asURL();
    }

    @Test
    public void asUrlShouldThrowExceptionIfResourcesNotFound() throws Exception
    {
        when(resources.getURL("LicencePanel.licence")).thenThrow(new ResourceNotFoundException(""));
        when(resources.getURL("LicencePanel.panelId")).thenThrow(new ResourceNotFoundException(""));

        Panel panel = createPanel("panelId");
        LicenceLoader loader = createFor(LicencePanel.class, panel);

        thrownException.expect(ResourceException.class);
        thrownException.expectMessage(endsWith(" resources (LicencePanel.licence, LicencePanel.panelId)" +
                " for panel type 'LicencePanel'"));


        loader.asURL();
    }

    @Test
    public void asUrlShouldUseIzPanelClassAsPrefix() throws Exception
    {
        when(resources.getURL("LicencePanel.licence")).thenReturn(defaultUrl);

        Panel panel = createPanel(null);
        URL url = createFor(LicenceConsolePanel.class, panel).asURL();

        assertThat(url, equalTo(defaultUrl));
    }

    @Test
    public void asUrlShouldPreferSpecificResource() throws Exception
    {
        when(resources.getURL("LicencePanel.somePanelId")).thenReturn(specificUrl);

        Panel panel = createPanel("somePanelId");
        URL result = createFor(LicencePanel.class, panel).asURL();

        assertThat(result, equalTo(specificUrl));
    }

    @Test
    public void asUrlShouldFallbackToDefaultResource() throws Exception
    {
        when(resources.getURL("LicencePanel.somePanelId")).thenThrow(new ResourceNotFoundException(""));
        when(resources.getURL("LicencePanel.licence")).thenReturn(defaultUrl);

        Panel panel = createPanel("somePanelId");
        URL result = createFor(LicencePanel.class, panel).asURL();

        assertThat(result, equalTo(defaultUrl));
    }

    @Test
    public void asStringShouldLoadResource() throws Exception
    {
        ResourceManager rm = new ResourceManager();
        rm.setResourceBasePath("/com/izforge/izpack/panels/licence/");

        Panel panel = createPanel(null);
        LicenceLoader loader = new LicenceLoader(LicenceConsolePanel.class, panel, rm);

        String result = loader.asString();
        assertThat(result, equalTo("This is a licence panel"));
    }

    @Test
    public void buildSpecificResourceNameShouldReturnNullIfPanelIsNull()
    {
        String result = LicenceLoader.buildSpecificResourceName("SomePrefix", null);
        assertThat(result, nullValue());
    }

    @Test
    public void buildSpecificResourceNameShouldReturnNullIfPanelIdIsNull()
    {
        Panel panel = createPanel(null);

        String result = LicenceLoader.buildSpecificResourceName("SomePrefix", panel);
        assertThat(result, nullValue());
    }

    @Test
    public void buildSpecificResourceNameShouldReturnPanelIdAsSuffix()
    {
        Panel panel = createPanel("somePanelId");

        String result = LicenceLoader.buildSpecificResourceName("SomePrefix", panel);
        assertThat(result, equalTo("SomePrefix.somePanelId"));
    }

    @Test
    public void buildFinalErrorMessageWithMultipleResourceNames()
    {
        String result = LicenceLoader.buildFinalErrorMessage("LicencePanel", "foo", "bar");
        assertThat(result, endsWith(" resources (foo, bar) for panel type 'LicencePanel'"));
    }

    @Test
    public void buildFinalErrorMessageWithNullResourceName()
    {
        String result = LicenceLoader.buildFinalErrorMessage("LicencePanel", "foo", null);
        assertThat(result, endsWith(" resources (foo) for panel type 'LicencePanel'"));
    }

    @Test
    public void buildFinalErrorMessageWithSingleResourceName()
    {
        String result = LicenceLoader.buildFinalErrorMessage("LicencePanel", "foo");
        assertThat(result, endsWith(" resources (foo) for panel type 'LicencePanel'"));
    }

    @Test
    public void findTargetClassWithConsolePanelClass()
    {
        Class<?> result = LicenceLoader.findTargetClass(LicenceConsolePanel.class);
        assertThat(result, Matchers.<Class<?>>equalTo(LicencePanel.class));
    }

    @Test
    public void findTargetClassWithIzPanelClass()
    {
        Class<?> result = LicenceLoader.findTargetClass(LicencePanel.class);
        assertThat(result, Matchers.<Class<?>>equalTo(LicencePanel.class));
    }

    /**
     * Helper which creates a licence loader for the given {@code clazz} and a
     * mocked instance of {@link Resources}.
     *
     * @param clazz The panel class for which the licence loader is to be created.
     * @return A newly created licence loader.
     */
    private LicenceLoader createFor(Class<?> clazz, Panel panel)
    {
        return new LicenceLoader(clazz, panel, resources);
    }

    private Panel createPanel(String id)
    {
        Panel panel = new Panel();
        panel.setPanelId(id);
        return panel;
    }
}