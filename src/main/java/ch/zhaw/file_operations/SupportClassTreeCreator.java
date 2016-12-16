package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.ObjectCreationExpr;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.getInputClass;
import static ch.zhaw.file_operations.UtilityClass.getOutputClass;
import static ch.zhaw.file_operations.UtilityClass.writeCuToFile;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SupportClassTreeCreator {
    private JavaProjectEntity projectEntity;
    private String oldPath;
    private String newPath;
    private String confPath;

    public SupportClassTreeCreator(JavaProjectEntity projectEntity, String oldPath, String newPath, String confPath) {
        this.projectEntity = projectEntity;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.confPath = confPath;
    }

    private List<String> create() {

        List<String> lambdaPathList = new ArrayList<>();
        List<ClassEntity> classEntityList = excludeInners(projectEntity.getClassEntities());
        List<ClassEntity> copyClassList = excludeInners(new JavaProjectEntity(Paths.get(oldPath)).getClassEntities());

        int i = 0;
        for (ClassEntity classEntity :
                classEntityList) {
            List<MethodEntity> methodEntityList = classEntity.getFunctions();
            CompilationUnit translatedClass = UtilityClass.translateClass(copyClassList.get(i), confPath);
            for (MethodEntity methodEntity :
                    methodEntityList) {
                if (!(methodEntity.getMethodDeclaration().getParentNode() instanceof ObjectCreationExpr)) {
                    MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();

                    //if it's not 'get' ot 'set' method
                    if (!isAccessMethod(methodDeclaration)) {
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
                        writeCuToFile(lambdaPath + "/OutputType.java", getOutputClass(methodEntity, true));
                        writeCuToFile(lambdaPath + "/InputType.java", getInputClass(methodEntity, true));
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

    public void build(boolean uploadFlag) {
        List<String> lambdaPathList = create();
        String suppClassTreePath;
        for (String path :
                lambdaPathList) {
            try {
                suppClassTreePath = path + "/src/main/java/";
                FileUtils.copyDirectoryStructure(new File(newPath + "/src/"),
                        new File(suppClassTreePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            JarBuilder jarBuilder = new JarBuilder(path);
            jarBuilder.createJar();
            if (uploadFlag) {
                JarUploader jarUploader = new JarUploader(UtilityClass.generateLambdaName(path, newPath),
                        path + "/target/lambda-java-example-1.0-SNAPSHOT.jar",
                        Constants.FUNCTION_PACKAGE + ".LambdaFunction::handleRequest", 60, 1024, confPath);
                jarUploader.uploadFunction();
                // TODO: 12/2/16 Fix the problem with missing information when function is uploading
            }
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
        File file = new File(path);
        file.mkdir();
        Files.copy(Paths.get(confPath + "/pom.xml"), Paths.get(path + "/pom.xml"), REPLACE_EXISTING);
        file = new File(path + "/src/main/java");
        file.mkdirs();
        return path + "/src/main/java";
    }
}
