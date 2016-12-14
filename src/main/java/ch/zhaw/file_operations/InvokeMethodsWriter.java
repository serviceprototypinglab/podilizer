package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;

import java.util.List;

public class InvokeMethodsWriter {
    private JavaProjectEntity oldProject;
    private String confPath;

    public InvokeMethodsWriter(JavaProjectEntity oldProject, String confPath) {
        this.oldProject = oldProject;
        this.confPath = confPath;
    }

    public void write() {
        List<ClassEntity> classEntityList = oldProject.getClassEntities();
        for (ClassEntity classEntity :
                classEntityList) {
            UtilityClass.makeAllMethodsPublic(classEntity);
            CompilationUnit cu = UtilityClass.translateClass(classEntity, confPath);
            UtilityClass.writeCuToFile(classEntity.getPath().toString(), cu);
        }
    }


}
