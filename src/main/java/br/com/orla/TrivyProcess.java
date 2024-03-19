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
import java.util.zip.GZIPInputStream;

public class TrivyProcess extends AbstractOSProcess {

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
                        System.out.println("Arquivo copiado com sucesso para: " + binPath);
                        return Path.of(binPath);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("decompress tar gz failed");
    }

    public File getLocationTrivyBin() throws Exception {
        // TODO: adicionar download/criar temp dir
        var tag = "v0.49.1";
        var release = githubTrivyReleaseApi.releaseByTag(tag);
        var resolveBinaryName = resolveBinaryName(tag);

        var donwloadURl = release.getAssets().stream()
                .filter(asset -> asset.getName().equals(resolveBinaryName))
                .findFirst();

        System.out.println("start download tar.gz");
        var pathFile = downloadBinaryFromGithubAssets(donwloadURl.get().getBrowserDownloadUrl(), resolveBinaryName);
        System.out.println("end download tar.gz");
        System.out.println("start decompress");
        var binFile = decompressTarGz(pathFile);
        System.out.println("end decompress");

        var fileBin = new File(binFile.toAbsolutePath().toString());
        fileBin.setExecutable(true);

        return fileBin;
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

    public Integer scanImage(String dockerImageName, String trivyParams) throws Exception {
        var command = "%s image --exit-code 1 %s %s";
        var bin = getLocationTrivyBin();
        var finalCommand = String.format(command, bin.getAbsolutePath(), trivyParams, dockerImageName);
        return execProcess(finalCommand, true);
    }
}
