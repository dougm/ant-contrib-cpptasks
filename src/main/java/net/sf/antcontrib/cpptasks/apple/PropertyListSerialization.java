/*
 * Licensed to the Ant-Contrib Project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Ant-Contrib licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.antcontrib.cpptasks.apple;

import net.sf.antcontrib.cpptasks.ide.CommentDef;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Static class that provides methods to serialize
 * a Map to a Cocoa XML Property List.  Does not currently support
 * date or data elements.
 */
public final class PropertyListSerialization {
    /**
     * Private constructor.
     */
    private PropertyListSerialization() {

    }

    /**
     * Serializes a property list into a Cocoa XML Property List document.
     *
     * @param propertyList property list.
     * @param file         destination.
     * @param comments     comments to insert into document.
     * @throws IOException                       if exception during I/O.
     * @throws SAXException                      if exception during serialization.
     * @throws TransformerConfigurationException if exception creating serializer.
     */
    public static void serialize(final Map<String, Object> propertyList,
                                 final List<CommentDef> comments, final File file)
            throws IOException, SAXException, TransformerConfigurationException {
        SAXTransformerFactory sf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        TransformerHandler handler = sf.newTransformerHandler();

        FileOutputStream os = new FileOutputStream(file);
        StreamResult result = new StreamResult(os);
        handler.setResult(result);

        handler.startDocument();
        for (CommentDef object : comments) {
            char[] comment = String.valueOf(object).toCharArray();
            handler.comment(comment, 0, comment.length);
        }
        AttributesImpl attributes = new AttributesImpl();
        handler.startElement(null, "plist", "plist", attributes);
        serializeMap(propertyList, handler);
        handler.endElement(null, "plist", "plist");

        handler.endDocument();
    }

    /**
     * Serialize a map as a dict element.
     *
     * @param map     map to serialize.
     * @param handler destination of serialization events.
     * @throws SAXException if exception during serialization.
     */
    private static void serializeMap(final Map<String, Object> map,
                                     final ContentHandler handler) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        handler.startElement(null, "dict", "dict", attributes);

        if (map.size() > 0) {
            //
            //   need to output with sorted keys to maintain
            //     reproducibility
            //
            String[] keys = map.keySet().toArray(new String[0]);
            Arrays.sort(keys);
            for (String key : keys) {
                handler.startElement(null, "key", "key", attributes);
                handler.characters(key.toCharArray(), 0, key.length());
                handler.endElement(null, "key", "key");
                serializeObject(map.get(key), handler);
            }
        }
        handler.endElement(null, "dict", "dict");
    }

    /**
     * Serialize a list as an array element.
     *
     * @param list    list to serialize.
     * @param handler destination of serialization events.
     * @throws SAXException if exception during serialization.
     */
    private static void serializeList(final List<Object> list,
                                      final ContentHandler handler) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        handler.startElement(null, "array", "array", attributes);
        for (Object object : list) {
            serializeObject(object, handler);
        }
        handler.endElement(null, "array", "array");
    }

    /**
     * Creates an element with the specified tag name and character content.
     *
     * @param tag     tag name.
     * @param content character content.
     * @param handler destination of serialization events.
     * @throws SAXException if exception during serialization.
     */
    private static void serializeElement(final String tag,
                                         final String content,
                                         final ContentHandler handler) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        handler.startElement(null, tag, tag, attributes);
        handler.characters(content.toCharArray(), 0, content.length());
        handler.endElement(null, tag, tag);
    }


    /**
     * Serialize a Number as an integer element.
     *
     * @param integer number to serialize.
     * @param handler destination of serialization events.
     * @throws SAXException if exception during serialization.
     */
    private static void serializeInteger(final Number integer,
                                         final ContentHandler handler) throws SAXException {
        serializeElement("integer", String.valueOf(integer.longValue()), handler);
    }

    /**
     * Serialize a Number as a real element.
     *
     * @param real    number to serialize.
     * @param handler destination of serialization events.
     * @throws SAXException if exception during serialization.
     */
    private static void serializeReal(final Number real,
                                      final ContentHandler handler) throws SAXException {
        serializeElement("real", String.valueOf(real.doubleValue()), handler);
    }

    /**
     * Serialize a Boolean as a true or false element.
     *
     * @param val     boolean to serialize.
     * @param handler destination of serialization events.
     * @throws SAXException if exception during serialization.
     */
    private static void serializeBoolean(final Boolean val,
                                         final ContentHandler handler) throws SAXException {
        String tag = "false";
        if (val.booleanValue()) {
            tag = "true";
        }
        AttributesImpl attributes = new AttributesImpl();
        handler.startElement(null, tag, tag, attributes);
        handler.endElement(null, tag, tag);
    }

    /**
     * Serialize a string as a string element.
     *
     * @param val     string to serialize.
     * @param handler destination of serialization events.
     * @throws SAXException if exception during serialization.
     */
    private static void serializeString(final String val,
                                        final ContentHandler handler) throws SAXException {
        serializeElement("string", val, handler);
    }


    /**
     * Serialize an object using the best available element.
     *
     * @param obj     object to serialize.
     * @param handler destination of serialization events.
     * @throws SAXException if exception during serialization.
     */
    private static void serializeObject(final Object obj,
                                        final ContentHandler handler) throws SAXException {
        if (obj instanceof Map) {
            serializeMap((Map<String, Object>) obj, handler);
        } else if (obj instanceof List) {
            serializeList((List<Object>) obj, handler);
        } else if (obj instanceof Number) {
            if (obj instanceof Double || obj instanceof Float) {
                serializeReal((Number) obj, handler);
            } else {
                serializeInteger((Number) obj, handler);
            }
        } else if (obj instanceof Boolean) {
            serializeBoolean((Boolean) obj, handler);
        } else {
            serializeString(String.valueOf(obj), handler);
        }
    }
}
