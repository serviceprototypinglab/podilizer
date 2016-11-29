package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;

import java.util.List;

public class InvokeMethodsWriter {
    private JavaProjectEntity oldProject;

    public InvokeMethodsWriter(JavaProjectEntity oldProject) {
        this.oldProject = oldProject;
    }
    public void write(){
        JavaProjectEntity resultProject = null;
        try {
            resultProject = (JavaProjectEntity)oldProject.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        List<ClassEntity> classEntityList = resultProject.getClassEntities();
        for (ClassEntity classEntity :
                classEntityList) {
            UtilityClass.makeAllMethodsPublic(classEntity);
            CompilationUnit cu = UtilityClass.translateClass(classEntity);
            UtilityClass.writeCuToFile(classEntity.getPath().toString(), cu);
        }
    }


}
