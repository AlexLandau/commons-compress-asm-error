package com.github.alexlandau.commonscompressrepro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.commons.compress.harmony.pack200.Archive;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingOptions;
import org.apache.commons.compress.harmony.pack200.Segment;
import org.objectweb.asm.Opcodes;

public class ManualTesting {
    private static final File JAXB_JAR = new File("resources/docx4j-JAXB-ReferenceImpl-11.2.9.jar");
    private static final File HW_JAR = new File("resources/hw.jar");
    private static final File GRADLE_JAR = new File("resources/gradle-wrapper.jar");

    // Change this variable to try out other JARs
    private static final File TARGET_JAR = GRADLE_JAR;

    private static final File PACKED_JAR = new File("resources/packed.pack");
    private static final File ROUNDTRIP_JAR = new File("resources/roundtrip.jar");



    public static void main(String[] args) throws IOException {
        convertAndCompare();

        // Comment this out if you want to use the same comparison for files you manually packed
        // compareManual();
    }

    private static void convertAndCompare() throws IOException {
        PACKED_JAR.delete();
        ROUNDTRIP_JAR.delete();

        // Comment this line out to test using ASM7
        // Segment.ASM_API = Opcodes.ASM7;
        doSomePacking();
        doSomeUnpacking();

        compareFiles(new JarFile(TARGET_JAR), new JarFile(ROUNDTRIP_JAR));
    }

    private static void compareManual() throws IOException {
        compareFiles(new JarFile(GRADLE_JAR), new JarFile(new File("resources/gradle-wrapper.manual.jar")));
    }

    public static void doSomePacking() {
        try (OutputStream outputStream = new FileOutputStream(PACKED_JAR);
             JarInputStream jarStream = new JarInputStream(new FileInputStream(TARGET_JAR))) {

            PackingOptions options = new PackingOptions();
            options.setGzip(false);
            new Archive(jarStream, outputStream, options).pack();
        } catch (IOException | Pack200Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void doSomeUnpacking() {
        try (OutputStream outputStream = new FileOutputStream(ROUNDTRIP_JAR);
             FileInputStream packedStream = new FileInputStream(PACKED_JAR);
             JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {

            new org.apache.commons.compress.harmony.unpack200.Archive(packedStream, jarOutputStream).unpack();
        } catch (IOException | Pack200Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void compareFiles(JarFile jarFile, JarFile jarFile2) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {

            JarEntry entry = entries.nextElement();
            assertNotNull("Initial entry is null", entry);

            String name = entry.getName();

            if (name.contains("META-INF")) {
                continue;
            }

            JarEntry entry2 = jarFile2.getJarEntry(name);
            assertNotNull("Missing Entry: " + name, entry2);

            InputStream ours = jarFile.getInputStream(entry);
            InputStream expected = jarFile2.getInputStream(entry2);

            BufferedReader reader1 = new BufferedReader(
                    new InputStreamReader(ours));
            BufferedReader reader2 = new BufferedReader(
                    new InputStreamReader(expected));
            String line1 = reader1.readLine();
            String line2 = reader2.readLine();
            int i = 1;
            while (line1 != null || line2 != null) {
                assertEquals("Unpacked files differ for " + name, line2, line1);
                line1 = reader1.readLine();
                line2 = reader2.readLine();
                i++;
            }
            reader1.close();
            reader2.close();
        }
        jarFile.close();
        jarFile2.close();
    }

    private static void assertNotNull(String errorMessage, Object notNull) {
        if (notNull == null) {
            throw new RuntimeException(errorMessage);
        }
    }

    private static void assertEquals(String errorMessage, String str1, String str2) {
        if (!str1.equals(str2)) {
            throw new RuntimeException(errorMessage);
        }
    }
}
