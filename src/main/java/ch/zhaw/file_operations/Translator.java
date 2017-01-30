package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.expr.NameExpr;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Translator {
    private String inPath;
    private String outPath;

    public Translator(String inPath, String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public String getInPath() {
        return inPath;
    }

    public String getOutPath() {
        return outPath;
    }
    public void translate(){
        // TODO: 1/16/17 handle missing in project exception
        // TODO: 1/18/17 if the outFolder is already exist ask about rewriting
        try {
            copyProject();
        } catch (IOException e) {
            System.err.print("In project folder is not found");
        }
        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(inPath));
        SupportClassTreeCreator classTreeCreator = new SupportClassTreeCreator(javaProjectEntityOld, inPath, outPath);
        classTreeCreator.translate();
    }
    private void copyProject() throws IOException {
        FileUtils.deleteDirectory(outPath);

        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(inPath));
        packageUnpackaged(javaProjectEntityOld.getUnpackagedClasses());

        FileUtils.copyDirectoryStructure(new File(inPath), new File(outPath));
        //addLibs();
        JavaProjectEntity javaProjectEntityNew = new JavaProjectEntity(Paths.get(outPath));
        InvokeMethodsWriter invokeMethodsWriter = new InvokeMethodsWriter(javaProjectEntityNew, outPath);
        invokeMethodsWriter.write();
    }
    private void packageUnpackaged(List<ClassEntity> classes) throws IOException {
        for (ClassEntity classEntity :
                classes) {
            CompilationUnit cu = classEntity.getCu();
            cu.setPackage(new PackageDeclaration(new NameExpr(Constants.EXTRA_PACKAGE)));
            UtilityClass.writeCuToFile(classEntity.getPath().toString(), cu);
            String classPath = classEntity.getPath().toString();
            Path tmp = Paths.get(classPath);
            String packagePath = classPath.substring(0, classPath.length() -
                    tmp.getFileName().toString().length()) + "/" + Constants.EXTRA_PACKAGE;
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
        srcClass.delete();
    }
}
