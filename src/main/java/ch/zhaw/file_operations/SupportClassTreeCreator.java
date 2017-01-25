package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.ObjectCreationExpr;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.getInputClass;
import static ch.zhaw.file_operations.UtilityClass.getOutputClass;
import static ch.zhaw.file_operations.UtilityClass.writeCuToFile;

public class SupportClassTreeCreator {
    private JavaProjectEntity projectEntity;
    private String oldPath;
    private String newPath;

    public SupportClassTreeCreator(JavaProjectEntity projectEntity, String oldPath, String newPath) {
        this.projectEntity = projectEntity;
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    private List<String> create() {

        List<String> lambdaPathList = new ArrayList<>();
        List<ClassEntity> classEntityList = excludeInners(projectEntity.getClassEntities());
        List<ClassEntity> copyClassList = excludeInners(new JavaProjectEntity(Paths.get(oldPath)).getClassEntities());

        int i = 0;
        for (ClassEntity classEntity :
                classEntityList) {
            List<MethodEntity> methodEntityList = classEntity.getFunctions();
            CompilationUnit translatedClass = UtilityClass.translateClass(copyClassList.get(i), "");
            for (MethodEntity methodEntity :
                    methodEntityList) {
                if (!(methodEntity.getMethodDeclaration().getParentNode() instanceof ObjectCreationExpr)) {
                    MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();

                    //if it's not 'get' or 'set' method
                    if (!isAccessMethod(methodDeclaration) &
                            !((methodDeclaration.getBody() == null) || (methodDeclaration.getBody().getStmts() == null))) {
                        String packageName = "";
                        if (classEntity.getCu().getPackage() != null) {
                            packageName = classEntity.getCu().getPackage().getName().toString();
                            packageName = packageName.replace('.', '/');
                        }
                        String className = classEntity.getCu().getTypes().get(0).getName();
                        String functionName = "" + methodDeclaration.getName();
                        if (methodDeclaration.getParameters() != null) {
                            functionName = functionName + methodDeclaration.getParameters().size();
                        }
                        String path = "" + newPath +
                                "/src/awsl/" + packageName + "/" + className + "/" + functionName;
                        File file = new File(path);
                        file.mkdirs();
                        writeCuToFile(newPath + "/src/awsl/AWSConfEntity.java", UtilityClass.createConfigEntity());
                        writeCuToFile(path + "/OutputType.java", getOutputClass(methodEntity, false));
                        writeCuToFile(path + "/InputType.java", getInputClass(methodEntity, false));

                        String pathLambdaProject = "" + newPath +
                                "/LambdaProjects/" + packageName + "/" + className + "/" + functionName;
                        File file1 = new File(pathLambdaProject);
                        file1.mkdirs();
                        try {
                            createProjTree(pathLambdaProject);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String classPath = pathLambdaProject + "/src/main/java";
                        lambdaPathList.add(pathLambdaProject);
                        String lambdaPath = classPath + "/" + Constants.FUNCTION_PACKAGE + "/";
                        File lambdaDir = new File(lambdaPath);
                        lambdaDir.mkdir();
                        LambdaFunction lambdaFunction = new LambdaFunction(methodEntity, translatedClass);
                        lambdaFunction.create();
                        CompilationUnit outputCU = getOutputClass(methodEntity, true);
                        writeCuToFile(lambdaPath + "/OutputType.java", outputCU);
                        CompilationUnit inputCU = getInputClass(methodEntity, true);
                        writeCuToFile(lambdaPath + "/InputType.java", inputCU);
                        CompilationUnit cuToWrite = lambdaFunction.getNewCU();
                        writeCuToFile(lambdaPath + "/LambdaFunction.java", cuToWrite);
                    }
                }

            }
            i++;
        }
        return lambdaPathList;
    }
    private boolean isAccessMethod(MethodDeclaration methodDeclaration){
        String str = methodDeclaration.getName().substring(0, 3);
        if (str.equals("set") | str.equals("get")){
            if (methodDeclaration.getBody().getStmts().size() < 2){
                return true;
            }
        }
        return false;
    }
    public void translate(){
        List<String> lambdaPathList = create();
        createDescriptor(lambdaPathList);
        String suppClassTreePath;
        for (String path :
                lambdaPathList) {
            suppClassTreePath = path + "/src/main/java/";
            writeSupportClasses(suppClassTreePath);
        }
    }
    private void writeSupportClasses(String path){
        JavaProjectEntity javaProjectEntity = new JavaProjectEntity(Paths.get(newPath));
        for (ClassEntity classEntity :
                javaProjectEntity.getAllClassEntities()) {
            String cuPath = classEntity.getPath().toString();
            cuPath = cuPath.substring(newPath.length(), cuPath.length());
            if (!cuPath.startsWith("/LambdaProjects/")){
                CompilationUnit cu = classEntity.getCu();
                String packagePath = cu.getPackage().getName().toString();
                packagePath = packagePath.replace('.', '/');
                String absolutePath = path + packagePath + "/";
                File file = new File(absolutePath);
                if (!file.exists()){
                    file.mkdirs();
                }
                writeCuToFile(absolutePath + classEntity.getPath().toFile().getName(), cu);
            }
        }
    }
    private void createDescriptor(List<String> paths){
        try {
            FileWriter fileWriter = new FileWriter(newPath + "/PodilizerDescriptor.txt");
            for (String str :
                    paths) {
                fileWriter.write(str + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * excludes compilation units which have inner classes from List<ClassEntity>
     */
    private List<ClassEntity> excludeInners(List<ClassEntity> list) {
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
            if (i == 0) {
                result.add(classEntity);
            }
        }
        return result;
    }

    /**
     * Creates project tree and pom-file for the 'Lambda function' maven project
     *
     * @return the {@code String} which represents path for code in the result maven project
     * @throws IOException
     */
    public String createProjTree(String path) throws IOException {
        String projectSourceFolder = "/src/main/java";
        File file = new File(path);
        file.mkdir();
        file = new File(path + projectSourceFolder);
        file.mkdirs();
        return path + projectSourceFolder;
    }
}
