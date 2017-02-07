package ch.zhaw.file_operations;

import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.IOUtil;
import sun.rmi.log.LogHandler;

import java.io.*;
import java.net.URISyntaxException;
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
        request.setPomFile(new File(path));
        request.setGoals(Arrays.asList("clean", "install"));
        File buildLog = new File(path + "/buildLog.txt");

        Invoker invoker = new DefaultInvoker();
        try {
            if (invoker.getMavenHome() == null){
                invoker.setMavenHome(new File("/usr/share/maven/"));
            }
            //log the build output to file
            PrintStream printStream = new PrintStream(buildLog);
            InvocationOutputHandler outputHandler = new PrintStreamHandler(printStream, true);
            invoker.setOutputHandler(outputHandler);

            InvocationResult result = invoker.execute(request);
            printBuildResult(path, result.getExitCode());
            printStream.close();
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
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
    private void printBuildResult(String path, int exitCode){
        String result = "Build result of project " + path + " : ";
        if (exitCode == 0) {
            result += "[SUCCESS]";
        } else {
            result += "[FAILURE]";
        }
        System.out.println(result);
    }

}
