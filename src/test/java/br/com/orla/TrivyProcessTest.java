package br.com.orla;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import br.com.orla.helper.ResourceFileReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TrivyProcessTest {

    private TrivyProcess trivyProcess;
    private DockerProcess dockerProcess;

    @BeforeEach
    public void setUp() {
        trivyProcess = new TrivyProcess();
        dockerProcess = new DockerProcess();
    }

    @Test
    public void given_unix_os_should_return_correct_trivy_bin() throws Exception {
        System.setProperty("os.name", "Linux");

        var locationTrivyBin = trivyProcess.getLocationTrivyBin();
        assertTrue(locationTrivyBin.endsWith("trivy_UNIX_X86_64"));
    }

    @Test
    public void should_run_trivy_scan_image() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile");
        dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api");

        var scanImageExitStatus = trivyProcess.scanImage("app/todo-api", "");
        assertEquals(Integer.valueOf(0), scanImageExitStatus);
    }

    @Test
    public void when_vuln_found_should_return_exit_code_1() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile-with-vuln");
        dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api-with-vuln");

        var scanImageExitStatus = trivyProcess.scanImage("app/todo-api-with-vuln", "");
        assertEquals(Integer.valueOf(1), scanImageExitStatus);
    }

    @Test
    public void when_set_type_vuln_to_low_should_return_exit_code_0() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile-with-vuln");
        dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api-with-vuln");

        var scanImageExitStatus = trivyProcess.scanImage("app/todo-api-with-vuln", "-s LOW");
        assertEquals(Integer.valueOf(0), scanImageExitStatus);
    }

    @Test
    public void when_set_just_vuln_type_to_library_should_return_exit_code_0() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile-with-vuln");
        dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api-with-vuln");

        var scanImageExitStatus =
                trivyProcess.scanImage("app/todo-api-with-vuln", "-s HIGH,CRITICAL --vuln-type library");
        assertEquals(Integer.valueOf(0), scanImageExitStatus);
    }

    @AfterEach
    public void tearDown() {
        dockerProcess.cleanDockerImage("app/todo-api");
    }
}
