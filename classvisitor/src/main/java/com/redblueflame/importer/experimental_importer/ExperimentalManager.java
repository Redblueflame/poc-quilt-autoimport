package com.redblueflame.importer.experimental_importer;

import org.apache.commons.io.IOUtils;
import org.quiltmc.json5.JsonReader;

import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExperimentalManager {
    public HashMap<String, String> experimental_values = new HashMap<>();

    public void import_file(JarFile jar) {
        try {
            JarEntry experimentalFile = jar.getJarEntry(".experimental.json");
            if (experimentalFile == null) {
                // This dependency does not contain an experimental file
                return;
            }
            // Import the experimental file
            InputStream stream = jar.getInputStream(experimentalFile);
            Reader reader = new InputStreamReader(stream);
            JsonReader json = JsonReader.createStrict(reader);
            json.beginObject();
            while (json.hasNext()) {
                String name = json.nextName();
                String feature = json.nextString();
                experimental_values.put(name, feature);
            }
        } catch (Exception e) {
            System.err.println("An error occurred while loading the dependencies");
        }
    }

    public void printExperimentalFunctions() {
        System.out.println("Printing experimental functions!");
        experimental_values.forEach((key, val) -> {
            System.out.println(key + " -> " + val);
        });
    }

    /**
     * Gets the corresponding experimental tag if the function is marked as one.
     * returns null otherwise.
     *
     * @param indicator The full indicator of the function, method call or
     * @return Null if not experimental, a string feature otherwise
     */
    @Nullable
    public String getExperimentalTag(String indicator) {
        if (indicator.startsWith("module/")) {
            // This is a module / class, we need it to be treated like that.
            String val = experimental_values.get(indicator.replace("module/", ""));
            if (val != null) {
                return val;
            }
            //TODO: Mount levels one by one & check if it appears.
        }
        return experimental_values.get(indicator);
    }
}
