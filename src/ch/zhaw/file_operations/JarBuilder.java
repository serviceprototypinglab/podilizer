package ch.zhaw.file_operations;

import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
            //
        }
    }
    public String createProjTree(String path) throws IOException {
        creatDir(path);
        Files.copy(Paths.get("additional/pom.xml"), Paths.get(path + "/pom.xml"), REPLACE_EXISTING);
        creatDir(path + "/src");
        creatDir(path + "/src/main");
        creatDir(path + "/src/main/java");
        return path + "/src/main/java";
    }
    void mvnBuild(String path) throws MavenInvocationException, URISyntaxException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(path + "/pom.xml"));
        request.setGoals( Arrays.asList( "clean", "install" ) );

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("/usr/share/maven"));
        invoker.execute( request );
    }
    public void createJar(String path){
        try {
            createProjTree(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mvnBuild(path);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
