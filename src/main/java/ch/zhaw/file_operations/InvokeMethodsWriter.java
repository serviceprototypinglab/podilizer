package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;

import java.util.List;

public class InvokeMethodsWriter {
    private JavaProjectEntity oldProject;
    private String newPath;

    public InvokeMethodsWriter(JavaProjectEntity oldProject, String newPath) {
        this.oldProject = oldProject;
        this.newPath = newPath;
    }

    public void write() {
        List<ClassEntity> classEntityList = oldProject.getClassEntities();
        for (ClassEntity classEntity :
                classEntityList) {
            UtilityClass.makeAllMethodsPublic(classEntity);
            UtilityClass.makeConstructorsPublic(classEntity);
            UtilityClass.makeClassPublic(classEntity.getCu());
            UtilityClass.addJsonAnnotations(classEntity);
            CompilationUnit cu = UtilityClass.translateClass(classEntity, newPath);
            UtilityClass.writeCuToFile(classEntity.getPath().toString(), cu);
        }
    }


}
