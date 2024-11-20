/*
 * IzPack - Copyright 2001-2015 Julien Ponge, René Krell, All Rights Reserved.
 *
 * http://izpack.org/
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
package com.izforge.izpack.api.adaptator.impl;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.XMLException;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class XMLParser implements IXMLParser
{
    public class ByteBufferInputStream extends InputStream
    {
        private final ByteBuffer buf;

        public ByteBufferInputStream(ByteBuffer buf)
        {
            this.buf = buf;
        }

        @Override
        public synchronized int read() throws IOException
        {
            if (!buf.hasRemaining())
            {
                return -1;
            }
            int c = buf.get() & 0xff;
            return c;
        }

        @Override
        public synchronized int read(byte[] bytes, int off, int len) throws IOException
        {
            if (!buf.hasRemaining())
            {
                return -1;
            }
            len = Math.min(len, buf.remaining());
            buf.get(bytes, off, len);
            return len;
        }

        @Override
        public int available() throws IOException
        {
            return buf.remaining();
        }
    }

    private LineNumberFilter filter;
    private String parsedItem = null;


    public XMLParser()
    {
        this(true);
    }

    public XMLParser(boolean validating)
    {
        this(validating, null);
    }

    public XMLParser(boolean validating, StreamSource[] schemaSources)
    {
        try
        {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setValidating(false); // we don't use DTD
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setXIncludeAware(true);

            if (validating && (schemaSources != null && schemaSources.length > 0))
            {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                saxParserFactory.setSchema(schemaFactory.newSchema(schemaSources));
            }

            saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
            saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);

            SAXParser parser = saxParserFactory.newSAXParser();
            if (validating && (schemaSources == null || schemaSources.length == 0))
            {
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                        XMLConstants.W3C_XML_SCHEMA_NS_URI);
            }

            XMLReader xmlReader = parser.getXMLReader();
            filter = new LineNumberFilter(xmlReader);
        }
        catch (ParserConfigurationException e)
        {
            throw new XMLException(e);
        }
        catch (SAXException e)
        {
            throw new XMLException(e);
        }
    }

    private IXMLElement searchFirstElement(DOMResult domResult)
    {
        for (Node child = domResult.getNode().getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                return new XMLElementImpl(child);
            }
        }
        return null;
    }

    private DOMResult parseLineNrFromInputSource(InputSource inputSource)
    {
        DOMResult result = null;
        try
        {
            result = new DOMResult();
            SAXSource source = new SAXSource(inputSource);
            source.setXMLReader(filter);

            TransformerFactory.newInstance().newTransformer().transform(source, result);

            filter.applyLN(result);
        }
        catch (TransformerException e)
        {
            String extraInfos = null;
            if (this.parsedItem != null)
            {
                extraInfos = " in " + parsedItem;
            }
            // we try to get the location of the error.
            // can't use an ErrorHander here !
            if (e.getLocator() == null && filter.getDocumentLocator() != null)
            {
                Locator locator = filter.getDocumentLocator();
                extraInfos += " at line " + locator.getLineNumber() + ", column " + locator.getColumnNumber();
            }
            if (extraInfos != null)
            {
                throw new XMLException("Error" + extraInfos + " : " + e.getMessage(), e);
            }
            throw new XMLException(e);
        }
        finally
        {
            this.parsedItem = null;
        }
        return result;
    }

    public IXMLElement parse(InputStream inputStream)
    {
        checkNotNullStream(inputStream);

        this.parsedItem = null;
        InputSource inputSource = new InputSource(inputStream);
        DOMResult result = parseLineNrFromInputSource(inputSource);
        return searchFirstElement(result);
    }

    public IXMLElement parse(InputStream inputStream, String systemId)
    {
        checkNotNullStream(inputStream);

        this.parsedItem = systemId;
        InputSource inputSource = new InputSource(inputStream);
        inputSource.setSystemId(systemId);
        DOMResult result = parseLineNrFromInputSource(inputSource);
        return searchFirstElement(result);
    }

    public IXMLElement parse(String inputString)
    {
        this.parsedItem = null;

        final ByteBuffer buf = Charset.forName("UTF-8").encode(inputString);

        return parse(new ByteBufferInputStream(buf));
    }

    public IXMLElement parse(URL inputURL)
    {
        this.parsedItem = inputURL.toString();
        InputSource inputSource = new InputSource(inputURL.toExternalForm());
        DOMResult domResult = parseLineNrFromInputSource(inputSource);
        return searchFirstElement(domResult);
    }

    private void checkNotNullStream(InputStream inputStream)
    {
        if (inputStream == null)
        {
            throw new NullPointerException("The input stream must be not null.");
        }
    }
}
