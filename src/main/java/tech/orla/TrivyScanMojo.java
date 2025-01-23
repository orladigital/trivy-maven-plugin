package tech.orla;

import java.util.ArrayList;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tech.orla.api.GithubTrivyRelease;

@Mojo(name = "trivy-scan")
public class TrivyScanMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(required = false)
    private String dockerFilePath;

    @Parameter(required = false)
    private String imageName;

    @Parameter(required = false, name = "vulnType")
    private String vulnType;

    @Parameter(required = false)
    private String severity;

    @Parameter(required = false, defaultValue = "false")
    private Boolean ignoreUnfixed;

    @Parameter(required = false, defaultValue = "v0.49.1")
    private String trivyVersion;

    @Override
    public void execute() throws MojoExecutionException {
        String dockerImageName = imageName;
        if (dockerImageName == null) {
            var dockerProcess = new DockerProcess();
            if (!dockerProcess.isDockerInstalled()) {
                throw new MojoExecutionException("docker engine not found");
            }

            var defLocationDockerFile = project.getBasedir().getAbsolutePath().concat("/Dockerfile");
            dockerProcess.buildDockerImage(
                dockerFilePath != null ? dockerFilePath : defLocationDockerFile, project.getArtifactId());

            dockerImageName = "app/".concat(project.getArtifactId());
        }
        
        var trivyProcess = new TrivyProcess(new GithubTrivyRelease());
        try {
            var params = buildTrivyParams();
            var exitCode = trivyProcess.scanImage(dockerImageName, params, trivyVersion);
            if (exitCode == 1) {
                throw new MojoExecutionException("your app have some vulnerabilities");
            }
        } catch (Exception e) {
            throw new MojoExecutionException("error when execute trivy scan, error: ".concat(e.getMessage()));
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
