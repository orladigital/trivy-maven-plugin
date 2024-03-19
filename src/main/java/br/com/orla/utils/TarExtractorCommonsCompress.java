package br.com.orla.utils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TarExtractorCommonsCompress extends TarExtractor {

    public TarExtractorCommonsCompress(InputStream tarStream, boolean gzip, Path destination) throws IOException {
        super(tarStream, gzip, destination);
    }

    @Override
    public void untar() throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(getTarStream());
             TarArchiveInputStream tar = new TarArchiveInputStream(
                     isGzip() ? new GzipCompressorInputStream(inputStream) : inputStream)) {
            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                Path extractTo = getDestination().resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(extractTo);
                } else {
                    Files.copy(tar, extractTo);
                }
            }
        }
    }
}
