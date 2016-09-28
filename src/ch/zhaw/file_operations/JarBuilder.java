package ch.zhaw.file_operations;

import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by dord on 9/28/16.
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
    public void creatDir(String path){
        File dir = new File(path);
        if (!dir.exists()){
            dir.mkdir();
        }else{
            System.err.println("Failed to create a directory");
        }
    }
    public void createProjTree(String path) throws IOException {
        creatDir(path);
        Files.copy(Paths.get("additional/pom.xml"), Paths.get("/home/dord/Templates/emptyTestDirectory1/pom.xml"), REPLACE_EXISTING);
        creatDir(path + "/src");
        creatDir(path + "/src/main");
        creatDir(path + "/src/main/java");

    }
    public void mvnBuild() throws MavenInvocationException, URISyntaxException {
        InvocationRequest request = new DefaultInvocationRequest();

//        URL url = this.getClass().getResource("/home/dord/Templates/emptyTestDirectory1/pom.xml");
//        File file = new File(url.toURI());
        request.setPomFile(new File("/home/dord/Templates/emptyTestDirectory1/pom.xml"));
        request.setGoals( Arrays.asList( "clean", "install" ) );

        Invoker invoker = new DefaultInvoker();
        invoker.execute( request );
    }


}
