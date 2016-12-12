package ch.zhaw.file_operations;

import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Paths;

public class NewProjectCreator {
    private String newPath;
    private String oldPath;
    private boolean uploadingFlag;

    public NewProjectCreator(boolean uploadingFlag) {
        newPath = ConfigReader.getConfig().getNewPath();
        oldPath = ConfigReader.getConfig().getPath();
        this.uploadingFlag = uploadingFlag;
    }

    void copyProject() throws IOException {
        FileUtils.deleteDirectory(newPath);
        FileUtils.copyDirectoryStructure(new File(oldPath), new File(newPath));
        addLibs();
        JavaProjectEntity javaProjectEntityNew = new JavaProjectEntity(Paths.get(newPath));
        InvokeMethodsWriter invokeMethodsWriter = new InvokeMethodsWriter(javaProjectEntityNew);
        invokeMethodsWriter.write();
        create();
    }

    private void addLibs() throws IOException {
        File libDir = new File(newPath + "/libs");
        libDir.mkdir();
        FileUtils.copyDirectoryStructure(new File("additional/libs/"), new File(newPath + "/libs/"));
    }

    private void create() {
        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(oldPath));
        SupportClassTreeCreator classTreeCreator = new SupportClassTreeCreator(javaProjectEntityOld);
        classTreeCreator.build(uploadingFlag);

    }
}
