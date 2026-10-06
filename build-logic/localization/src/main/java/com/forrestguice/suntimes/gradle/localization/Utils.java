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

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils
{
    public static boolean isTranslatable(Element element)
    {
        if (element.hasAttribute("translatable")) {
            return Boolean.parseBoolean(element.getAttribute("translatable"));
        }
        return true;
    }

    public static boolean isReference(Node element) {
        return element.getTextContent().startsWith("@string/");
    }

    public static boolean isMarkedCommon(Element element)
    {
        if (element.hasAttribute("tools:common")) {
            return Boolean.parseBoolean(element.getAttribute("tools:common"));
        }
        return false;
    }

    public static boolean containsOnlyItemReferences(Element element)
    {
        NodeList children = element.getElementsByTagName("item");
        for (int j=0; j<children.getLength(); j++)
        {
            Node child = (Element) children.item(j);
            if (!Utils.isReference(child)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsLineReturn(String text) {
        return (text.contains("\n") || text.contains("\r"));
    }

    public static boolean containsDoubleLineReturn(String text) {
        return (text.contains("\n\n") || text.contains("\r\r") || text.contains("\r\n\r\n"));
    }

    public static int arrayChecksum(Element element)
    {
        int checksum = 0;
        NodeList children = element.getElementsByTagName("item");
        for (int i=0; i<children.getLength(); i++) {
            Node child = (Element) children.item(i);
            checksum += child.getTextContent().hashCode();
        }
        return checksum;
    }

    public static Element findElementNamed(Document document, String tag, String name)
    {
        NodeList nodes = document.getElementsByTagName(tag);
        for (int i=0; i<nodes.getLength(); i++)
        {
            Element element = (Element) nodes.item(i);
            if (name.equals(element.getAttribute("name"))) {
                return element;
            }
        }
        return null;
    }

    public static String getCommentAfter(Node element)
    {
        Node node = element.getNextSibling();
        if (node != null)
        {
            while (node.getNodeType() == Node.TEXT_NODE)
            {
                String text = node.getTextContent();
                if (text.contains("\n") || text.contains("\r")) {
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

}
