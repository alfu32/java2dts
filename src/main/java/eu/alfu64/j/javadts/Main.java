package eu.alfu64.j.javadts;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner())
                .setUrls(ClasspathHelper.forJavaClassPath()));

        Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : allClasses) {
            String tsInterface = generateTypeScriptInterface(clazz);
            File outputFile = new File("scan-out/" + clazz.getCanonicalName() + ".d.ts");
            Files.write(Paths.get(outputFile.toURI()), tsInterface.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String generateTypeScriptInterface(Class<?> clazz) {
        StringBuilder result = new StringBuilder();

        result.append("interface ").append(clazz.getSimpleName()).append(" {\n");
        HashMap<String,String> importsMap=new HashMap<>();
        try{
            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isPrivate(field.getModifiers())) {
                    for (Annotation annotation : field.getAnnotations()) {
                        result.append("  @").append(annotation.annotationType().getSimpleName()).append("()\n");
                        importsMap.put(annotation.annotationType().getCanonicalName(),"");
                    }

                    if (Modifier.isProtected(field.getModifiers())) {
                        result.append("  /* @protected */\n");
                    }
                    importsMap.put(field.getType().getCanonicalName(),"");
                    result.append("  ")
                            .append(field.getName())
                            .append(": ")
                            .append(field.getType().getCanonicalName())
                            .append(";\n");
                }
            }
        }catch(Throwable th){
            result.append("  /* error listing fields " + th.getMessage() + " */");
        }
        try{
            for (Method method : clazz.getDeclaredMethods()) {
                if (!Modifier.isPrivate(method.getModifiers())) {
                    for (Annotation annotation : method.getAnnotations()) {
                        result.append("  @").append(annotation.annotationType().getSimpleName()).append("()\n");
                    }

                    if (Modifier.isProtected(method.getModifiers())) {
                        result.append("  /* @protected */\n");
                    }

                    result.append("  ").append(method.getName()).append("(");

                    Class<?>[] parameterTypes = method.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        importsMap.put(parameterTypes[i].getCanonicalName(),"");
                        result.append("param")
                                .append(i)
                                .append(": ")
                                .append(parameterTypes[i].getCanonicalName());
                        if (i < parameterTypes.length - 1) {
                            result.append(", ");
                        }
                    }

                    result.append("): ").append(method.getReturnType().getCanonicalName()).append(";\n");
                }
            }
        }catch(Throwable th){
            result.append("  /* error listing methods " + th.getMessage() + " */");
        }

        result.append("}\n");
        StringBuilder imports = new StringBuilder();
        for(String key: importsMap.keySet()){
            imports.append("import ")
                    .append(key)
                    .append(";");
        }
        imports.append("\n")
                .append(result.toString());

        return imports.toString();
    }
}
