package com.github.alexlandau.commonscompressrepro;

import org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.harmony.pack200.Segment;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Repros {
    public static String loadTheSegmentClass() {
        return Segment.class.getName();
    }

    public static void doSomePacking() {
        File jarFile = new File("gradle/wrapper/gradle-wrapper.jar");
        try (OutputStream archiveStream = new ByteArrayOutputStream();
             FileInputStream jarStream = new FileInputStream(jarFile);
             XZCompressorOutputStream xzStream = new XZCompressorOutputStream(archiveStream);
             Pack200CompressorOutputStream pack200stream = new Pack200CompressorOutputStream(
                     xzStream)) {

            IOUtils.copy(jarStream, pack200stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
