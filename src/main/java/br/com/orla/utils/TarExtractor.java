package br.com.orla.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class TarExtractor {

    private InputStream tarStream;
    private boolean gzip;
    private Path destination;

    public abstract void untar() throws IOException;

    public TarExtractor(InputStream tarStream, boolean gzip, Path destination) throws IOException {
        this.tarStream = tarStream;
        this.gzip = gzip;
        this.destination = destination;

        Files.createDirectories(destination);
    }

    protected TarExtractor(Path tarFile, Path destination) throws IOException {
        this(Files.newInputStream(tarFile), tarFile.endsWith("gz"), destination);
    }


    public InputStream getTarStream() {
        return tarStream;
    }

    public void setTarStream(InputStream tarStream) {
        this.tarStream = tarStream;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public Path getDestination() {
        return destination;
    }

    public void setDestination(Path destination) {
        this.destination = destination;
    }
}
