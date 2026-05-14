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

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class CleanupTranslations extends DefaultTask
{
    /**
     * @return e.g. strings|help_content
     */
    @Input
    public abstract Property<String> getBaseNames();

    /**
     * Base directory of resources to be scanned.
     * @return e.g. app/src/main/res
     */
    @Input
    public abstract Property<String> getInputDir();

    /**
     * Output directory for generated bundles.
     * @return e.g. ${buildDir}/generated/sources/staticResources
     */
    @Input
    public abstract Property<String> getOutputDir();

    @TaskAction
    public void run()
    {
        init();
        String[] baseNames = getBaseNames().get().split("\\|");
        for (String baseName : baseNames) {
            cleanupTranslations(baseName.trim());
        }
    }

    protected void init() {
    }

    protected void cleanupTranslations(String baseName)
    {
        Path directory = Path.of(getInputDir().get());
        List<Path> resources = listXmlFiles(baseName, directory);
        for (Path path : resources)
        {
            try
            {
                Document document = loadDocument(path);
                String parent = path.getParent().getFileName().toString();
                writeDocument(baseName, parent, reviseDocument(parent, baseName, document));

            } catch (IOException e) {
                getLogger().error("CleanupTranslations", e);
            }
        }
    }

    protected Document reviseDocument(String parent, String baseName, Document document) {
        return document;
    }

    protected Document loadDocument(Path path)
    {
        Document document = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            try (Reader in = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))
            {
                InputSource source = new InputSource(in);
                source.setEncoding("UTF-8");
                document = builder.parse(source);

            } catch (SAXException | IOException e) {
                getLogger().error("CleanupTranslations", e);
            }

        } catch (ParserConfigurationException e) {
            getLogger().error("CleanupTranslations", e);
        }
        return document;
    }

    protected void writeDocument(String baseName, String parent, Document document) throws IOException
    {
        String path = getOutputDir().get() + "/" + parent + "/" +  baseName + ".xml";
        File outputFile = new File(path);
        File directory = outputFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create output directory " + directory.getAbsolutePath());
        }

        try (OutputStream out = Files.newOutputStream(Path.of(path)))
        {
            try {
                DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
                DOMImplementationLS ls = (DOMImplementationLS) registry.getDOMImplementation("LS");
                LSSerializer serializer = ls.createLSSerializer();

                LSOutput lsOutput = ls.createLSOutput();
                lsOutput.setByteStream(out);
                lsOutput.setEncoding("UTF-8");
                serializer.write(document, lsOutput);

            } catch (ClassNotFoundException e) {
                getLogger().error("CleanupTranslations: Class not found!", e);

            } catch (InstantiationException e) {
                getLogger().error("CleanupTranslations", e);

            } catch (IllegalAccessException e) {
                getLogger().error("CleanupTranslations: IllegalAccess!", e);
            }
        }
    }

    protected List<Path> listXmlFiles(String baseName, Path directory)
    {
        try (Stream<Path> stream = Files.walk(directory)) {
            return stream.filter(Files::isRegularFile)
                    .filter(new Predicate<Path>() {
                        @Override
                        public boolean test(Path path) {
                            return hasXmlFileName(baseName, path);
                        }
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            getLogger().error("Failed to list string.xml files in {}", directory, e);
            return Collections.emptyList();
        }
    }
    protected static boolean hasXmlFileName(String baseName, Path path)
    {
        String p = path.getFileName().toString().toLowerCase();
        return p.equals(baseName + ".xml");
    }

}
