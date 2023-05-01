package br.com.orla;

import br.com.orla.utils.OS;
import br.com.orla.utils.OSDetector;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.codehaus.plexus.util.FileUtils;

public class TrivyProcess extends AbstractOSProcess {

    public String getLocationTrivyBin() throws Exception {
        var os = OSDetector.getOS();
        if (os.equals(OS.UNIX)) {
            URL resourceURL = getClass().getClassLoader().getResource("trivy_UNIX_X86_64");
            if (resourceURL != null) {
                return resourceURL.getPath();
            }
        }
        throw new Exception("Trivy bin not found");
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
        var bin = extractExecutableFromJar("trivy_UNIX_X86_64");
        var finalCommand = String.format(command, bin.getAbsolutePath(), trivyParams, dockerImageName);
        return execProcess(finalCommand, true);
    }
}
