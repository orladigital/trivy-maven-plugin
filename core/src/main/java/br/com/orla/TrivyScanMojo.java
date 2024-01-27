package br.com.orla;

import java.util.ArrayList;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "trivy-scan")
public class TrivyScanMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(required = false)
    private String dockerFilePath;

    @Parameter(required = false, name = "vulnType")
    private String vulnType;

    @Parameter(required = false)
    private String severity;

    @Parameter(required = false, defaultValue = "false")
    private Boolean ignoreUnfixed;

    @Override
    public void execute() throws MojoExecutionException {
        var dockerProcess = new DockerProcess();
        if (dockerProcess.isDockerInstalled()) {
            var defLocationDockerFile = project.getBasedir().getAbsolutePath().concat("/Dockerfile");
            dockerProcess.buildDockerImage(
                    dockerFilePath != null ? dockerFilePath : defLocationDockerFile, project.getArtifactId());
            var trivyProcess = new TrivyProcess();
            try {
                var params = buildTrivyParams();
                var exitCode = trivyProcess.scanImage("app/".concat(project.getArtifactId()), params);
                if (exitCode == 1) {
                    throw new MojoExecutionException("your app have some vulnerabilities");
                }
            } catch (Exception e) {
                throw new MojoExecutionException("error when execute trivy scan, error: ".concat(e.getMessage()));
            }
        } else {
            throw new MojoExecutionException("docker engine not found");
        }
    }

    public String buildTrivyParams() {
        var params = new ArrayList<String>();

        if (vulnType != null && !vulnType.isEmpty()) {
            params.add("--vuln-type ".concat(vulnType));
        }
        if (severity != null && !severity.isEmpty()) {
            params.add("-s ".concat(severity));
        }
        if (ignoreUnfixed) {
            params.add("--ignore-unfixed");
        }
        return String.join(" ", params);
    }
}
