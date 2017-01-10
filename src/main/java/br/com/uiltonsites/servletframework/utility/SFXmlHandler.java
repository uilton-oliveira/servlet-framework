/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.uiltonsites.servletframework.utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class to navigate over XML String / file
 *
 * @author Uilton Oliveira - uilton.dev@gmail.com
 */
public class SFXmlHandler {
    private final String xml;
    private boolean loadDtd = true;

    public SFXmlHandler(String xml) {
        this.xml = xml;
    }    
    
    public SFXmlHandler(String xml, boolean loadDtd) {
        this.xml = xml;
        this.loadDtd = loadDtd;
    }    
    
    public SFXmlHandler(Path xmlFile) throws IOException {
        this.xml = readFileToString(xmlFile, "UTF-8");
    }
    
    public SFXmlHandler(Path xmlFile, boolean loadDtd) throws IOException {
        this.xml = readFileToString(xmlFile, "UTF-8");
        this.loadDtd = loadDtd;
    }
    
    private String readFileToString(Path filePath, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(filePath);
        return new String(encoded, encoding);
    }
    
    private NodeList xPathBuilder(String xpathExp) {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        XPathFactory xPathFactory;
        InputStream stream;
        XPath xpath;
        XPathExpression expr;
        NodeList result;

        try {
            factory.setNamespaceAware(true);
            if (!loadDtd) {
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            }
            
            builder = factory.newDocumentBuilder();
            stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            doc = builder.parse(stream);
            
            xPathFactory = XPathFactory.newInstance();
            xpath = xPathFactory.newXPath();
            
            expr = xpath.compile(xpathExp);
            
            result =  (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            
            return result;

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e ) {
             // intentionally empty
        }
        
        return null;
    }    
        
    /**
     * Get value from path.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param returnClass Result will return casted to this class.
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param path Path to search in xml
     * @return Result Object
     */
    public <T> T getCustom(Class<T> returnClass, int pos, String path) {
        String tmp;
        Node node = xPathBuilder(path).item(pos);
        if (node == null) {
            return null;
        } else {
            tmp = node.getTextContent();
        }
        
        if (returnClass.isAssignableFrom(String.class)) {
            return returnClass.cast(String.valueOf(tmp));
        } else if (returnClass.isAssignableFrom(Integer.class)) {
            return returnClass.cast(Integer.valueOf(String.valueOf(tmp)));
        } else if (returnClass.isAssignableFrom(Double.class)) {
            return returnClass.cast(Double.valueOf(String.valueOf(tmp)));
        } else if (returnClass.isAssignableFrom(Float.class)) {
            return returnClass.cast(Float.valueOf(String.valueOf(tmp)));
        }  else if (returnClass.isAssignableFrom(Boolean.class)) {
            return returnClass.cast(Boolean.valueOf(String.valueOf(tmp)));
        } else {
            return returnClass.cast(tmp);
        }
    }
    
    private String getAttributeFromName(NamedNodeMap namedNode, String name) {
        for (int i = 0; i < namedNode.getLength(); i++) {
            Node node = namedNode.item(i);
            if (node.getLocalName().equalsIgnoreCase(name)) {
                return node.getTextContent();
            }
            
        }
        return null;
    }
    
    /**
     * Get attribute value from path.
     * Usage Example: getCustomAttribute(String.class, 0, "date", "Response/Info");
     *
     * @param returnClass Result will return casted to this class.
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public <T> T getCustomAttribute(Class<T> returnClass, int pos, String attribute, String path) {
        String tmp;
        Node node = xPathBuilder(path).item(pos);
        if (node == null) {
            return null;
        } else {
            tmp = getAttributeFromName(node.getAttributes(), attribute);
        }
        if (tmp == null) {
            return null;
        }
        
        if (returnClass.isAssignableFrom(String.class)) {
            return returnClass.cast(String.valueOf(tmp));
        } else if (returnClass.isAssignableFrom(Integer.class)) {
            return returnClass.cast(Integer.valueOf(String.valueOf(tmp)));
        } else if (returnClass.isAssignableFrom(Double.class)) {
            return returnClass.cast(Double.valueOf(String.valueOf(tmp)));
        } else if (returnClass.isAssignableFrom(Float.class)) {
            return returnClass.cast(Float.valueOf(String.valueOf(tmp)));
        }  else if (returnClass.isAssignableFrom(Boolean.class)) {
            return returnClass.cast(Boolean.valueOf(String.valueOf(tmp)));
        } else {
            return returnClass.cast(tmp);
        }
    }
    
    /**
     * Get attribute value from path.
     * Usage Example: getCustomAttribute(String.class, "date", "Response/Info");
     *
     * @param returnClass Result will return casted to this class.
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public <T> T getCustomAttribute(Class<T> returnClass, String attribute, String path) {
        return getCustomAttribute(returnClass, 0, attribute, path);
    }
    
    /**
     * This method will navigate into multidimensional xml at pos 0.
     *
     * @param returnClass Result will return casted to this class.
     * @param path Keys to search in Map
     * @return Result Object
     */
    public <T> T getCustom(Class<T> returnClass, String path) {
        return getCustom(returnClass, 0, path);
    }
    
    /**
     * Get the size of how many elements is at the same path
     * @param path
     * @return size
     */
    public int getSize(String path) {
        NodeList node = xPathBuilder(path);
        return node.getLength();
    }
    
    
    /**
     * Get attribute value from path as String.
     * Usage Example: getCustomAttribute(String.class, "date", "Response/Info");
     *
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public String getAttributeString(String attribute, String path) {
        return getCustomAttribute(String.class, attribute, path);
    }
    
    /**
     * Get attribute value from path as String.
     * Usage Example: getCustomAttribute(String.class, 0, "date", "Response/Info");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public String getAttributeString(int pos, String attribute, String path) {
        return getCustomAttribute(String.class, pos, attribute, path);
    }
    
    /**
     * Get attribute value from path as Integer.
     * Usage Example: getCustomAttribute(String.class, "date", "Response/Info");
     *
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Integer getAttributeInt(String attribute, String path) {
        return getCustomAttribute(Integer.class, attribute, path);
    }
    
    /**
     * Get attribute value from path as Integer.
     * Usage Example: getCustomAttribute(String.class, 0, "date", "Response/Info");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Integer getAttributeInt(int pos, String attribute, String path) {
        return getCustomAttribute(Integer.class, pos, attribute, path);
    }
    
    /**
     * Get attribute value from path as Boolean.
     * Usage Example: getCustomAttribute(String.class, "date", "Response/Info");
     *
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Boolean getAttributeBoolean(String attribute, String path) {
        return getCustomAttribute(Boolean.class, attribute, path);
    }
    
    /**
     * Get attribute value from path as Boolean.
     * Usage Example: getCustomAttribute(String.class, 0, "date", "Response/Info");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Boolean getAttributeBoolean(int pos, String attribute, String path) {
        return getCustomAttribute(Boolean.class, pos, attribute, path);
    }
    
    /**
     * Get attribute value from path as Double.
     * Usage Example: getCustomAttribute(String.class, "date", "Response/Info");
     *
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Double getAttributeDouble(String attribute, String path) {
        return getCustomAttribute(Double.class, attribute, path);
    }
    
    /**
     * Get attribute value from path as Double.
     * Usage Example: getCustomAttribute(String.class, 0, "date", "Response/Info");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Double getAttributeDouble(int pos, String attribute, String path) {
        return getCustomAttribute(Double.class, pos, attribute, path);
    }
    
    /**
     * Get attribute value from path as Float.
     * Usage Example: getCustomAttribute(String.class, "date", "Response/Info");
     *
     * @param attribute Attribute to search for.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Float getAttributeFloat(String attribute, String path) {
        return getCustomAttribute(Float.class, attribute, path);
    }
    
    /**
     * Get attribute value from path as Float.
     * Usage Example: getCustomAttribute(String.class, 0, "date", "Response/Info");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param attribute Attribute to search for
     * @param path Path to search in xml
     * @return Result Object
     */
    public Float getAttributeFloat(int pos, String attribute, String path) {
        return getCustomAttribute(Float.class, pos, attribute, path);
    }
    
    
    /**
     * Get value from path as String.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param path Path to search in xml
     * @return Result Object
     */
    public String getString(String path) {
        return getCustom(String.class, path);
    }
    
    /**
     * Get value from path as String.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param path Path to search in xml
     * @return Result Object
     */
    public String getString(int pos, String path) {
        return getCustom(String.class, pos, path);
    }
    
    /**
     * Get value from path as Integer.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param path Path to search in xml
     * @return Result Object
     */
    public int getInt(String path) {
        return getCustom(Integer.class, path);
    }
    
    /**
     * Get value from path as Integer.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param path Path to search in xml
     * @return Result Object
     */
    public int getInt(int pos, String path) {
        return getCustom(Integer.class, pos, path);
    }
    
    /**
     * Get value from path as Boolean.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param path Path to search in xml
     * @return Result Object
     */
    public Boolean getBoolean(String path) {
        return getCustom(Boolean.class, path);
    }
    
    /**
     * Get value from path as Boolean.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Boolean getBoolean(int pos, String path) {
        return getCustom(Boolean.class, pos, path);
    }
    
    /**
     * Get value from path as Double.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param path Path to search in xml
     * @return Result Object
     */
    public Double getDouble(String path) {
        return getCustom(Double.class, path);
    }
    
    /**
     * Get value from path as Double.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Double getDouble(int pos, String path) {
        return getCustom(Double.class, pos, path);
    }
    
    /**
     * Get value from path as Float.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param path Path to search in xml
     * @return Result Object
     */
    public Float getFloat(String path) {
        return getCustom(Float.class, path);
    }
    
    /**
     * Get value from path as Float.
     * Usage Example: getCustom(String.class, 0, "Response/Info/Msisdn");
     *
     * @param pos Position to be used in case of more than one result, 0 = first.
     * @param path Path to search in xml
     * @return Result Object
     */
    public Float getFloat(int pos, String path) {
        return getCustom(Float.class, pos, path);
    }
    

    /**
     * @return the loadDtd
     */
    public boolean isLoadDtd() {
        return loadDtd;
    }

    /**
     * Set this to true to do not try load the dtd.
     * @param loadDtd the loadDtd to set
     */
    public void setLoadDtd(boolean loadDtd) {
        this.loadDtd = loadDtd;
    }

    @Override
    public String toString() {
        return xml;
    }
    
    
    
    
}
