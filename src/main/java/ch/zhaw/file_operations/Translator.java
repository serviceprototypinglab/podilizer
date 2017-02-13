package ch.zhaw.file_operations;

import ch.zhaw.statistic.Parse;
import ch.zhaw.time.AnalysisTimer;
import ch.zhaw.time.DecompositionTimer;
import ch.zhaw.time.TranslationTimer;
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
        try {
            copyProject();
        } catch (IOException e) {
            System.err.print("In project folder is not found");
        }
        DecompositionTimer.start();
        JavaProjectEntity javaProjectEntityNew = new JavaProjectEntity(Paths.get(outPath));
        DecompositionTimer.stop();

        TranslationTimer.start();
        InvokeMethodsWriter invokeMethodsWriter = new InvokeMethodsWriter(javaProjectEntityNew, outPath);
        invokeMethodsWriter.write();

        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(inPath));
        SupportClassTreeCreator classTreeCreator = new SupportClassTreeCreator(javaProjectEntityOld, inPath, outPath);
        classTreeCreator.translate();
        TranslationTimer.stop();
    }
    private void copyProject() throws IOException {
        File outDirectory = new File(outPath);
        if (outDirectory.exists()){
            System.out.println("Out directory is already exists. Do you want to delete it? [y/n]: ");
            int string = System.in.read();
            switch (string) {
                case 'y':
                    FileUtils.deleteDirectory(outPath);
                    break;
                case 'n':
                    System.exit(0);
                default:
                    System.exit(0);
            }
        }

        AnalysisTimer.start();
        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(inPath));
        AnalysisTimer.stop();
        //fetch the parsing statistic
        Parse.setFilesNumber(javaProjectEntityOld.getAllClassEntities().size());
        Parse.setMethodsNumber(javaProjectEntityOld.getMethodEntities().size());

        packageUnpackaged(javaProjectEntityOld.getUnpackagedClasses());

        FileUtils.copyDirectoryStructure(new File(inPath), new File(outPath));
        //addLibs();
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
