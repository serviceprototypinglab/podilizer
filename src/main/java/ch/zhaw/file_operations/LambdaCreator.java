package ch.zhaw.file_operations;

import ch.zhaw.statistic.Upload;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
        
        //upload statistic fetching
        Upload.setBuiltProjectsNumber(lambdaPathList.size());
        JarUploader jarUploader = null;
        for (String path :
                lambdaPathList) {
                jarUploader = new JarUploader(UtilityClass.generateLambdaName(path, outPath),
                        path + "/target/lambda-java-example-1.0-SNAPSHOT.jar",
                        Constants.FUNCTION_PACKAGE + ".LambdaFunction::handleRequest", 60, 1024, confPath);
                jarUploader.uploadFunction();

                // TODO: 12/2/16 Fix the problem with missing information when function is uploading
        }
        try {
            //create credentials config file in output project for local invocation(see InvokeMethodCreator.java)
            AwsCredentialsReader awsCredentialsReader = new AwsCredentialsReader();
            awsCredentialsReader.read();
            File outConf = new File(outPath + "/jyaml.yml");
            Writer fileWriter = new FileWriter(outConf);
            fileWriter.write("awsRegion: " + awsCredentialsReader.getRegion() + "\n");
            fileWriter.write("awsRole: " + jarUploader.getRole() + "\n");
            fileWriter.write("awsAccessKeyId: " + awsCredentialsReader.getAwsAccessKeyId() + "\n");
            fileWriter.write("awsSecretAccessKey: " + awsCredentialsReader.getAwsSecretAccessKey() + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
