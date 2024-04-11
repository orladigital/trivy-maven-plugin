package tech.orla;

import tech.orla.api.GithubTrivyRelease;
import tech.orla.helper.ResourceFileReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static junit.framework.Assert.*;

public class TrivyProcessTest {

    private static final String TRIVY_TAG = "v0.49.1";

    private TrivyProcess trivyProcess;
    private DockerProcess dockerProcess;

    private GithubTrivyRelease githubTrivyRelease;

    @BeforeEach
    public void setUp() {
        githubTrivyRelease = new GithubTrivyRelease();
        trivyProcess = new TrivyProcess(githubTrivyRelease);
        dockerProcess = new DockerProcess();
    }

    @Test
    public void given_unix_os_should_return_correct_trivy_bin() throws Exception {
        System.setProperty("os.name", "Linux");

        var locationTrivyBin = trivyProcess.getLocationTrivyBin(TRIVY_TAG);
        assertTrue(locationTrivyBin.getAbsolutePath().endsWith("/target/trivy"));
    }

    @Test
    public void given_windows_os_should_return_correct_trivy_bin() throws Exception {
        System.setProperty("os.name", "win");

        var locationTrivyBin = trivyProcess.getLocationTrivyBin(TRIVY_TAG);
        assertTrue(locationTrivyBin.getAbsolutePath().endsWith("/target/trivy"));
    }

    @Test
    public void given_macos_os_should_return_correct_trivy_bin() throws Exception {
        System.setProperty("os.name", "Mac OS X");

        var locationTrivyBin = trivyProcess.getLocationTrivyBin(TRIVY_TAG);
        assertTrue(locationTrivyBin.getAbsolutePath().endsWith("/target/trivy"));
    }

    @Test
    public void should_run_trivy_scan_image() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile");
        dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api");

        var scanImageExitStatus = trivyProcess.scanImage("app/todo-api", "", TRIVY_TAG);
        assertNotNull(scanImageExitStatus);
    }

    @Test
    public void when_vuln_found_should_return_exit_code_1() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile-with-vuln");
        dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api-with-vuln");

        var scanImageExitStatus = trivyProcess.scanImage("app/todo-api-with-vuln", "", TRIVY_TAG);
        assertEquals(Integer.valueOf(1), scanImageExitStatus);
    }

    @Test
    public void when_set_type_vuln_to_low_should_return_exit_code_0() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile-with-vuln");
        dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api-with-vuln");

        var scanImageExitStatus = trivyProcess.scanImage("app/todo-api-with-vuln", "-s LOW", TRIVY_TAG);
        assertEquals(Integer.valueOf(0), scanImageExitStatus);
    }

    @Test
    public void when_set_just_vuln_type_to_library_should_return_exit_code_0() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile-with-vuln");
        dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api-with-vuln");

        var scanImageExitStatus =
                trivyProcess.scanImage("app/todo-api-with-vuln", "-s HIGH,CRITICAL --vuln-type library", TRIVY_TAG);
        assertEquals(Integer.valueOf(0), scanImageExitStatus);
    }

    @Test
    public void when_os_linux_should_return_correct_binary_name() {
        System.setProperty("os.name", "Linux");
        var binaryName = trivyProcess.resolveBinaryName("v0.50.0");
        System.out.println(binaryName);
        assertEquals("trivy_0.50.0_Linux-64bit.tar.gz", binaryName);
    }

    @Test
    public void when_os_windows_should_return_correct_binary_name() {
        System.setProperty("os.name", "win");
        var binaryName = trivyProcess.resolveBinaryName("v0.50.0");
        System.out.println(binaryName);
        assertEquals("trivy_0.50.0_Windows-64bit.tar.gz", binaryName);
    }

    @Test
    public void when_os_mac_should_return_correct_binary_name() {
        System.setProperty("os.name", "Mac OS X");
        var binaryName = trivyProcess.resolveBinaryName("v0.50.0");
        System.out.println(binaryName);
        assertEquals("trivy_0.50.0_macOS-64bit.tar.gz", binaryName);
    }

    @AfterEach
    public void tearDown() {
        System.setProperty("os.name", "Linux");
        dockerProcess.cleanDockerImage("app/todo-api");
    }
}
