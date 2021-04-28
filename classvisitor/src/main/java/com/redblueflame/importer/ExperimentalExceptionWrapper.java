package com.redblueflame.importer;

import org.gradle.internal.exceptions.DefaultMultiCauseException;
import org.gradle.api.logging.Logger;

import java.util.List;

public class ExperimentalExceptionWrapper extends DefaultMultiCauseException {
    private final List<ExperimentalException> causes;

    public ExperimentalExceptionWrapper(String message, List<ExperimentalException> causes) {
        super(message, causes);
        this.causes = causes;
    }
    public void printMessages(Logger logger) {
        causes.forEach(e -> logger.error(e.getLongMessage()));
    }
}
