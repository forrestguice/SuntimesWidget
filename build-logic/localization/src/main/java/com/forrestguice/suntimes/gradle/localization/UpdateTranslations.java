package com.forrestguice.suntimes.gradle.localization;/*
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
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Copies missing values from default into each translation; reports warnings / number of todos.
 */
public abstract class UpdateTranslations extends ProcessTranslations
{
    /**
     * @return e.g. en-rUS|en-rCA|...
     */
    @Input
    public abstract Property<String> getExcludeLocales();

    /**
     * @return e.g. strings|help_content
     */
    @Input
    public abstract Property<String> getBaseNames();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);

    @Override
    protected Document reviseDocument(String parent, String baseName, Document original)
    {
        if (!parent.contains("-")) {
            return original;
        }

        ArrayList<String> excluded = new ArrayList<>(List.of(getExcludeLocales().get().split("\\|")));
        for (String locale : excluded) {
            if (parent.endsWith(locale)) {
                return original;
            }
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document revised = builder.newDocument();
            Node node = revised.importNode(original.getDocumentElement(), true);    // true; deep copy
            revised.appendChild(node);

            Document baseDocument = loadDocument(Path.of(getInputDir().get() + "/values/" + baseName + ".xml"));
            checkForIllegalItems(parent, baseName, baseDocument, revised);

            // copy missing string values
            Map<String, Node> missingStringNodes = findMissing(baseDocument, revised, "string");
            List<String> missingStrings = getSortedKeySet(missingStringNodes);

            boolean first = true;
            for (String name : missingStrings)
            {
                Element element = (Element) missingStringNodes.get(name);
                if (first) {
                    appendFirstComment(node, revised);
                    first = false;
                }
                node.appendChild(revised.createTextNode("    "));

                String reportTag = (parent + "/" + baseName + ".xml");
                getLogger().warn("{} needs update! missing string: {}", reportTag, name);
                appendLineToReport("actions", MessageFormat.format("{0} :: added string {1}", reportTag, name));

                Element missingString = revised.createElement("string");
                missingString.setAttribute("name", name);
                missingString.setTextContent(element.getTextContent());
                node.appendChild(missingString);

                String missingComment = getCommentAfter(element);
                if (missingComment != null && !missingComment.trim().equals("TODO")) {
                    node.appendChild(revised.createTextNode("    "));
                    node.appendChild(revised.createComment(missingComment));
                }
                node.appendChild(revised.createTextNode("    "));
                node.appendChild(revised.createComment(" TODO "));
                node.appendChild(revised.createTextNode("\n"));
            }

            // copy missing string-arrays from base document
            Map<String, Node> missingStringArrayNodes = findMissing(baseDocument, revised, "string-array");
            List<String> missingStringArrays = getSortedKeySet(missingStringArrayNodes);

            if (!missingStringArrays.isEmpty()) {
                node.appendChild(revised.createTextNode("\n"));
            }
            for (String name : missingStringArrays)
            {
                Element element = (Element) missingStringArrayNodes.get(name);

                Element missingStringArray = revised.createElement("string-array");
                missingStringArray.setAttribute("name", name);
                missingStringArray.appendChild(revised.createTextNode("\n        "));

                if (!containsOnlyItemReferences(element))
                {
                    if (first) {
                        appendFirstComment(node, revised);
                        first = false;
                    }

                    String reportTag = (parent + "/" + baseName + ".xml");
                    getLogger().warn("{} needs update! missing string-array: {}", reportTag, name);
                    appendLineToReport("actions", MessageFormat.format("{0} :: added string-array {1}", reportTag, name));

                    node.appendChild(revised.createTextNode("    "));

                    NodeList children = element.getElementsByTagName("item");
                    for (int j=0; j<children.getLength(); j++)
                    {
                        Node child = (Element) children.item(j);
                        Element item = revised.createElement("item");
                        item.appendChild(revised.createTextNode("\n        "));
                        item.setTextContent(child.getTextContent());
                        missingStringArray.appendChild(item);
                        missingStringArray.appendChild(revised.createTextNode("\n    "));
                        if (j != children.getLength() - 1) {
                            missingStringArray.appendChild(revised.createTextNode("    "));
                        }
                    }
                    node.appendChild(missingStringArray);

                    String missingComment = getCommentAfter(element);
                    if (missingComment != null && !missingComment.trim().equals("TODO")) {
                        node.appendChild(revised.createTextNode("    "));
                        node.appendChild(revised.createComment(missingComment));

                    }
                    node.appendChild(revised.createTextNode("    "));
                    node.appendChild(revised.createComment(" TODO "));
                    node.appendChild(revised.createTextNode("\n\n"));
                }
            }

            // report number of todos
            NodeList nodes = node.getChildNodes();
            int todoCount = 0;
            for (int i=0; i<nodes.getLength(); i++)
            {
                Node n = nodes.item(i);
                if (n.getNodeType() == Node.COMMENT_NODE) {
                    if (n.getTextContent().contains("TODO")) {
                        todoCount++;
                    }
                }
            }

            if (todoCount > 0) {
                appendLineToReport("todo", String.format("%1$s:\t%2$s", (parent + "/" + baseName + ".xml"), todoCount));
            }
            appendLineToReport("actions");

            node.normalize();
            return revised;

        } catch (ParserConfigurationException e) {
            getLogger().error("SyncTranslations", e);
        }
        return original;
    }

    protected Map<String, Node> findMissing(Document baseDocument, Document other, String tag)
    {
        Set<String> names = collectNames(other, tag);
        Map<String, Node> missingNodes = new HashMap<>();
        NodeList nodes = baseDocument.getElementsByTagName(tag);
        for (int i=0; i<nodes.getLength(); i++)
        {
            Element element = (Element) nodes.item(i);
            if (isTranslatable(element) && !isMarkedCommon(element) && !isReference(element))
            {
                String name = element.getAttribute("name");
                if (!names.contains(name)) {
                    missingNodes.put(name, element);
                }
            }
        }
        return missingNodes;
    }

    protected List<String> getSortedKeySet(Map<String, Node> map) {
        ArrayList<String> arrayList = new ArrayList<>(map.keySet());
        Collections.sort(arrayList);
        return arrayList;
    }

    public static final String COMMENT_TAG = ":: Update ::";
    protected void appendFirstComment(Node node, Document document)
    {
        node.appendChild(document.createTextNode("\n    "));
        node.appendChild(document.createComment("\n        " + COMMENT_TAG +
                                                   "\n        Copied from default on " + dateFormat.format(new Date(System.currentTimeMillis())) +
                                                   " \n    "));
        node.appendChild(document.createTextNode("\n"));
    }

    protected void checkForIllegalItems(String parent, String baseName, Document baseDocument, Document revised)
    {
        String reportTag = (parent + "/" + baseName + ".xml");
        Set<String> baseStringNames = collectNames(baseDocument, "string");
        Set<String> translatableFalse = collectNames(baseDocument, "string", false);
        NodeList nodes0 = revised.getElementsByTagName("string");
        for (int i=0; i<nodes0.getLength(); i++)
        {
            Element element = (Element) nodes0.item(i);
            String name = element.getAttribute("name");
            if (!baseStringNames.contains(name))
            {
                String message = MessageFormat.format("{0} contains string not present in default! {1}", reportTag, name);
                getLogger().error(message);
                appendLineToReport("warnings", message);
            }
            if (translatableFalse.contains(name)) {
                String message = MessageFormat.format("{0} contains string marked translatable=false in default! {1}", reportTag, name);
                getLogger().error(message);
                appendLineToReport("warnings", message);
            }
            if (element.hasAttribute("translatable"))
            {
                boolean isTranslatable = Boolean.parseBoolean(element.getAttribute("translatable"));
                if (!isTranslatable) {
                    String message = MessageFormat.format("{0} contains string marked translatable=false! {1}", reportTag, name);
                    getLogger().error(message);
                    appendLineToReport("warnings", message);
                }
            }
        }

        Set<String> baseStringArrayNames = collectNames(baseDocument, "string-array");
        NodeList nodes1 = revised.getElementsByTagName("string-array");
        for (int i=0; i<nodes1.getLength(); i++)
        {
            Element element = (Element) nodes1.item(i);
            String name = element.getAttribute("name");
            if (!baseStringArrayNames.contains(name)) {
                String message = MessageFormat.format("{0} contains string-array not present in default! {1}", reportTag, name);
                getLogger().error(message);
                appendLineToReport("warnings", message);
            }
        }
    }

    private String getCommentAfter(Element element)
    {
        Node node = element.getNextSibling();
        if (node != null)
        {
            while (node.getNodeType() == Node.TEXT_NODE)
            {
                if (node.getTextContent().contains("\n")) {
                    return null;
                }
                node = node.getNextSibling();
            }
            if (node.getNodeType() == Node.COMMENT_NODE) {
                return ((Comment) node).getData();
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

    private boolean isReference(Node element) {
        return element.getTextContent().startsWith("@string/");
    }

    private boolean containsOnlyItemReferences(Element element)
    {
        NodeList children = element.getElementsByTagName("item");
        for (int j=0; j<children.getLength(); j++)
        {
            Node child = (Element) children.item(j);
            if (!isReference(child)) {
                return false;
            }
        }
        return true;
    }

    private boolean isMarkedCommon(Element element)
    {
        if (element.hasAttribute("tools:common")) {
            return Boolean.parseBoolean(element.getAttribute("tools:common"));
        }
        return false;
    }

    private Set<String> collectNames(Document document, String tag) {
        return collectNames(document, tag, null);
    }
    private Set<String> collectNames(Document document, String tag, Boolean translatable)
    {
        Set<String> set = new TreeSet<>();
        NodeList nodes = document.getElementsByTagName(tag);
        for (int i=0; i<nodes.getLength(); i++)
        {
            Element element = (Element) nodes.item(i);
            if (translatable != null)
            {
                if ((translatable && isTranslatable(element)) ||
                        (!translatable && !isTranslatable(element))) {
                    set.add(element.getAttribute("name"));
                }
            } else {
                set.add(element.getAttribute("name"));
            }
        }
        return set;
    }

    @Override
    protected String getReportFileName(String reportName) {
        return "translation-" + reportName + ".txt";
    }

    @Override
    protected void writeReport() {
        writeReport("actions", getReportBuilder("actions").toString());
        writeReport("todo", getReportBuilder("todo").toString());
        writeReport("warnings", getReportBuilder("warnings").toString());
    }

    protected void appendLineToReports() {
        appendLineToReport("actions");
        appendLineToReport("todo");
        appendLineToReport("warnings");
    }

}
