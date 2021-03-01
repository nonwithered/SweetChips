package org.sweetchips.transformlauncher;

import org.sweetchips.constsweeper.ConstSweeperExtension;
import org.sweetchips.plugin4gradle.UnionExtension;
import org.sweetchips.plugin4gradle.Util;
import org.sweetchips.plugin4gradle.util.FilesUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Launcher {

    public static void main(String[] args) throws Throwable {
        int idx = 0;
        Path in = Paths.get(args[idx++]);
        Path out = Paths.get(args[idx++]);
        Path tmp = Paths.get(args[idx++]);
        Path script = Paths.get(args[idx++]);
        String sep = args[idx++];
        String[] dirs = args[idx++].split(sep);
        Map<String, String> properties = getProperties(args, idx);
        TransformBridge.init(properties);
        config();
        TransformBridge.launch(in, out, tmp, (i, o) ->
                new TransformInvocationImpl(i, o, Arrays.stream(dirs).map(Paths::get).collect(Collectors.toList())));
    }

    private static void config() {
        TransformBridge.apply(Util.NAME);
        TransformBridge.config("SweetChips", (UnionExtension it) -> {
            it.incremental(true);
            it.addPrepare(Collections.singletonMap("doLast", Arrays.asList("org.sweetchips.visitors.HidePrepareClassVisitor", "org.sweetchips.visitors.UncheckcastPrepareClassVisitor")));
            it.addTransform(Collections.singletonMap("doLast", Arrays.asList("org.sweetchips.visitors.HideTransformClassVisitor", "org.sweetchips.visitors.UncheckcastTransformClassVisitor")));
        });
        TransformBridge.apply("ConstSweeper");
        TransformBridge.config("ConstSweeper", (ConstSweeperExtension it) -> {
            it.attach(null);
            it.ignore("org.sweetchips.test.TestConst", "org.sweetchips.test.TestConst$CheckIgnorePart#sIgnore");
        });
    }

    private static Map<String, String> getProperties(String[] args, int idx) throws IOException {
        Map<String, String> properties = new HashMap<>();
        while (idx < args.length) {
            Path path = Paths.get(args[idx++]);
            addProperty(path, properties);
        }
        return properties;
    }

    private static void addProperty(Path path, Map<String, String> map) throws IOException {
        ZipFile zipFile = new ZipFile(path.toFile());
        try (InputStream inputStream = Files.newInputStream(path);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String fileName = FilesUtil.getFileName(Paths.get(entry.getName()));
                if (fileName.endsWith(".properties")) {
                    Properties property = new Properties();
                    try (InputStream stream = zipFile.getInputStream(entry)) {
                        property.load(stream);
                    }
                    Collections.list(property.propertyNames()).stream()
                            .filter("implementation-class"::equals)
                            .forEach(it -> map.put(fileName.substring(0, fileName.indexOf(".properties")), property.getProperty(it.toString())));
                }
            }
        }
    }
}
