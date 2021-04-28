package com.redblueflame.importer.visitor;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class AccessRecord {
    public static String basePath;
    public static String config_name;

    private boolean is_other;

    public String file_name;
    public String scope;
    public String method;
    public String descriptor;
    public int line;
    public AccessRecord(String file_name, String scope, int line, String method, String descriptor) {
        this.is_other = false;
        this.file_name = file_name;
        this.scope = scope;
        this.method = method;
        this.line = line;
        this.descriptor = descriptor;
    }

    public AccessRecord special() {
        this.is_other = true;
        return this;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "AccessRecord{" +
                "file_name='" + file_name + '\'' +
                ", scope='" + scope + '\'' +
                ", method='" + method + '\'' +
                ", line=" + line +
                '}';
    }

    public String getPath() {
        return AccessRecord.basePath
                + File.separator +
                getTestedPath().replace("/", File.separator) + ":"
                + this.line;
    }

    public String getShortPath() {
        return this.file_name + ":" + this.line;
    }

    public String getTestedPath() {
        String extension = FilenameUtils.getExtension(this.file_name);
        String expected = this.file_name.replace("." + extension, "");
        String data = this.scope;
        while (!data.endsWith(expected) || data.length() <= 1) {
            data = this.scope.substring(0,this.scope.lastIndexOf("/"));
        }
        return data + "." + extension;
    }

    public StackTraceElement getStackElement() {
        if (!is_other) {
            return new StackTraceElement(scope.replace("/", "."), method, file_name, line);
        } else {
            return new StackTraceElement(Arrays.stream(descriptor.split("/")).skip(1).reduce("", (s, s2) -> s = s + "." + s2), "", file_name, line);
        }
    }
}
