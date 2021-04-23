package com.redblueflame.importer.visitor;

public class AccessRecord {
    public String scope;
    public String method;
    public AccessRecord(String scope, String method) {
        this.method = method;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "AccessRecord{" +
                "scope='" + scope + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
