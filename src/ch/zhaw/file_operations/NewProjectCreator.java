package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import com.sun.org.apache.xpath.internal.operations.Mod;
import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.ReferenceType;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NewProjectCreator {
    private String newPath;

    public NewProjectCreator(){
        newPath = ConfigReader.getConfig().getNewPath();
    }

    void copyProject() throws IOException {
        FileUtils.copyDirectoryStructure(new File(ConfigReader.getConfig().getPath()), new File(newPath));
    }
    void create() throws TooManyMainMethodsException {
        JavaProjectEntity javaProjectEntity = new JavaProjectEntity(Paths.get(ConfigReader.getConfig().getNewPath()));

        /*
        JarUploader jarUploader = new JarUploader(ConfigReader.getConfig().getFileName(), "/home/dord/LambdaA.zip", "example.LambdaA::handleRequest", 30, 1024);
        jarUploader.uploadFunction();
        */

        try{
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(javaProjectEntity.getMainClass().getPath().toString(), "UTF-8");
                System.out.println(createLambdaFunction(javaProjectEntity.getMainClass().getMainMethod()));
                writer.print(createLambdaFunction(javaProjectEntity.getMainClass().getMainMethod()));
                System.out.println("-------------------------------------------------------------");
                System.out.println(createInPutFile(javaProjectEntity.getMainClass().getMainMethod()));
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                writer.close();
            }

            /* Build jar

            JarBuilder jarBuilder = new JarBuilder();
            jarBuilder.createJar("/home/dord/Templates/emptyTestDirectory/");
            */

        }catch (TooManyMainMethodsException e){
            System.err.print("There is more then one main method, please define a path to the single project");
            System.exit(-1);
        }
    }
    CompilationUnit createLambdaFunction(MethodEntity methodEntity){
        CompilationUnit cu = methodEntity.getClassEntity().getCu();
        String name = cu.getPackage().getName() + "." + cu.getTypes().get(0).getName() + "." +
                methodEntity.getMethodDeclaration().getName() + methodEntity.getMethodDeclaration().getParameters().size();
        CompilationUnit newCU = new CompilationUnit();
        newCU.setImports(createImports(methodEntity));

        ClassOrInterfaceDeclaration classDeclaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, cu.getTypes().get(0).getName() + "AWSF" +
                        methodEntity.getMethodDeclaration().getName());
        List implementsList = new ArrayList();
        implementsList.add(new ClassOrInterfaceType("RequestHandler<InputType, OutputType>"));
        classDeclaration.setImplements(implementsList);
        ASTHelper.addTypeDeclaration(newCU, classDeclaration);
        MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, new ClassOrInterfaceType("InputType"), "handleRequest");
        ASTHelper.addMember(classDeclaration, method);

        Parameter param1 = ASTHelper.createParameter(new ClassOrInterfaceType("InputType"), "inputType");
        Parameter param2 = ASTHelper.createParameter(new ClassOrInterfaceType("Context"), "context");
        ASTHelper.addParameter(method, param1);
        ASTHelper.addParameter(method, param2);
        method.setBody(methodEntity.getMethodDeclaration().getBody());
        return newCU;
    }
    private CompilationUnit createInPutFile(MethodEntity methodEntity){
        CompilationUnit compilationUnit = methodEntity.getClassEntity().getCu();
        CompilationUnit inputCu = new CompilationUnit();
        ClassOrInterfaceDeclaration declaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "InputType");
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        ASTHelper.addTypeDeclaration(inputCu, declaration);
        for (FieldDeclaration field:
             fields) {
            ASTHelper.addMember(declaration, field);
        }
        return inputCu;

    }
    private List<ImportDeclaration> createImports(MethodEntity methodEntity){
        ArrayList<ImportDeclaration> imports = new ArrayList<>();
        ImportDeclaration imd1 = new ImportDeclaration();
        imd1.setName(new NameExpr("com.amazonaws.services.lambda.runtime.RequestHandler"));
        ImportDeclaration imd2 = new ImportDeclaration();
        imd2.setName(new NameExpr("com.amazonaws.services.lambda.runtime.Context"));
        ImportDeclaration imd3 = new ImportDeclaration();
        imd3.setName(new NameExpr("com.amazonaws.services.lambda.runtime.LambdaLogger"));
        imports.add(imd1);
        imports.add(imd2);
        imports.add(imd3);
        return imports;
    }
}
