package br.com.orla;

public class DockerProcess extends AbstractOSProcess {

    public Boolean isDockerInstalled() {
        var command = "docker --version";
        return execProcess(command, false) == 0;
    }

    public Integer buildDockerImage(String absolutePath, String artifactId) {
        var dockerCommand = "docker build . -t app/%s -f %s";
        var finalCommand = String.format(dockerCommand, artifactId, absolutePath);
        return execProcess(finalCommand, false);
    }

    public Integer cleanDockerImage(String artifactId) {
        var dockerCommand = "docker rmi app/%s";
        var finalCommand = String.format(dockerCommand, artifactId);
        return execProcess(finalCommand, false);
    }
}
