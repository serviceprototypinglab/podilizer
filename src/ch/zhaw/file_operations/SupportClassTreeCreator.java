package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.ObjectCreationExpr;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.getInputClass;
import static ch.zhaw.file_operations.UtilityClass.getOutputClass;
import static ch.zhaw.file_operations.UtilityClass.writeCuToFile;

public class SupportClassTreeCreator {
    JavaProjectEntity projectEntity;
    String string;

    public SupportClassTreeCreator(JavaProjectEntity projectEntity) {
        this.projectEntity = projectEntity;
    }
    public void sameNameFieldTest(String string, int projectEntity){
        int o = 0;
        String m = " ";
    }

    private List<String> create(){
        try {
            projectEntity = (JavaProjectEntity)projectEntity.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        List<String> lambdaPathList = new ArrayList<>();
        List<ClassEntity> classEntityList = excludeInners(projectEntity.getClassEntities());
//        List<ClassEntity> copyClassList = new JavaProjectEntity(Paths.get(ConfigReader.getConfig().getPath())).getClassEntities();
        List<ClassEntity> copyClassList = excludeInners(new JavaProjectEntity(Paths.get(ConfigReader.getConfig().getPath())).getClassEntities());

        int i = 0;
        for (ClassEntity classEntity :
                classEntityList) {
//            System.out.println(classEntity.getCu());
            List<MethodEntity> methodEntityList = classEntity.getFunctions();
            CompilationUnit translatedClass = UtilityClass.translateClass(copyClassList.get(i));
            for (MethodEntity methodEntity :
                    methodEntityList) {
                if (!(methodEntity.getMethodDeclaration().getParentNode() instanceof ObjectCreationExpr)){
                    MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();

                    //if the method has more then one lien of code
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
                        writeCuToFile(path + "/OutputType.java", getOutputClass(methodEntity, false));
                        writeCuToFile(path + "/InputType.java", getInputClass(methodEntity, false));

                        String pathLambdaProject = "" + ConfigReader.getConfig().getNewPath() +
                                "/LambdaProjects/" + packageName + "/" + className + "/" + functionName;
                        NewProjectCreator projectCreator = new NewProjectCreator();
                        File file1 = new File(pathLambdaProject);
                        file1.mkdirs();
                        JarBuilder jarBuilder = new JarBuilder();
                        try {
                            jarBuilder.createProjTree(pathLambdaProject);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String classPath = pathLambdaProject + "/src/main/java";
                        lambdaPathList.add(pathLambdaProject);
                        String lambdaPath = classPath + "/" + Constants.FUNCTION_PACKAGE + "/";
                        File lambdaDir = new File(lambdaPath);
                        lambdaDir.mkdir();
                        writeCuToFile(lambdaPath + "/OutputType.java", getOutputClass(methodEntity, true));
                        writeCuToFile(lambdaPath + "/InputType.java", getInputClass(methodEntity, true));
                        CompilationUnit cuToWrite = projectCreator.createLambdaFunction(methodEntity,
                                translatedClass);
                        writeCuToFile(lambdaPath + "/LambdaFunction.java", cuToWrite);
                    }
                }

            }
            i++;
        }
        return lambdaPathList;
    }
    public void build(){
        List<String> lambdaPathList = create();
        JarBuilder jarBuilder = new JarBuilder();
        String suppClassTreePath;
        for (String path :
                lambdaPathList) {
            try {
                suppClassTreePath = path + "/src/main/java/";
                FileUtils.copyDirectoryStructure(new File(ConfigReader.getConfig().getNewPath() + "/src/"),
                        new File(suppClassTreePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            jarBuilder.createJar(path);
        }
    }
    /**
    excludes compilation units which have inner classes from List<ClassEntity>
     */
    private List<ClassEntity> excludeInners(List<ClassEntity> list){
        List<ClassEntity> result = new ArrayList<>();
        for (ClassEntity classEntity :
                list) {
            CompilationUnit cu = classEntity.getCu();
            int i = 0;
            for (Node node :
                    cu.getTypes().get(0).getChildrenNodes()) {
                if (node instanceof ClassOrInterfaceDeclaration) {
                    i++;
                }
            }
            if (i == 0){
                result.add(classEntity);
            }
        }
        return result;
    }
}
