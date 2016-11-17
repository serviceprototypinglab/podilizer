package ch.zhaw.file_operations;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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

                    String pathLambda = "" + ConfigReader.getConfig().getNewPath() +
                            "/LambdaProjects/" + packageName + "/" + className + "/" + functionName;
                    NewProjectCreator projectCreator = new NewProjectCreator();
                    File file1 = new File(pathLambda);
                    file1.mkdirs();
                    JarBuilder jarBuilder = new JarBuilder();
//                    System.out.println(methodEntity.getClassEntity().getCu().getTypes().get(0).getName() +  " - class located -" +
//                            packageName);
                    String classPath = "";
                    try {
                        classPath = jarBuilder.createProjTree(pathLambda);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        FileUtils.copyDirectoryStructure(new File(ConfigReader.getConfig().getPath() + "/src/"),
                                new File(classPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    classPath = classPath + "/" + packageName;
                    writeToFile(classPath + "/OutputType.java", getOutputClass(methodEntity));
                    writeToFile(classPath + "/InputType.java", getInputClass(methodEntity));
                    writeToFile(classPath + "/LambdaFunction.java", projectCreator.createLambdaFunction(methodEntity));
                    ClassOrInterfaceDeclaration parentClass =
                            (ClassOrInterfaceDeclaration)methodEntity.getMethodDeclaration().getParentNode();
                    System.out.println(parentClass.getName());
                    try {
                        jarBuilder.mvnBuild(pathLambda);
                    } catch (MavenInvocationException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }


                }
            }
        }
    }
}
