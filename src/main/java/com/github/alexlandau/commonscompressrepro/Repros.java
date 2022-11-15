package com.github.alexlandau.commonscompressrepro;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarInputStream;

import org.apache.commons.compress.harmony.pack200.Archive;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingOptions;
import org.apache.commons.compress.harmony.pack200.Segment;

public class Repros {
    public static String loadTheSegmentClass() {
        return Segment.class.getName();
    }

    public static void doSomePacking() {
        File jarFile = new File("resources/docx4j-JAXB-ReferenceImpl-11.2.9.jar");
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile))) {
            PackingOptions options = new PackingOptions();
            options.setGzip(false);
            new Archive(jarStream, OutputStream.nullOutputStream(), options).pack();
        } catch (IOException | Pack200Exception e) {
            throw new RuntimeException(e);
        }
    }
}
