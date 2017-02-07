package ch.zhaw.file_operations;

import org.codehaus.plexus.util.FileUtils;

import java.io.IOException;
import java.util.List;

public class LambdaCreator{
    private String outPath;
    private String confPath;

    public LambdaCreator(String outPath, String confPath) {
        this.outPath = outPath;
        this.confPath = confPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public String getConfPath() {
        return confPath;
    }
    public void create(){
        List<String> lambdaPathList = DescriptorCreator.readDescriptor(outPath, Constants.BUILT_DESCRIPTOR_NAME);
        for (String path :
                lambdaPathList) {
            // TODO: 1/17/17 handle the missing of built projects
                JarUploader jarUploader = new JarUploader(UtilityClass.generateLambdaName(path, outPath),
                        path + "/target/lambda-java-example-1.0-SNAPSHOT.jar",
                        Constants.FUNCTION_PACKAGE + ".LambdaFunction::handleRequest", 60, 1024, confPath);
                jarUploader.uploadFunction();

                // TODO: 12/2/16 Fix the problem with missing information when function is uploading
        }
        try {
            FileUtils.copyFileToDirectory(confPath, outPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
