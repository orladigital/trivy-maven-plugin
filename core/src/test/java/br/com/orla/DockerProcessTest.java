package br.com.orla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.orla.helper.ResourceFileReader;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DockerProcessTest {

    private DockerProcess dockerProcess;

    @BeforeEach
    public void setUp() {
        dockerProcess = new DockerProcess();
    }

    @Test
    @Order(1)
    public void should_return_true() {
        var existsDocker = dockerProcess.isDockerInstalled();
        assertTrue(existsDocker);
    }

    @Test
    @Order(2)
    public void when_build_image_should_return_exit_code_0() throws Exception {
        var dockerFile = ResourceFileReader.loadFile("Dockerfile");
        var exitCode = dockerProcess.buildDockerImage(dockerFile.getAbsolutePath(), "todo-api");
        assertEquals(0, exitCode);
    }

    @Test
    @Order(3)
    public void when_clean_image_should_return_exit_code_0() {
        var exitCode = dockerProcess.cleanDockerImage("todo-api");
        assertEquals(0, exitCode);
    }
}
