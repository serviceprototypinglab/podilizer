package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ReturnStmt;
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

            System.out.println(javaProjectEntityOld.getStaticMethods().get(3).getMethodDeclaration());
            System.out.println(javaProjectEntityOld.getStaticMethods().get(3).getClassEntity().getPath());

            InvokeMethodCreator invokeMethodCreator = new InvokeMethodCreator();
            invokeMethodCreator.createMethodInvoker(javaProjectEntityOld.getStaticMethods().get(3));
            System.out.println(javaProjectEntityOld.getStaticMethods().get(3).getMethodDeclaration());
            System.out.println("=====================================================================================");
            System.out.println(invokeMethodCreator.addBufferByteReaderMethod(javaProjectEntityOld.getStaticMethods().get(3).getClassEntity().getCu()));
            */
            //jarBuilder.mvnBuild("LambdaProjects/NewProjectCreatorAWSFfirstLetterToUpperCase/");

    }

    public CompilationUnit createLambdaFunction(MethodEntity methodEntity){
        CompilationUnit cu = methodEntity.getClassEntity().getCu();
        List<FieldDeclaration> staticFields = new ArrayList<>();
        CompilationUnit newCU = new CompilationUnit();
        List<ImportDeclaration> imports = methodEntity.getClassEntity().getCu().getImports();
        if (imports == null){
            imports = new ArrayList<>();
        }
        if (!imports.containsAll(createImports())){
            imports.addAll(createImports());
        }
        newCU.setImports(imports);
        newCU.setPackage(methodEntity.getClassEntity().getCu().getPackage());

        /*
        ClassOrInterfaceDeclaration classDeclaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, cu.getTypes().get(0).getName() + "AWSF" +
                        methodEntity.getMethodDeclaration().getName());
                        */
        ClassOrInterfaceDeclaration classDeclaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "LambdaFunction");
        if(methodEntity.getParentClass().getExtends() != null){
            classDeclaration.setExtends(methodEntity.getParentClass().getExtends());
        }
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        for (FieldDeclaration field :
                fields) {
            ASTHelper.addMember(classDeclaration, field);
            /*
            boolean isStaticNonFinal =
                    field.getModifiers() == ModifierSet.STATIC && field.getModifiers() != ModifierSet.FINAL;
            if (isStaticNonFinal){
                staticFields.add(field);
                ASTHelper.addMember(classDeclaration, field);
            }
            */
        }
        List implementsList = new ArrayList();
        implementsList.add(new ClassOrInterfaceType("RequestHandler<InputType, OutputType>"));
        classDeclaration.setImplements(implementsList);
        ASTHelper.addTypeDeclaration(newCU, classDeclaration);
        MethodDeclaration method =
                new MethodDeclaration(ModifierSet.PUBLIC, new ClassOrInterfaceType("OutputType"), "handleRequest");
        List<NameExpr> throwsList = methodEntity.getMethodDeclaration().getThrows();
        if(throwsList != null){
            method.setThrows(throwsList);
        }
        ASTHelper.addMember(classDeclaration, method);

        Parameter param1 = ASTHelper.createParameter(new ClassOrInterfaceType("InputType"), "inputType");
        Parameter param2 = ASTHelper.createParameter(new ClassOrInterfaceType("Context"), "context");
        ASTHelper.addParameter(method, param1);
        ASTHelper.addParameter(method, param2);
        BlockStmt bodyBlock = new BlockStmt();

        for (FieldDeclaration field:
                fields){
            for (VariableDeclarator var:
                    field.getVariables()) {
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
        if (methodEntity.getMethodDeclaration().getType().equals(ASTHelper.VOID_TYPE)){
            ASTHelper.addStmt(bodyBlock, addReturnCode(methodEntity, fields));
        }else {
            ASTHelper.addStmt(bodyBlock, returnReplace(methodEntity, fields));
        }
        method.setBody(bodyBlock);
        return newCU;
    }
    private BlockStmt returnReplace(MethodEntity methodEntity, List<FieldDeclaration> staticFields){
        BlockStmt bodyBlock = methodEntity.getMethodDeclaration().getBody();
        List<Statement> statements = bodyBlock.getStmts();
        List<Statement> newStatments = new ArrayList<>();
        for (Statement statement :
                statements) {
            if (statement instanceof ReturnStmt){
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
                newStatments.add(returnBlock);
            }else{
                newStatments.add(statement);
            }
        }
        bodyBlock = new BlockStmt(newStatments);
        return bodyBlock;
    }
    private BlockStmt addReturnCode(MethodEntity methodEntity, List<FieldDeclaration> staticFields){
        BlockStmt bodyBlock = methodEntity.getMethodDeclaration().getBody();
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
