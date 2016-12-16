package ch.zhaw.file_operations;

import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


/**
 * Contains tools for creating proper result project tree ind building
 */
public class JarBuilder {
    private String path;


    public JarBuilder() {
    }

    public JarBuilder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Builds the maven project using maven sdk for java
     *
     * @throws URISyntaxException
     */
    private void mvnBuild() throws URISyntaxException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(path + "/pom.xml"));
        request.setGoals(Arrays.asList("clean", "install"));


        Invoker invoker = new DefaultInvoker();
        try {
            if (invoker.getMavenHome() == null){
                invoker.setMavenHome(new File("/usr/share/maven/"));
            }

            invoker.execute(request);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates built jar of 'Lambda Function'
     */
    public void createJar() {
        try {
            mvnBuild();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
