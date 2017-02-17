package ch.zhaw.file_operations;

import ch.zhaw.statistic.Compile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Builder {
    private String outPath;
    private String pomPath;

    public Builder(String outPath, String pomPath) {
        this.outPath = outPath;
        this.pomPath = pomPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public String getPomPath() {
        return pomPath;
    }

    public void build(){
        List<String> lambdaPathList = DescriptorCreator.readDescriptor(outPath, Constants.TRANSLATED_DESCRIPTOR_NAME);
        List<String> jarPathList = new ArrayList<>();
        for (String path :
                lambdaPathList) {
            JarBuilder jarBuilder = new JarBuilder(path);
            try {
                Files.copy(Paths.get(pomPath), Paths.get(path + "/pom.xml"), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String builtPath = jarBuilder.mvnBuild();
            if (builtPath != null){
                jarPathList.add(builtPath);
            }
        }
        DescriptorCreator.createDescriptor(jarPathList, outPath, Constants.BUILT_DESCRIPTOR_NAME);
        Compile.setTranslatedProjectsNumber(lambdaPathList.size());
    }
}
