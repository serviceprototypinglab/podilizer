package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.*;

public class NewProjectCreator {
    private String newPath;
    private String oldPath;
    private static String testField;

    public NewProjectCreator(){
        newPath = ConfigReader.getConfig().getNewPath();
        oldPath = ConfigReader.getConfig().getPath();
    }

    void copyProject() throws IOException {
        FileUtils.copyDirectoryStructure(new File(ConfigReader.getConfig().getPath()), new File(newPath));
    }
    void create() throws TooManyMainMethodsException {
        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(oldPath));
        JavaProjectEntity javaProjectEntityNew = new JavaProjectEntity(Paths.get(newPath));
        SupportClassTreeCreator classTreeCreator = new SupportClassTreeCreator(javaProjectEntityOld);
        classTreeCreator.create();

        /*
        JarUploader jarUploader = new JarUploader(ConfigReader.getConfig().getFileName(), "/home/dord/LambdaA.zip", "example.LambdaA::handleRequest", 30, 1024);
        jarUploader.uploadFunction();
        */

        try{
            JarBuilder jarBuilder = new JarBuilder();

            jarBuilder.creatDir("LambdaProjects");
            String classesPath = jarBuilder.createProjTree("LambdaProjects/NewProjectCreatorAWSFfirstLetterToUpperCase");

            writeToFile(classesPath + "/NewProjectCreatorAWSFfirstLetterToUpperCase.java",
                    createLambdaFunction(javaProjectEntityOld.getStaticMethods().get(3)));
            writeToFile(classesPath + "/OutputType.java",
                    getOutputClass(javaProjectEntityOld.getStaticMethods().get(3)));
            writeToFile(classesPath + "/InputType.java",
                    getInputClass(javaProjectEntityOld.getStaticMethods().get(3)));

            System.out.println(javaProjectEntityOld.getStaticMethods().get(3).getMethodDeclaration());
            System.out.println(javaProjectEntityOld.getStaticMethods().get(3).getClassEntity().getPath());

            InvokeMethodCreator invokeMethodCreator = new InvokeMethodCreator();
            invokeMethodCreator.createMethodInvoker(javaProjectEntityOld.getStaticMethods().get(3));
            System.out.println(javaProjectEntityOld.getStaticMethods().get(3).getMethodDeclaration());
            System.out.println("=====================================================================================");
            System.out.println(invokeMethodCreator.addBufferByteReaderMethod(javaProjectEntityOld.getStaticMethods().get(3).getClassEntity().getCu()));

            //jarBuilder.mvnBuild("LambdaProjects/NewProjectCreatorAWSFfirstLetterToUpperCase/");


        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (MavenInvocationException e) {
//            e.printStackTrace();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
    }

    private CompilationUnit createLambdaFunction(MethodEntity methodEntity){
        CompilationUnit cu = methodEntity.getClassEntity().getCu();
        List<FieldDeclaration> staticFields = new ArrayList<>();
        CompilationUnit newCU = new CompilationUnit();
        newCU.setImports(createImports());

        ClassOrInterfaceDeclaration classDeclaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, cu.getTypes().get(0).getName() + "AWSF" +
                        methodEntity.getMethodDeclaration().getName());
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        for (FieldDeclaration field :
                fields) {
            boolean isStaticNonFinal =
                    field.getModifiers() == ModifierSet.STATIC && field.getModifiers() != ModifierSet.FINAL;
            if (isStaticNonFinal){
                staticFields.add(field);
                ASTHelper.addMember(classDeclaration, field);
            }

        }
        List implementsList = new ArrayList();
        implementsList.add(new ClassOrInterfaceType("RequestHandler<InputType, OutputType>"));
        classDeclaration.setImplements(implementsList);
        ASTHelper.addTypeDeclaration(newCU, classDeclaration);
        MethodDeclaration method =
                new MethodDeclaration(ModifierSet.PUBLIC, new ClassOrInterfaceType("OutputType"), "handleRequest");
        ASTHelper.addMember(classDeclaration, method);

        Parameter param1 = ASTHelper.createParameter(new ClassOrInterfaceType("InputType"), "inputType");
        Parameter param2 = ASTHelper.createParameter(new ClassOrInterfaceType("Context"), "context");
        ASTHelper.addParameter(method, param1);
        ASTHelper.addParameter(method, param2);
        BlockStmt bodyBlock = new BlockStmt();

        for (FieldDeclaration staticField:
                staticFields){
            for (VariableDeclarator var:
                    staticField.getVariables()) {
                NameExpr staticFieldVar = new NameExpr(var.getId().getName());
                MethodCallExpr methodCallExpr =
                        new MethodCallExpr(new NameExpr("inputType"), "get" +
                                firstLetterToUpperCase(var.getId().getName()));
                AssignExpr assignExpr = new AssignExpr(staticFieldVar, methodCallExpr, AssignExpr.Operator.assign);
                ASTHelper.addStmt(bodyBlock, assignExpr);
            }
        }
        List<Parameter> parameters = methodEntity.getMethodDeclaration().getParameters();
        if (parameters != null){
            for (Parameter parameter :
                    parameters) {
                NameExpr localVar = new NameExpr(parameter.getType().toString() + " " + parameter.getId().getName());
                MethodCallExpr methodCallExpr =
                        new MethodCallExpr(new NameExpr("inputType"), "get" +
                                firstLetterToUpperCase(parameter.getId().getName()));
                AssignExpr assignExpr = new AssignExpr(localVar, methodCallExpr, AssignExpr.Operator.assign);
                ASTHelper.addStmt(bodyBlock, assignExpr);
            }
        }
        method.setBody(bodyBlock);
        if (methodEntity.getMethodDeclaration().getType().equals(ASTHelper.VOID_TYPE)){
            ASTHelper.addStmt(bodyBlock, addReturnCode(methodEntity, staticFields));
        }else {
            ASTHelper.addStmt(bodyBlock, returnReplace(methodEntity, staticFields));
        }
        return newCU;
    }
    private BlockStmt returnReplace(MethodEntity methodEntity, List<FieldDeclaration> staticFields){
        BlockStmt bodyBlock = methodEntity.getMethodDeclaration().getBody();
        List<Statement> statements = new ArrayList<>(bodyBlock.getStmts());
        for (Statement statement :
                statements) {
            if (statement.toString().contains("return")){
                String returnVar = statement.toString().substring(7, (statement.toString().length() - 1));
                Expression outputTypeExpr = new NameExpr("OutputType outputType");
                List<Expression> arguments = new ArrayList<>();
                for (FieldDeclaration field:
                        staticFields) {
                    for (VariableDeclarator var :
                            field.getVariables()) {
                        arguments.add(new NameExpr(var.getId().getName()));
                    }
                }
                arguments.add(new NameExpr(returnVar));
                ClassOrInterfaceType type = new ClassOrInterfaceType("OutputType");

                ObjectCreationExpr objectCreationExpr =
                        new ObjectCreationExpr(null, type, arguments);
                AssignExpr assign = new AssignExpr(outputTypeExpr, objectCreationExpr, AssignExpr.Operator.assign);
                NameExpr returnExpr = new NameExpr("return outputType");
                BlockStmt returnBlock = new BlockStmt();
                ASTHelper.addStmt(returnBlock, assign);
                ASTHelper.addStmt(returnBlock, returnExpr);
                statements.add(statements.indexOf(statement), returnBlock);
                statements.remove(statement);
                return bodyBlock;
            }
        }
        return bodyBlock;
    }
    private BlockStmt addReturnCode(MethodEntity methodEntity, List<FieldDeclaration> staticFields){
        BlockStmt bodyBlock = new BlockStmt();
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = new ArrayList<>();
        for (FieldDeclaration field:
                staticFields) {
            for (VariableDeclarator var :
                    field.getVariables()) {
                arguments.add(new NameExpr(var.getId().getName()));
            }
        }
        ClassOrInterfaceType type = new ClassOrInterfaceType("OutputType");

        ObjectCreationExpr objectCreationExpr =
                new ObjectCreationExpr(null, type, arguments);
        AssignExpr assign = new AssignExpr(outputTypeExpr, objectCreationExpr, AssignExpr.Operator.assign);
        NameExpr returnExpr = new NameExpr("return outputType");
        ASTHelper.addStmt(bodyBlock, assign);
        ASTHelper.addStmt(bodyBlock, returnExpr);
        return bodyBlock;
    }
    private List<ImportDeclaration> createImports(){
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
