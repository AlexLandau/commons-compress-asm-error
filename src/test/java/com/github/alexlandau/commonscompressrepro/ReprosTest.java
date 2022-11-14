/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.alexlandau.commonscompressrepro;

import static org.junit.Assert.fail;

import org.apache.commons.compress.harmony.pack200.Segment;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class ReprosTest {
    @Test
    public void testLoadingTheClass() {
        Repros.loadTheSegmentClass();
    }

    @Test
    public void testActuallyUsingPack200() {
        try {
            Repros.doSomePacking();
            fail();
        } catch (UnsupportedOperationException e) {
            // intentional passthrough
        }


        // override the ASM to 7
        Segment.ASM_API = Opcodes.ASM7;
        Repros.doSomePacking();

        // Resetting back to the default to avoid stateful behavior
        Segment.ASM_API = Opcodes.ASM4;
    }
}
