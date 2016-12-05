package ch.zhaw.file_operations;

import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Contains tools for creating proper result project tree ind building
 */
public class JarBuilder {
    private Path outputLocation;
    private Path projectToBuildLocation;


    public JarBuilder() {
        this.outputLocation = null;
        this.projectToBuildLocation = null;
    }

    public JarBuilder(Path outputLocation, Path projectToBuildLocation) {
        this.outputLocation = outputLocation;
        this.projectToBuildLocation = projectToBuildLocation;
    }

    public Path getOutputLocation() {
        return outputLocation;
    }

    public Path getProjectToBuildLocation() {
        return projectToBuildLocation;
    }

    public void setOutputLocation(Path outputLocation) {
        this.outputLocation = outputLocation;
    }

    public void setProjectToBuildLocation(Path projectToBuildLocation) {
        this.projectToBuildLocation = projectToBuildLocation;
    }

    /**
     * Creates project tree and pom-file for the 'Lambda function' maven project
     * @param path is the path for saving 'Lambda Function' project file tree;
     * @return the {@code String} which represents path for code in the result maven project
     * @throws IOException
     */
    public String createProjTree(String path) throws IOException {
        File file = new File(path);
        file.mkdir();
        Files.copy(Paths.get("additional/pom.xml"), Paths.get(path + "/pom.xml"), REPLACE_EXISTING);
        file = new File(path + "/src/main/java");
        file.mkdirs();
        return path + "/src/main/java";
    }

    /**
     * Builds the maven project using maven sdk for java
     * @param path of the maven project to be built
     * @throws MavenInvocationException
     * @throws URISyntaxException
     */
    private void mvnBuild(String path) throws MavenInvocationException, URISyntaxException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(path + "/pom.xml"));
        request.setGoals( Arrays.asList( "clean", "install" ) );

        Invoker invoker = new DefaultInvoker();
        invoker.execute( request );
    }

    /**
     * Creates built jar of 'Lambda Function'
     * @param path of the 'Lambda Function' maven project
     */
    public void createJar(String path){
        try {
            mvnBuild(path);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
