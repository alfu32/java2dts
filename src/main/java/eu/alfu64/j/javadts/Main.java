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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Main {


    public static void main(String[] args) throws IOException {
        final List<URL> urls = new ArrayList<>();

        // Add provided JAR paths from command-line arguments to URLs
        for (final String jarPath : args) {
            try {
                urls.add(new File(jarPath).toURI().toURL());
            } catch (MalformedURLException e) {
                System.err.println("Invalid path: " + jarPath);
            }
        }

        // Add the default Java classpath to the list of URLs
        urls.addAll(ClasspathHelper.forJavaClassPath());

        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner())
                .setUrls(urls));

        final Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);
        final ArrayList<String> ignoredPackages=new ArrayList<String>(){{
            add("eu.alfu64.j.javadts");
        }};
        new File("java-types").mkdirs();
        //// Files.write(
        ////         Paths.get(new File("java-types/general.d.ts").toURI()),
        ////         (
        ////                 "export declare type  Class=Object;\n" +
        ////                 "export declare type Java={type:(fqn:string)=>Class};\n"
        ////         ).getBytes(StandardCharsets.UTF_8),
        ////         StandardOpenOption.CREATE, StandardOpenOption.APPEND
        //// );
        for (Class<?> clazz : allClasses) {
            FqnInfo classFqnInfo=new FqnInfo(clazz.getName());
            if (
                clazz.getPackage() != null &&
                        !clazz.getPackage().getName().startsWith("com.myapp")) {
                String tsInterface = generateTypeScriptInterface(clazz);

                final String dirname="java-types/" + classFqnInfo.getParentFqnInfo().getParentFqnInfo().getParentPath();
                new File(dirname).mkdirs();
                File outputFile = new File("java-types/" + classFqnInfo.getParentFqnInfo().getParentPath() + ".d.ts");
                Files.write(Paths.get(outputFile.toURI()), tsInterface.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        }
    }
    public static String generateTypeScriptInterface(Class<?> clazz) {
        FqnInfo classFqnInfo=new FqnInfo(clazz.getName());
        final StringBuilder result = new StringBuilder();

        result.append("export interface ").append(classFqnInfo.getSimpleTypeName()).append(" {\n");
        final HashMap<String,String> importsMap=new HashMap<String,String>();
        try{
            for (final Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isPrivate(field.getModifiers())) {
                    for (Annotation annotation : field.getAnnotations()) {
                        final FqnInfo fqnInfo = new FqnInfo(annotation.annotationType().getCanonicalName());
                        result.append("  @").append(fqnInfo.getSimpleTypeName()).append("()\n");
                        importsMap.put(fqnInfo.fqn,fqnInfo.getSimpleTypeName());
                    }

                    if (Modifier.isProtected(field.getModifiers())) {
                        result.append("  /* @protected */\n");
                    }
                    final FqnInfo fqnFieldTypeInfo = new FqnInfo(field.getType().getCanonicalName());
                    importsMap.put(fqnFieldTypeInfo.fqn,fqnFieldTypeInfo.getSimpleTypeNameNoArray());
                    result.append("  ")
                            .append(field.getName().replaceAll("\\[\\]",""))
                            .append(": ")
                            .append(fqnFieldTypeInfo.getSimpleTypeName())
                            .append(";\n");
                }
            }
        }catch(Throwable th){
            result.append("  /* error listing fields " + th.getMessage() + " */");
        }

        try{
            for (final Method method : clazz.getDeclaredMethods()) {
                if (!Modifier.isPrivate(method.getModifiers())) {
                    for (Annotation annotation : method.getAnnotations()) {
                        final FqnInfo fqnInfo = new FqnInfo(annotation.annotationType().getCanonicalName());
                        result.append("  @").append(fqnInfo.getSimpleTypeName()).append("()\n");
                        importsMap.put(fqnInfo.fqn,fqnInfo.getSimpleTypeName());
                    }

                    if (Modifier.isProtected(method.getModifiers())) {
                        result.append("  /* @protected */\n");
                    }

                    result.append("  ").append(method.getName()).append("(");

                    Class<?>[] parameterTypes = method.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        final FqnInfo fqnFieldTypeInfo = new FqnInfo(parameterTypes[i].getCanonicalName());
                        importsMap.put(fqnFieldTypeInfo.fqn,fqnFieldTypeInfo.getSimpleTypeNameNoArray());
                        result.append(fqnFieldTypeInfo.getSimpleTypeNameNoArray().toLowerCase())
                                .append(i)
                                .append(": ")
                                .append(fqnFieldTypeInfo.getSimpleTypeName()  );
                        if (i < parameterTypes.length - 1) {
                            result.append(", ");
                        }
                    }
                    importsMap.put(method.getReturnType().getCanonicalName(),method.getReturnType().getSimpleName());
                    result.append("): ").append(method.getReturnType().getSimpleName()).append(";\n");
                }
            }
        }catch(Throwable th){
            result.append("  /* error listing methods " + th.getMessage() + " */");
        }

        result.append("}\n");
        final StringBuilder imports = new StringBuilder();
        String pathToRoot=clazz.getPackage().getName()
                .replaceAll("\\.","/")
                .replaceAll("[a-zA-Z0-9_]+","..");
        pathToRoot=pathToRoot.length()>2?pathToRoot.substring(3):pathToRoot;
        imports.append("/* generated class ").append(clazz.getSimpleName()).append(" */\n");
        imports.append("import {Java,Class} from '")
                .append(classFqnInfo.getParentFqnInfo().getPathToRoot())
                .append("/general.d';\n");
        //imports.append("declare var Java={type:(fqn:string):Class => { return {}}}\n");
        for(final String fqn: importsMap.keySet()){
            final String name = importsMap.get(fqn);
            // imports.append("// var  ")
            //         .append(name)
            //         .append(" = Java.type('")
            //         .append(fqn)
            //         .append("');\n");

            if(fqn != null && !fqn.equals(classFqnInfo.fqn)) {
                final FqnInfo fqnInfo = new FqnInfo(fqn);
                final String firstCharName = fqnInfo.getSimpleTypeName().substring(0, 1);
                if (!firstCharName.toLowerCase().equals(firstCharName)) {
                    imports.append("import {")
                            .append(fqnInfo.getSimpleTypeNameNoArray())
                            .append("} from '")
                            .append(classFqnInfo.getParentFqnInfo().getPathToRoot()+"/"+fqnInfo.getParentPath() + ".d';\n");
                }
            }
        }
        imports.append("\n")
                .append(result.toString());

        return imports.toString();
    }
}
