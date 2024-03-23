package tech.orla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractOSProcess {

    protected static Integer execProcess(String command, Boolean shouldStdOut) {
        try {
            var process = Runtime.getRuntime().exec(command);
            if (shouldStdOut) {
                InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String linha;
                while ((linha = reader.readLine()) != null) {
                    System.out.println(linha);
                }
            }
            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
