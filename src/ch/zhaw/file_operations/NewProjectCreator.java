package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.SourcesHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.FileUtils;
import sun.org.mozilla.javascript.internal.ast.VariableDeclaration;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NewProjectCreator {
    private String newPath;
    protected static String someTestString;
    private static JarBuilder jb;

    public NewProjectCreator(){
        newPath = ConfigReader.getConfig().getNewPath();
    }

    void copyProject() throws IOException {
        FileUtils.copyDirectoryStructure(new File(ConfigReader.getConfig().getPath()), new File(newPath));
    }
    void create() throws TooManyMainMethodsException {
        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(ConfigReader.getConfig().getPath()));

        /*
        JarUploader jarUploader = new JarUploader(ConfigReader.getConfig().getFileName(), "/home/dord/LambdaA.zip", "example.LambdaA::handleRequest", 30, 1024);
        jarUploader.uploadFunction();
        */

        try{
            JarBuilder jarBuilder = new JarBuilder();

            jarBuilder.creatDir("LambdaProjects");
            String classesPath = jarBuilder.createProjTree("LambdaProjects/NewProjectCreatorAWSFfirstLetterToUpperCase");

            writeToFile(classesPath + "/NewProjectCreatorAWSFfirstLetterToUpperCase.java",
                    createLambdaFunction(javaProjectEntityOld.getStaticMethods().get(2)));
            writeToFile(classesPath + "/OutputType.java",
                    createGetSet(createOutPutType(javaProjectEntityOld.getStaticMethods().get(2))));
            writeToFile(classesPath + "/InputType.java",
                    createGetSet(createInPutType(javaProjectEntityOld.getStaticMethods().get(2))));
            //System.out.println(createGetSet(createInPutType(javaProjectEntityOld.getStaticMethods().get(2))));
            //System.out.println(createGetSet(createInPutType(javaProjectEntityOld.getMainClass().getMainMethod())));
            //creating a JSON of input object
            InputType inputData = new InputType("hello");
            CompilationUnit cu = createGetSet(createInPutType(javaProjectEntityOld.getStaticMethods().get(2)));

            ObjectMapper objectMapper = new ObjectMapper();
            String json = "";
            try {
                json = objectMapper.writeValueAsString(inputData);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            System.out.println(json);

            jarBuilder.mvnBuild("LambdaProjects/NewProjectCreatorAWSFfirstLetterToUpperCase/");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private void writeToFile(String path, CompilationUnit cu){
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.print(cu);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private CompilationUnit createLambdaFunction(MethodEntity methodEntity){
        CompilationUnit cu = methodEntity.getClassEntity().getCu();
        List<FieldDeclaration> staticFields = new ArrayList<>();
        CompilationUnit newCU = new CompilationUnit();
        newCU.setImports(createImports(methodEntity));

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
        List<Statement> statements = bodyBlock.getStmts();
        for (Statement statment :
                statements) {
            if (statment.toString().contains("return")){
                String returnVar = statment.toString().substring(7, (statment.toString().length() - 1));
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
                //statment = returnBlock;
                statements.add(statements.indexOf(statment), returnBlock);
                statements.remove(statment);

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
            boolean isStaticNonFinal =
                    ModifierSet.isStatic(field.getModifiers()) & !ModifierSet.isFinal(field.getModifiers());
            //System.out.println("The fields " + field.getType() + " " + field.getVariables() + " modifier bool value is " + isStaticNonFinal);
            if (isStaticNonFinal){
                FieldDeclaration tmp = new FieldDeclaration(ModifierSet.PUBLIC, field.getType(), field.getVariables());
                ASTHelper.addMember(declaration, tmp);
            }
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
    private CompilationUnit createOutPutType(MethodEntity methodEntity){
        CompilationUnit compilationUnit = methodEntity.getClassEntity().getCu();
        CompilationUnit outputCu = new CompilationUnit();
        ClassOrInterfaceDeclaration declaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "OutputType");
        ASTHelper.addTypeDeclaration(outputCu, declaration);
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        for (FieldDeclaration field:
                fields) {
            boolean isStaticNonFinal =
                    ModifierSet.isStatic(field.getModifiers()) & !ModifierSet.isFinal(field.getModifiers());
            if (isStaticNonFinal){
                FieldDeclaration tmp = new FieldDeclaration(ModifierSet.PUBLIC, field.getType(), field.getVariables());
                ASTHelper.addMember(declaration, tmp);
            }
        }
        Type returnType = methodEntity.getMethodDeclaration().getType();
        if (!returnType.equals(ASTHelper.VOID_TYPE)){
            FieldDeclaration fieldDeclaration =
                    new FieldDeclaration(ModifierSet.PUBLIC, returnType, new VariableDeclarator(
                            new VariableDeclaratorId(methodEntity.getMethodDeclaration().getName() + "Result")));
            ASTHelper.addMember(declaration, fieldDeclaration);
        }
        return outputCu;

    }
    private CompilationUnit createGetSet(CompilationUnit compilationUnit){
        FieldsVisitor fieldsVisitor = new FieldsVisitor();
        fieldsVisitor.visit(compilationUnit, null);
        List<FieldDeclaration> fieldDeclarationList = fieldsVisitor.getFieldDeclarationList();
        ConstructorDeclaration constructor = new ConstructorDeclaration(ModifierSet.PUBLIC, compilationUnit.getTypes().get(0).getName());
        ConstructorDeclaration emptyConstructor = new ConstructorDeclaration(ModifierSet.PUBLIC, compilationUnit.getTypes().get(0).getName());
        List<Parameter> constrParameters = new LinkedList<>();
        BlockStmt constrBlock = new BlockStmt();
        BlockStmt emptyConstrBlock = new BlockStmt();
        constructor.setBlock(constrBlock);
        emptyConstructor.setBlock(emptyConstrBlock);
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
        ASTHelper.addMember(compilationUnit.getTypes().get(0), emptyConstructor);


        return  compilationUnit;
    }
    private static String firstLetterToUpperCase(String string){
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
