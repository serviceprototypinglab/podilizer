package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.codehaus.plexus.util.FileUtils;
import sun.org.mozilla.javascript.internal.ast.VariableDeclaration;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
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
                System.out.println(createGetSet(createInPutType(javaProjectEntity.getMainClass().getMainMethod())));
                System.out.println("-------------------------------------------------------------");
                System.out.println(createGetSet(createOutPutType(javaProjectEntity.getMainClass().getMainMethod())));
                System.out.println("-------------------------------------------------------------");
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
    private CompilationUnit createLambdaFunction(MethodEntity methodEntity){
        CompilationUnit cu = methodEntity.getClassEntity().getCu();
        String name = cu.getPackage().getName() + "." + cu.getTypes().get(0).getName() + "." +
                methodEntity.getMethodDeclaration().getName() +
                methodEntity.getMethodDeclaration().getParameters().size();
        CompilationUnit newCU = new CompilationUnit();
        newCU.setImports(createImports(methodEntity));

        ClassOrInterfaceDeclaration classDeclaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, cu.getTypes().get(0).getName() + "AWSF" +
                        methodEntity.getMethodDeclaration().getName());
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        for (FieldDeclaration field :
                fields) {
             ASTHelper.addMember(classDeclaration, field);
        }
                List implementsList = new ArrayList();
        implementsList.add(new ClassOrInterfaceType("RequestHandler<InputType, OutputType>"));
        classDeclaration.setImplements(implementsList);
        ASTHelper.addTypeDeclaration(newCU, classDeclaration);
        MethodDeclaration method =
                new MethodDeclaration(ModifierSet.PUBLIC, new ClassOrInterfaceType("InputType"), "handleRequest");
        ASTHelper.addMember(classDeclaration, method);


        Parameter param1 = ASTHelper.createParameter(new ClassOrInterfaceType("InputType"), "inputType");
        Parameter param2 = ASTHelper.createParameter(new ClassOrInterfaceType("Context"), "context");
        ASTHelper.addParameter(method, param1);
        ASTHelper.addParameter(method, param2);
        BlockStmt bodyBlock = new BlockStmt();
        List<Parameter> parameters = methodEntity.getMethodDeclaration().getParameters();
        for (Parameter parameter :
                parameters) {
            NameExpr localVar = new NameExpr(parameter.getType().toString() + " " + parameter.getId().getName());
            MethodCallExpr methodCallExpr =
                    new MethodCallExpr(new NameExpr("InputType"), "get" +
                            firstLetterToUpperCase(parameter.getId().getName()));
            AssignExpr assignExpr = new AssignExpr(localVar, methodCallExpr, AssignExpr.Operator.assign);
            ASTHelper.addStmt(bodyBlock, assignExpr);
        }
        method.setBody(bodyBlock);
        return newCU;
    }
    private CompilationUnit createInPutType(MethodEntity methodEntity){
        CompilationUnit compilationUnit = methodEntity.getClassEntity().getCu();
        List<Parameter> parameters = methodEntity.getMethodDeclaration().getParameters();
        CompilationUnit inputCu = new CompilationUnit();
        ClassOrInterfaceDeclaration declaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "InputType");
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        ASTHelper.addTypeDeclaration(inputCu, declaration);
        for (FieldDeclaration field:
             fields) {
            field.setModifiers(ModifierSet.PUBLIC);
            ASTHelper.addMember(declaration, field);
        }
        for (Parameter param :
                parameters) {
            FieldDeclaration fieldDeclaration =
                    new FieldDeclaration(ModifierSet.PUBLIC, param.getType(), new VariableDeclarator(param.getId()));
            ASTHelper.addMember(declaration, fieldDeclaration);

        }
        VariableDeclarationExpr var = new VariableDeclarationExpr();

        return inputCu;

    }

    // TODO: 10/22/16  Create OutputType class creator

    private CompilationUnit createGetSet(CompilationUnit compilationUnit){
        FieldsVisitor fieldsVisitor = new FieldsVisitor();
        fieldsVisitor.visit(compilationUnit, null);
        List<FieldDeclaration> fieldDeclarationList = fieldsVisitor.getFieldDeclarationList();
        ConstructorDeclaration constructor = new ConstructorDeclaration(ModifierSet.PUBLIC, compilationUnit.getTypes().get(0).getName());
        List<Parameter> constrParameters = new LinkedList<>();
        BlockStmt constrBlock = new BlockStmt();
        constructor.setBlock(constrBlock);
        for (FieldDeclaration field :
                fieldDeclarationList) {
            for (VariableDeclarator var:
                 field.getVariables()) {
                Parameter constrParam = new Parameter(field.getType(), var.getId());
                constrParameters.add(constrParam);
                FieldAccessExpr fieldAccessExpr = new FieldAccessExpr(new ThisExpr(), var.getId().getName());
                AssignExpr assignExprConstr =
                        new AssignExpr(fieldAccessExpr, new NameExpr(var.getId().getName()), AssignExpr.Operator.assign);
                ASTHelper.addStmt(constrBlock, assignExprConstr);

                MethodDeclaration setter =
                        new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "set" +
                                firstLetterToUpperCase(var.getId().getName()));
                BlockStmt setBlock = new BlockStmt();
                setter.setBody(setBlock);
                FieldAccessExpr fieldExpr = new FieldAccessExpr(new ThisExpr(), var.getId().getName());
                AssignExpr assignExpr = new AssignExpr(fieldExpr, new NameExpr(var.getId().getName()), AssignExpr.Operator.assign);
                ASTHelper.addStmt(setBlock, assignExpr);
                Parameter param = ASTHelper.createParameter(field.getType(), var.getId().getName());
                ASTHelper.addParameter(setter, param);
                ASTHelper.addMember(compilationUnit.getTypes().get(0), setter);

                MethodDeclaration getter =
                        new MethodDeclaration(ModifierSet.PUBLIC, field.getType(), "get" +
                        firstLetterToUpperCase(var.getId().getName()));
                BlockStmt getBlock = new BlockStmt();
                NameExpr returnExpr = new NameExpr("return " + var.getId().getName());
                ASTHelper.addStmt(getBlock, returnExpr);
                getter.setBody(getBlock);
                ASTHelper.addMember(compilationUnit.getTypes().get(0), getter);

            }


        }
        constructor.setParameters(constrParameters);
        ASTHelper.addMember(compilationUnit.getTypes().get(0), constructor);


        return  compilationUnit;
    }
    private String firstLetterToUpperCase(String string){
        String first = string.substring(0, 1);
        String second = string.substring(1, string.length());
        first = first.toUpperCase();
        return first + second;
    }
    private class FieldsVisitor extends VoidVisitorAdapter {
        private List<FieldDeclaration> fieldDeclarationList = new ArrayList<>();

        @Override
        public void visit(FieldDeclaration n, Object arg) {
            fieldDeclarationList.add(n);
            super.visit(n, arg);
        }

        public List<FieldDeclaration> getFieldDeclarationList() {
            return fieldDeclarationList;
        }
    }
    private  CompilationUnit createOutPutType(MethodEntity methodEntity){
        CompilationUnit compilationUnit = methodEntity.getClassEntity().getCu();
        CompilationUnit outputCu = new CompilationUnit();
        ClassOrInterfaceDeclaration declaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "OutputType");
        ASTHelper.addTypeDeclaration(outputCu, declaration);
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        for (FieldDeclaration field:
                fields) {
            field.setModifiers(ModifierSet.PUBLIC);
            ASTHelper.addMember(declaration, field);
        }
        Type returnType = methodEntity.getMethodDeclaration().getType();
        if (returnType != ASTHelper.VOID_TYPE){
            FieldDeclaration fieldDeclaration =
                    new FieldDeclaration(ModifierSet.PUBLIC, returnType, new VariableDeclarator(
                            new VariableDeclaratorId(methodEntity.getMethodDeclaration().getName() + "Result")));
        }
        return outputCu;

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
