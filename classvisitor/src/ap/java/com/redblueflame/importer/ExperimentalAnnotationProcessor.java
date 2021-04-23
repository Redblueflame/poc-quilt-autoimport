package com.redblueflame.importer;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

@SupportedAnnotationTypes("com.redblueflame.importer.Experimental")
public class ExperimentalAnnotationProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnvironment;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
        this.elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        StringBuilder file = new StringBuilder();
        annotations.forEach(annotation -> {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element elem : elements) {
                Experimental annotationVal = elem.getAnnotation(Experimental.class);
                String feature = annotationVal.value();
                String packageName = annotationVal.value();
                if (elem.getKind() == ElementKind.PACKAGE) {
                    packageName = getCanonicalPackageName(elem);
                } else if (elem.getKind().isClass()) {
                    packageName = getCanonicalClassName(elem);
                } else if (elem.getKind() == ElementKind.METHOD || elem.getKind() == ElementKind.CONSTRUCTOR) {
                    packageName = getFullMethodName(elem);
                } else if (elem.getKind() == ElementKind.FIELD) {
                    packageName = getFullFieldName(elem);
                } else {
                    throw new RuntimeException("Cannot process unknown element kind " + elem.getKind());
                }
                file.append(packageName).append(":").append(feature).append(";\n");
                System.err.println(packageName + " -> " + feature);
            }
            try {
                FileObject ressource = processingEnvironment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", ".experimental");
                Writer writer = ressource.openWriter();
                writer.write(file.toString());
                writer.close();
            } catch (IOException e) {
                System.err.println("An error occurred while creating the ressource file.");
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    private static String getCanonicalPackageName(Element pkg) {
        return ((PackageElement) pkg).getQualifiedName().toString().replace('.', '/');
    }
    private static String getCanonicalClassName(Element klass) {
        return ((TypeElement) klass).getQualifiedName().toString().replace('.', '/');
    }

    private static String getFullMethodName(Element method) {
        return getCanonicalClassName(method.getEnclosingElement()) + buildMethodName((ExecutableElement) method);
    }

    private static String buildMethodName(ExecutableElement element) {
        StringBuilder sb = new StringBuilder();
        sb.append(element.getSimpleName());
        sb.append('(');
        for (TypeMirror parameterType : ((ExecutableType) element.asType()).getParameterTypes()) {
            String type = parameterType.toString();
            boolean array = false;
            // First we need to remove arrays and generics
            if (type.endsWith("[]")) {
                type = type.substring(0, type.indexOf('['));
                array = true;
            }

            if (type.contains("<")) {
                type = type.substring(0, type.indexOf('<')); // This will remove all generics
            }

            sb.append(signature(type));
            if (array) {
                sb.append("[");
            }

        }
        sb.append(")");
        sb.append(signature(((ExecutableType) element.asType()).getReturnType().toString()));
        return sb.toString();
    }

    private static String getFullFieldName(Element field) {
        if (!(field instanceof VariableElement)) {
            throw new ClassCastException("Expected variable element");
        }
        return getCanonicalClassName(field.getEnclosingElement()) + field.getSimpleName() + signature(field.asType().toString());
    }

    /**
     * Returns 'L' to indicate the start of a reference
     */
    private static String signature(String type) {
        switch (type) {
            case "void":
                return "V";
            case "byte":
                return "B";
            case "char":
                return "C";
            case "double":
                return "D";
            case "float":
                return "F";
            case "int":
                return "I";
            case "long":
                return "J";
            case "short":
                return "S";
            case "boolean":
                return "Z";
            default:
                return "L" + type.replace('.', '/') + ";";
        }
    }
}
