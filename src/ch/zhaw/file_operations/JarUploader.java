package ch.zhaw.file_operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class JarUploader {
    private String region = "us-west-2";
    private String runtime = "java8";
    private String role = "arn:aws:iam::520974061566:role/lambda_basic_execution";
    private String functionName;
    private String zipFile;
    private String handler;
    private int timeout;
    private int memorySize;

    JarUploader(String functionName, String zipFile, String handler, int timeout, int memorySize) {
        this.functionName = functionName;
        this.zipFile = zipFile;
        this.handler = handler;
        this.timeout = timeout;
        this.memorySize = memorySize;
    }
    void writeIntoCMD(String command){
        Runtime runtime = Runtime.getRuntime();
        try {
            final Process process = runtime.exec(command);
            new Thread(new Runnable() {
                public void run() {
                    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    try {
                        while ((line = input.readLine()) != null)
                            System.out.println(line);
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
    String getCommand(){
        String result = "";
        result = "aws lambda create-function" +
                " --function-name " + functionName +
                " --region " + region +
                " --zip-file fileb://" + zipFile +
                " --role " + role +
                " --handler " + handler +
                " --runtime " + runtime +
                " --timeout " + timeout +
                " --memory-size " + memorySize;
        return result;
    }
    void uploadFunction(){
        writeIntoCMD(getCommand());
    }


}
