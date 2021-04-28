package com.redblueflame.importer;

import com.redblueflame.importer.visitor.AccessRecord;

import java.io.File;

public class ExperimentalException extends Error {
    private final AccessRecord ar;
    private final String feature;

    public ExperimentalException(AccessRecord ar, String feature) {
        super("The feature " + feature + " is marked as experimental, but not enabled in the " + AccessRecord.config_name + " file.");
        this.ar = ar;
        this.feature = feature;
        this.setStackTrace(new StackTraceElement[]{
                ar.getStackElement()
        });
    }
    // Print a java-like error message


    @Override
    public String getMessage() {
        return ar.getShortPath() + ": error: " + super.getMessage();
    }

    public String getLongMessage() {
        return ar.getPath() + ": error: " + super.getMessage();
    }

    public String getBaseMessage() {
        return super.getMessage();
    }

    public String getTrace() {
        return this.getStackTrace()[0].toString();
    }
}
