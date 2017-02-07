package ch.zhaw.file_operations;

import ch.zhaw.statistic.Compile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Builder extends ProjectCreator{
    private String pomPath;

    public Builder(String outPath, String pomPath) {
        super(outPath);
        this.pomPath = pomPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public String getPomPath() {
        return pomPath;
    }

    public void build(){
        List<String> lambdaPathList = super.readDescriptor();
        for (String path :
                lambdaPathList) {
            JarBuilder jarBuilder = new JarBuilder(path);
            try {
                Files.copy(Paths.get(pomPath), Paths.get(path + "/pom.xml"), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            jarBuilder.createJar();
        }
        Compile.displayCompileStatistic(lambdaPathList.size());
    }
}
