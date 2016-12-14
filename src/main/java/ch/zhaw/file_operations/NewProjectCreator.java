package ch.zhaw.file_operations;

import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Paths;

public class NewProjectCreator {
    private String newPath;
    private String oldPath;
    private String confPath;
    private boolean uploadingFlag;

    public NewProjectCreator(String resultDir, boolean uploadingFlag, String confPath) {
        oldPath = new File(".").getAbsolutePath();
        newPath = resultDir;
        this.confPath = confPath;
        this.uploadingFlag = uploadingFlag;

    }

    public NewProjectCreator(String sourceDir, String resultDir, boolean uploadingFlag, String confPath) {
        oldPath = sourceDir;
        newPath = resultDir;
        this.confPath = confPath;
        this.uploadingFlag = uploadingFlag;
    }

    public NewProjectCreator(boolean uploadingFlag, String confPath) {
        this.confPath = confPath;
        newPath = ConfigReader.getConfig(confPath).getNewPath();
        oldPath = ConfigReader.getConfig(confPath).getPath();
        this.uploadingFlag = uploadingFlag;
    }

    void copyProject() throws IOException {
        FileUtils.deleteDirectory(newPath);
        FileUtils.copyDirectoryStructure(new File(oldPath), new File(newPath));
        //addLibs();
        JavaProjectEntity javaProjectEntityNew = new JavaProjectEntity(Paths.get(newPath));
        InvokeMethodsWriter invokeMethodsWriter = new InvokeMethodsWriter(javaProjectEntityNew, confPath);
        invokeMethodsWriter.write();
        create();
    }

    private void addLibs() throws IOException {
        File libDir = new File(newPath + "/libs");
        libDir.mkdir();
        FileUtils.copyDirectoryStructure(new File(confPath + "/libs/"), new File(newPath + "/libs/"));
    }

    private void create() {
        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(oldPath));
        SupportClassTreeCreator classTreeCreator = new SupportClassTreeCreator(javaProjectEntityOld, oldPath, newPath, confPath);
        classTreeCreator.build(uploadingFlag);

    }
}
