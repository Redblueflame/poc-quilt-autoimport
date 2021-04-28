package com.redblueflame.importer;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import org.quiltmc.json5.JsonWriter;

public class ExperimentalWriter {
    private final HashMap<String, String> experimentalValues = new HashMap<>();
    public void addValue(String descriptor, String feature) {
        experimentalValues.put(descriptor, feature);
    }

    public void save(Writer in) throws IOException {
        JsonWriter writer = JsonWriter.createStrict(in);
        writer.beginObject();
        experimentalValues.forEach((desc, feat) -> {
            try {
                writer
                    .name(desc)
                    .value(feat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.endObject().close();
    }
}
