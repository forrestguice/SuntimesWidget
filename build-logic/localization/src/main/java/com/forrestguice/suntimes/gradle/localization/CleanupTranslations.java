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

package com.forrestguice.suntimes.gradle.localization;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Cleans up translations; removes string duplicates marked common and unchanged string-arrays
 * that contain only references.
 */
public abstract class CleanupTranslations extends ProcessTranslations
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

        String reportTag = (parent + "/" + baseName + ".xml");

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document revised = builder.newDocument();
            Node node = revised.importNode(original.getDocumentElement(), true);    // true; deep copy
            revised.appendChild(node);

            Document baseDocument = loadDocument(Path.of(getInputDir().get() + "/values/" + baseName + ".xml"));
            checkForIllegalItems(parent, baseName, baseDocument, revised);

            // remove duplicated strings...
            // strings marked "common" should only appear in translations when they change something
            Map<String, Node> duplicateStringNodes = findDuplicates(baseDocument, revised, "string", true);
            for (String name : duplicateStringNodes.keySet())
            {
                getLogger().warn("{} needs update! duplicated string: {}", reportTag, name);
                appendLineToReport("actions", MessageFormat.format("{0} :: removed duplicated string {1}", reportTag, name));
                removeNode(revised, reportTag, name, duplicateStringNodes.get(name));
            }

            // remove duplicated string-arrays
            Map<String, Node> duplicateStringArrayNodes = findDuplicates(baseDocument, revised, "string-array", false);
            for (String name : duplicateStringArrayNodes.keySet())
            {
                getLogger().warn("{} needs update! duplicated string-array: {}", reportTag, name);
                appendLineToReport("actions", MessageFormat.format("{0} :: removed duplicated string-array {1}", reportTag, name));
                removeNode(revised, reportTag, name, duplicateStringArrayNodes.get(name));
            }

            node.normalize();
            return revised;

        } catch (ParserConfigurationException e) {
            getLogger().error("SyncTranslations", e);
        }
        return original;
    }

    /**
     * @param baseDocument base document
     * @param other other document
     * @param tag resource tag; e.g. string, string-array, ...
     * @return a map of duplicate nodes in the `other` document
     */
    protected Map<String, Node> findDuplicates(Document baseDocument, Document other, String tag, boolean markedCommonOnly)
    {
        Map<String, Node> duplicates = new HashMap<>();
        NodeList nodes = other.getElementsByTagName(tag);
        for (int i=0; i<nodes.getLength(); i++)
        {
            Element element = (Element) nodes.item(i);
            String name = element.getAttribute("name");

            if (!markedCommonOnly || Utils.isMarkedCommon(element))
            {
                Element element0 = Utils.findElementNamed(baseDocument, tag, name);
                if (element0 == null) {
                    duplicates.put(name, element);
                    continue;
                }

                if (tag.equals("string-array")) {
                    if (Utils.containsOnlyItemReferences(element)
                            && Utils.arrayChecksum(element) == Utils.arrayChecksum(element0)) {
                        duplicates.put(name, element);
                    }

                } else {
                    if (element.getTextContent().equals(element0.getTextContent())) {
                        duplicates.put(name, element);
                    }
                }
            }
        }
        return duplicates;
    }

    private void removeNode(Document document, String reportTag, String name, Node element)
    {
        /*Node nextNode = element.getNextSibling();
        if (nextNode != null)
        {
            String comment = removeCommentAfter(nextNode, true, new CommentFilter()
            {
                @Override
                public boolean matchesFilter(String comment) {
                    return (comment != null && !Utils.containsLineReturn(comment));
                }
            });
            getLogger().error(reportTag + ": " + name + ":" + comment);
        }*/

        /*Node prevNode = element.getPreviousSibling();
        if (hasCommentBefore(prevNode)) {
            String comment = removeCommentBefore(prevNode);
            getLogger().error(reportTag + ": " + name + ":" + comment);
        }*/

        Node parent = element.getParentNode();

        String comment = createCommentFrom(element);
        Comment commentNode = document.createComment(" OPTIONAL: " + comment + " ");
        //Text newLine = document.createTextNode("\n    ");

        parent.appendChild(commentNode);
        //parent.appendChild(newLine);

        parent.insertBefore(commentNode, element);
        //parent.insertBefore(newLine, element);
        parent.removeChild(element);

        getLogger().error("!!" + comment);
    }

    private String createCommentFrom(Node node)
    {
        if (node != null)
        {
            StringBuilder comment = new StringBuilder();
            comment.append(tagStart(node));

            if (node.hasChildNodes())
            {
                NodeList children = node.getChildNodes();
                for (int i=0; i<children.getLength(); i++)
                {
                    Node childNode = children.item(i);
                    if (childNode.getNodeType() == Node.COMMENT_NODE) {
                        comment.append("//");
                        comment.append(((Comment) childNode).getData());

                    } else if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        comment.append(tagStart(childNode));
                        comment.append(childNode.getTextContent());
                        comment.append(tagEnd(childNode));

                    } else {
                        comment.append(childNode.getTextContent());
                    }
                }
            } else {
                comment.append(node.getNodeValue());
            }

            comment.append(tagEnd(node));
            return comment.toString();
        }
        return null;
    }

    public static String tagStart(Node node)
    {
        StringBuilder retValue = new StringBuilder();
        retValue.append("<");
        retValue.append(node.getNodeName());

        NamedNodeMap attribs = node.getAttributes();
        for (int i=0; i<attribs.getLength(); i++)
        {
            Node attrib = attribs.item(i);
            retValue.append(" ");
            retValue.append(attrib);
        }
        retValue.append(">");
        return retValue.toString();
    }
    public static String tagEnd(Node node) {
        return "</" + node.getNodeName() + ">";
    }

    /**
     * Removes whitespaces, comments, and the line return after a node.
     */
    private String removeCommentAfter(Node node, boolean allowRecursion, CommentFilter filter)
    {
        if (node == null) {
            return null;
        }

        StringBuilder retValue = new StringBuilder();
        while (node != null && node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.COMMENT_NODE)
        {
            boolean isComment = (node.getNodeType() == Node.COMMENT_NODE);
            String text = (isComment) ? ((Comment) node).getData() : node.getTextContent();
            retValue.append(text);

            boolean hasLineReturn = Utils.containsLineReturn(text);
            boolean hasDoubleLineReturn = Utils.containsDoubleLineReturn(text);
            String extraComment = Utils.getCommentAfter(node);

            Node nextNode = node.getNextSibling();
            if (!isComment || filter == null || filter.matchesFilter(text)) {
                node.getParentNode().removeChild(node);
            }

            if (hasDoubleLineReturn) {
                break;

            } else if (hasLineReturn) {    // remove comments on trailing line
                if (allowRecursion && filter != null && filter.matchesFilter(extraComment)) {
                    retValue.append(removeCommentAfter(nextNode, false, filter));
                    break;
                }
                break;
            }
            node = nextNode;
        }
        return retValue.toString();
    }

    private boolean hasCommentBefore(Node node)
    {
        if (node == null) {
            return false;
        }

        boolean foundComment = false;
        boolean foundWhitespace = false;

        while (node != null && (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.COMMENT_NODE))
        {
            boolean isComment = (node.getNodeType() == Node.COMMENT_NODE);
            String text = (isComment) ? ((Comment) node).getData() : node.getTextContent();
            foundComment |= isComment;

            if (!isComment && Utils.containsDoubleLineReturn(text)) {
                foundWhitespace = true;
                break;
            }
            node = node.getPreviousSibling();
        }
        return (foundComment && foundWhitespace);
    }

    private String removeCommentBefore(Node node)
    {
        if (node == null) {
            return null;
        }

        StringBuilder retValue = new StringBuilder();
        while (node != null && (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.COMMENT_NODE))
        {
            boolean isComment = (node.getNodeType() == Node.COMMENT_NODE);
            String text = (isComment) ? ((Comment) node).getData() : node.getTextContent();
            retValue.append(text);

            boolean hasDoubleLineReturn = Utils.containsDoubleLineReturn(text);
            node.getParentNode().removeChild(node);

            if (hasDoubleLineReturn) {
                break;
            }
            node = node.getPreviousSibling();
        }
        return retValue.toString();
    }

    public interface CommentFilter {
        boolean matchesFilter(String comment);
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
