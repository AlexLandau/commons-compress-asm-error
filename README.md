# commons-compress-asm-error

## Update: Repro for commons-compress 1.22 failing with fix COMPRESS-582

(Note: This is a modified repro contributed by @bmarcaur.)

https://issues.apache.org/jira/browse/COMPRESS-582

I added an OSS jar `docx4j-JAXB-ReferenceImpl-11.2.9.jar` (a jar that we use internally). Attempting to pack this jar yields the following stacktrace:

```
at org.objectweb.asm.ClassVisitor.visitModule(ClassVisitor.java:153)
at org.objectweb.asm.ClassReader.readModuleAttributes(ClassReader.java:781)
at org.objectweb.asm.ClassReader.accept(ClassReader.java:580)
at org.apache.commons.compress.harmony.pack200.Segment.processClasses(Segment.java:160)
at org.apache.commons.compress.harmony.pack200.Segment.pack(Segment.java:110)
at org.apache.commons.compress.harmony.pack200.Archive.doNormalPack(Archive.java:128)
at org.apache.commons.compress.harmony.pack200.Archive.pack(Archive.java:98)
at org.apache.commons.compress.harmony.pack200.Pack200PackerAdapter.pack(Pack200PackerAdapter.java:58)
at org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream.finish(Pack200CompressorOutputStream.java:136)
at org.apache.commons.compress.compressors.pack200.Pack200CompressorOutputStream.close(Pack200CompressorOutputStream.java:118)
```

Digging in slightly, we notice that in the fix linked in the ticket the [ASM_API is being explicitly set to 4](https://github.com/apache/commons-compress/pull/216/files#diff-3c1addad1db02e94719df6002b5dee21e99ed33ae2b0d953b31d416c500f82b4R44). Fortunately, this field is non-final so it allowed me to test a "fix" using the broken setup within this repo. Setting `Segment.ASM_API = Opcodes.ASM7` causes the previously failing test to pass. Given that the linked ticket intentionally took an ASM 7.X+ dependency, I think we could potentially change that ASM_API to ASM7, but this is also my first time in the codebase.


## Repro for commons-compress 1.21 failing with asm 4.0+

(This was fixed by the 1.22 release for the example given, but see the above issue that still affects jars using certain Java 11 language features.)

Trying to use the Pack200CompressorOutputStream with asm 4.0+ on the classpath results in the error: `java.lang.IncompatibleClassChangeError: class org.apache.commons.compress.harmony.pack200.Segment can not implement org.objectweb.asm.ClassVisitor, because it is not an interface (org.objectweb.asm.ClassVisitor is in unnamed module of loader 'app')`

Running `./gradlew test` runs two tests reproducing the issue. The `doSomePacking()` test works successfully when commons-compress 1.20 is used instead of 1.21.

Per https://asm.ow2.io/versions.html, the ClassVisitor in ASM became an abstract class in 4.0 RC2 (in 2011) "in order to ensure backward binary compatibility in future ASM versions".

This was fixed by https://github.com/apache/commons-compress/pull/216, which should be part of the 1.22 release. See also the ticket: https://issues.apache.org/jira/projects/COMPRESS/issues/COMPRESS-582
