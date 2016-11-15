package ch.zhaw.file_operations;

import japa.parser.ast.body.MethodDeclaration;

import java.io.File;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.getInputClass;
import static ch.zhaw.file_operations.UtilityClass.getOutputClass;
import static ch.zhaw.file_operations.UtilityClass.writeToFile;

public class SupportClassTreeCreator {
    JavaProjectEntity projectEntity;

    public SupportClassTreeCreator(JavaProjectEntity projectEntity) {
        this.projectEntity = projectEntity;
    }

    public void create(){
        List<ClassEntity> classEntityList = projectEntity.getClassEntities();
        for (ClassEntity classEntity :
                classEntityList) {
            List<MethodEntity> methodEntityList = classEntity.getFunctions();
            for (MethodEntity methodEntity :
                    methodEntityList) {
                MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();
                int methodBodyLength = methodDeclaration.getBody().getStmts().size();
                if (methodBodyLength > 1){
                    String packageName = "";
                    if(classEntity.getCu().getPackage() != null){
                        packageName = classEntity.getCu().getPackage().getName().toString();
                        packageName = packageName.replace('.', '/');
                    }
                    String className = classEntity.getCu().getTypes().get(0).getName();
                    String functionName = "" + methodDeclaration.getName();
                    if (methodDeclaration.getParameters() != null){
                        functionName = functionName + methodDeclaration.getParameters().size();
                    }
                    String path = "" + ConfigReader.getConfig().getNewPath() +
                            "/src/awsl/" + packageName + "/" + className + "/" + functionName;
                    File file = new File(path);
                    file.mkdirs();
                    writeToFile(path + "/OutputType.java", getOutputClass(methodEntity));
                    writeToFile(path + "/InputType.java", getInputClass(methodEntity));
                }
            }
        }
    }
}
