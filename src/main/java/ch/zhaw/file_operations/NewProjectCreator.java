package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.expr.NameExpr;
import org.codehaus.plexus.util.FileUtils;
import java.io.*;
import java.nio.file.Paths;
import java.util.List;

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

        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(oldPath));
        packageUnpackaged(javaProjectEntityOld.getUnpackagedClasses());

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

    private void packageUnpackaged(List<ClassEntity> classes) throws IOException {
        for (ClassEntity classEntity :
                classes) {
            CompilationUnit cu = classEntity.getCu();
            cu.setPackage(new PackageDeclaration(new NameExpr(Constants.EXTRA_PACKAGE)));
            UtilityClass.writeCuToFile(classEntity.getPath().toString(), cu);
            String classPath = classEntity.getPath().toString();
            String packagePath = classPath.substring(0, classPath.length() -
                    classEntity.getCu().getTypes().get(0).getName().length() - 5) + Constants.EXTRA_PACKAGE;
            File newPackage = new File(packagePath);
            if (newPackage.exists() & newPackage.isDirectory()) {
                packager(classPath, packagePath);
            } else {
                if (!newPackage.mkdir()){
                    System.out.println("couldn't create a package! " + packagePath);
                    System.exit(0);
                }
                packager(classPath, packagePath);
            }
        }
    }
    private void packager(String file, String packagePath) throws IOException {
        File srcClass = new File(file);
        FileUtils.copyFileToDirectory(srcClass, new File(packagePath));
        if (srcClass.exists()){
            System.out.println(srcClass.getAbsolutePath() + " exists");
        }
        if (srcClass.delete()){
            System.out.println("deleted " + srcClass.getAbsolutePath());
        }
        if (srcClass.exists()){
            System.out.println(srcClass.getAbsolutePath() + " exists\n" +
                    "-----------------------");
        }
        //FileUtils.forceDelete(file);
    }
}
