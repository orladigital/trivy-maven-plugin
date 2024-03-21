package br.com.orla;

import br.com.orla.api.GithubTrivyReleaseApi;
import br.com.orla.utils.OS;
import br.com.orla.utils.OSDetector;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class TrivyProcess extends AbstractOSProcess {

    public static final Logger LOG = Logger.getLogger(TrivyProcess.class.getName());

    private final GithubTrivyReleaseApi githubTrivyReleaseApi;

    public TrivyProcess(GithubTrivyReleaseApi githubTrivyReleaseApi) {
        this.githubTrivyReleaseApi = githubTrivyReleaseApi;
    }

    public String resolveBinaryName(String tag) {
        var os = OSDetector.getOS();
        var pattern = "trivy_%s_%s-%sbit.tar.gz";
        if (os.equals(OS.UNIX)) {
            return String.format(pattern, tag.substring(1), "Linux", "64");
        }
        if (os.equals(OS.WINDOWS)) {
            return String.format(pattern, tag.substring(1), "Windows", "64");
        }
        if (os.equals(OS.MAC_OSX)) {
            return String.format(pattern, tag.substring(1), "macOS", "64");
        }
        return "";
    }

    public Path downloadBinaryFromGithubAssets(String downloadUrl, String file) {
        try {
            var url = new URL(downloadUrl);
            var inputStream = url.openStream();
            var targetDirectoryPath = Paths.get("").toAbsolutePath() + "/target";

            String targetFilePath = targetDirectoryPath + "/" + file;
            try (FileOutputStream outputStream = new FileOutputStream(targetFilePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            inputStream.close();
            return Path.of(targetFilePath);
        } catch (Exception e) {
            LOG.info("error download trivy binary. Error: ".concat(e.getMessage()));
            System.out.println("error download trivy binary".concat(e.getMessage()));
        }
        throw new RuntimeException("error download binary trivy");
    }

    public Path decompressTarGz(Path pathFile) {
        try {
            var source = java.nio.file.Files.newInputStream(pathFile);
            var gzip = new GZIPInputStream(source);
            TarArchiveInputStream tar = new TarArchiveInputStream(gzip);
            TarArchiveEntry entry;

            while ((entry = tar.getNextTarEntry()) != null) {
                if (entry.getName().equals("trivy")) {
                    var targetDirectoryPath = Paths.get("").toAbsolutePath() + "/target";
                    String binPath = targetDirectoryPath + "/trivy";

                    try (OutputStream fileOutput = java.nio.file.Files.newOutputStream(Path.of(binPath));
                         BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput)) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = tar.read(buffer)) != -1) {
                            bufferedOutput.write(buffer, 0, read);
                        }
                        return Path.of(binPath);
                    }
                }
            }
        } catch (Exception e) {
            LOG.info("decompress tar.gz failed. Error = " + e.getMessage());
            throw new RuntimeException(e);
        }
        throw new RuntimeException("decompress tar gz failed");
    }

    public File getLocationTrivyBin(String trivyTag) throws Exception {
        var fileAlreadyExists = getBinFileIfAlreadyExists();
        if (fileAlreadyExists != null) return fileAlreadyExists;

        var release = githubTrivyReleaseApi.releaseByTag(trivyTag);
        var resolveBinaryName = resolveBinaryName(trivyTag);

        var donwloadURl = release.getAssets().stream()
                .filter(asset -> asset.getName().equals(resolveBinaryName))
                .findFirst();

        LOG.info("Start download trivy binary from github. binary_name: ".concat(resolveBinaryName));

        var pathFile = downloadBinaryFromGithubAssets(donwloadURl.get().getBrowserDownloadUrl(), resolveBinaryName);
        LOG.info("Download finished");

        LOG.info("Start decompress tar.gz");
        var binFile = decompressTarGz(pathFile);
        LOG.info("Decompress finished");

        var fileBin = new File(binFile.toAbsolutePath().toString());
        fileBin.setExecutable(true);

        return fileBin;
    }

    private File getBinFileIfAlreadyExists() {
        var targetPath = Paths.get("").toAbsolutePath() + "/target/trivy";
        var binFile = new File(targetPath);
        if (binFile.exists()) {
            return binFile;
        }
        return null;
    }

    public File extractExecutableFromJar(String executable) throws IOException {
        File tmpDir = Files.createTempDir();
        tmpDir.deleteOnExit();

        File command = new File(tmpDir, executable);
        FileUtils.copyURLToFile(Resources.getResource(executable), command);
        command.deleteOnExit();
        command.setExecutable(true);

        return command;
    }

    public Integer scanImage(String dockerImageName, String trivyParams, String trivyTag) throws Exception {
        var command = "%s image --exit-code 1 %s %s";
        var bin = getLocationTrivyBin(trivyTag);
        var finalCommand = String.format(command, bin.getAbsolutePath(), trivyParams, dockerImageName);
        return execProcess(finalCommand, true);
    }
}
