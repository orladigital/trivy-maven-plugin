package br.com.orla.helper;

import java.io.File;
import java.net.URL;

public class ResourceFileReader {

    public static File loadFile(String fileName) throws Exception {
        ClassLoader classLoader = ResourceFileReader.class.getClassLoader();
        URL resourceURL = classLoader.getResource(fileName);

        if (resourceURL != null) {
            return new File(resourceURL.getFile());
        }
        throw new Exception("Resource file not found");
    }
}
