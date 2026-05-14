/*
    Copyright (C) 2026 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
     along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class UpdateTranslations extends CleanupTranslations
{
    /**
     * @return e.g. strings|help_content
     */
    @Input
    public abstract Property<String> getBaseNames();

    @Override
    protected Document reviseDocument(String parent, String baseName, Document original)
    {
        if (!parent.contains("-")) {
            return original;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document revised = builder.newDocument();
            Node rootNode = revised.importNode(original.getDocumentElement(), true);    // true; deep copy
            revised.appendChild(rootNode);

            Document baseDocument = loadDocument(Path.of(getInputDir().get() + "/values/" + baseName + ".xml"));
            Set<String> baseStringNames = collectStringNames(baseDocument);

            // check for values that are not in the base document
            NodeList nodes = revised.getElementsByTagName("string");
            for (int i=0; i<nodes.getLength(); i++)
            {
                Element element = (Element) nodes.item(i);
                String name = element.getAttribute("name");
                if (!baseStringNames.contains(name)) {
                    getLogger().error("{} contains strings not present in default! {}", (parent + "/" + baseName + ".xml"), name);
                }
            }

            // copy missing values from base document
            Set<String> stringNames = collectStringNames(revised);
            nodes = baseDocument.getElementsByTagName("string");
            for (int i=0; i<nodes.getLength(); i++)
            {
                Element element = (Element) nodes.item(i);
                if (!isTranslatable(element) || isMarkedSkip(element) || isReference(element)) {
                    continue;
                }

                String name = element.getAttribute("name");
                if (!stringNames.contains(name))
                {
                    getLogger().warn("{} needs update! missing: {}", (parent + "/" + baseName + ".xml"), name);

                    Element missingString = revised.createElement("string");
                    missingString.setAttribute("name", name);
                    missingString.setTextContent(element.getTextContent());

                    rootNode.appendChild(revised.createTextNode("\n    "));
                    rootNode.appendChild(missingString);
                    rootNode.appendChild(revised.createTextNode("    "));
                    rootNode.appendChild(revised.createComment(" TODO "));
                }
            }

            return revised;

        } catch (ParserConfigurationException e) {
            getLogger().error("SyncTranslations", e);
        }
        return original;
    }

    private Element findElementWithName(NodeList nodes, String name)
    {
        for (int i=0; i<nodes.getLength(); i++)
        {
            Element element = (Element) nodes.item(i);
            String name0 = element.getAttribute("name");
            if (name.equals(name0)) {
                return element;
            }
        }
        return null;
    }

    private boolean isTranslatable(Element element)
    {
        if (element.hasAttribute("translatable")) {
            return Boolean.parseBoolean(element.getAttribute("translatable"));
        }
        return true;
    }

    private boolean isReference(Element element) {
        return element.getTextContent().startsWith("@string/");
    }

    private boolean isMarkedSkip(Element element)
    {
        if (element.hasAttribute("tools:exclude")) {
            return Boolean.parseBoolean(element.getAttribute("tools:exclude"));
        }
        return false;
    }

    private Set<String> collectStringNames(Document document)
    {
        Set<String> set = new TreeSet<>();
        NodeList nodes = document.getElementsByTagName("string");
        for (int i=0; i<nodes.getLength(); i++)
        {
            Element element = (Element) nodes.item(i);
            set.add(element.getAttribute("name"));
        }
        return set;
    }

}
