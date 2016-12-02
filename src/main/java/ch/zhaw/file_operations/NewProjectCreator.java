package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.*;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.ReadOnlyBufferException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.*;

public class NewProjectCreator {
    private String newPath;
    private String oldPath;
    private boolean uploadingFlag;

    public NewProjectCreator(boolean uploadingFlag){
        newPath = ConfigReader.getConfig().getNewPath();
        oldPath = ConfigReader.getConfig().getPath();
        this.uploadingFlag = uploadingFlag;
    }

    void copyProject() throws IOException, TooManyMainMethodsException {
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
    private void create() throws TooManyMainMethodsException {
        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(oldPath));
        SupportClassTreeCreator classTreeCreator = new SupportClassTreeCreator(javaProjectEntityOld);
        classTreeCreator.build(uploadingFlag);

    }
}
