package ch.zhaw.file_operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class JarUploader {
    private String region;
    private String runtime = "java8";
    private String role;
    private String functionName;
    private String zipFile;
    private String handler;
    private int timeout;
    private int memorySize;

    JarUploader(String functionName, String zipFile, String handler, int timeout, int memorySize, String confPath) {
        this.functionName = functionName;
        this.zipFile = zipFile;
        this.handler = handler;
        this.timeout = timeout;
        this.memorySize = memorySize;
        this.region = ConfigReader.getConfig(confPath).getAwsRegion();
        this.role = ConfigReader.getConfig(confPath).getAwsRole();
    }

    /**
     * Writes command into CMD and run it
     *
     * @param command the {@code String} to be run
     */
    private void writeIntoCMD(String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            final Process process = runtime.exec(command);
            new Thread(new Runnable() {
                public void run() {
                    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    BufferedReader outErrors = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String lineError = null;
                    try {
                        while ((line = input.readLine()) != null)
                            System.out.println(line);
                        while ((lineError = outErrors.readLine()) != null) {
                            System.err.println(lineError);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the CMD command which creates 'Lambda Function'
     *
     * @return generated {@code String}
     */
    private String getCommand() {
        String result = "aws lambda create-function" +
                " --function-name " + functionName +
                " --region " + region +
                " --zip-file fileb://" + zipFile +
                " --role " + role +
                " --handler " + handler +
                " --runtime " + runtime +
                " --timeout " + timeout +
                " --memory-size " + memorySize;
        System.out.println(result);
        return result;
    }

    private String getDeleteCommand() {
        String result = "sudo aws lambda delete-function " +
                "--function-name " + functionName;
        return result;
    }

    /**
     * Creates Lambda Function on AWS and uploads the source code jar
     */
    void uploadFunction() {
        writeIntoCMD(getDeleteCommand());
        writeIntoCMD(getCommand());
    }


}
