# commons-compress-asm-error

Repro for commons-compress 1.21 failing with asm 4.0+

Trying to use the Pack200CompressorOutputStream with asm 4.0+ on the classpath results in the error: `java.lang.IncompatibleClassChangeError: class org.apache.commons.compress.harmony.pack200.Segment can not implement org.objectweb.asm.ClassVisitor, because it is not an interface (org.objectweb.asm.ClassVisitor is in unnamed module of loader 'app')`

Running `./gradlew test` runs two tests reproducing the issue. The `doSomePacking()` test works successfully when commons-compress 1.20 is used instead of 1.21.

Per https://asm.ow2.io/versions.html, the ClassVisitor in ASM became an abstract class in 4.0 RC2 (in 2011) "in order to ensure backward binary compatibility in future ASM versions".
