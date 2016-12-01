package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.*;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.ReadOnlyBufferException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.*;

public class NewProjectCreator {
    private String newPath;
    private String oldPath;

    public NewProjectCreator(){
        newPath = ConfigReader.getConfig().getNewPath();
        oldPath = ConfigReader.getConfig().getPath();
    }

    void copyProject() throws IOException, TooManyMainMethodsException {
        FileUtils.deleteDirectory(newPath);
        FileUtils.copyDirectoryStructure(new File(oldPath), new File(newPath));
        addLibs();
        JavaProjectEntity javaProjectEntityNew = new JavaProjectEntity(Paths.get(newPath));
        InvokeMethodsWriter invokeMethodsWriter = new InvokeMethodsWriter(javaProjectEntityNew);
        invokeMethodsWriter.write();
        create();
    }
    private void addLibs() throws IOException {
        File libDir = new File(newPath + "/libs");
        libDir.mkdir();
        FileUtils.copyDirectoryStructure(new File("additional/libs/"), new File(newPath + "/libs/"));
    }
    void create() throws TooManyMainMethodsException {
        JavaProjectEntity javaProjectEntityOld = new JavaProjectEntity(Paths.get(oldPath));
        SupportClassTreeCreator classTreeCreator = new SupportClassTreeCreator(javaProjectEntityOld);
        classTreeCreator.build();

    }
    public CompilationUnit createLambdaFunction(MethodEntity methodEntity, CompilationUnit translatedCu){
        CompilationUnit cu = methodEntity.getClassEntity().getCu();
        //System.out.println(cu.getImports());
        List<FieldDeclaration> staticFields = new ArrayList<>();
        //changeFiledCalls(methodEntity.getMethodDeclaration().getBody());
        CompilationUnit newCU = new CompilationUnit();

        List<ImportDeclaration> imports;
        if (methodEntity.getClassEntity().getCu().getImports() != null){
            imports = new ArrayList<>(methodEntity.getClassEntity().getCu().getImports());
        }else {
            imports = new ArrayList<>();
        }

        if (!imports.containsAll(createImports())){
            imports.addAll(createImports());
        }
        if (cu.getPackage() != null){
            String packageName  = cu.getPackage().getName().toString() + ".*";
            ImportDeclaration selfImport = new ImportDeclaration(new NameExpr(packageName), false, false);
            if (!imports.contains(selfImport)){
                imports.add(selfImport);
            }
        }
//        if (translatedCu.getImports() != null){
//            imports.addAll(translatedCu.getImports());
//        }
        newCU.setImports(imports);
        newCU.setPackage(new PackageDeclaration(new NameExpr(Constants.FUNCTION_PACKAGE)));

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
//        List<NameExpr> throwsList = methodEntity.getMethodDeclaration().getThrows();
//        if(throwsList != null){
//            method.setThrows(throwsList);
//        }
        ASTHelper.addMember(classDeclaration, method);

        Parameter param1 = ASTHelper.createParameter(new ClassOrInterfaceType("InputType"), "inputType");
        Parameter param2 = ASTHelper.createParameter(new ClassOrInterfaceType("Context"), "context");
        ASTHelper.addParameter(method, param1);
        ASTHelper.addParameter(method, param2);
        BlockStmt bodyBlock = new BlockStmt();
        ArrayList<String> fieldsNames = new ArrayList<>();
        for (FieldDeclaration field:
                fields){
            if (UtilityClass.isFieldAccessible(methodEntity.getMethodDeclaration(), field)){
                for (VariableDeclarator var:
                        field.getVariables()) {
                    fieldsNames.add(var.getId().getName());
                    NameExpr staticFieldVar = new NameExpr("this." + var.getId().getName());
                    MethodCallExpr methodCallExpr =
                            new MethodCallExpr(new NameExpr("inputType"), "get" +
                                    firstLetterToUpperCase(var.getId().getName()));
                    AssignExpr assignExpr = new AssignExpr(staticFieldVar, methodCallExpr, AssignExpr.Operator.assign);
                    ASTHelper.addStmt(bodyBlock, assignExpr);
                }
            }
        }
        List<Parameter> parameters = methodEntity.getMethodDeclaration().getParameters();
        List<Expression> args = new ArrayList<>();
        if (parameters != null){
            for (Parameter parameter :
                    parameters) {
                MethodCallExpr methodCallExpr =
                        new MethodCallExpr(new NameExpr("inputType"), "get" +
                                firstLetterToUpperCase(getFieldNameFromParam(parameter.getId(), fieldsNames).getId().getName()));
//                NameExpr localVar = new NameExpr(parameter.getType().toString() + " " + parameter.getId().getName());
//                AssignExpr assignExpr = new AssignExpr(localVar, methodCallExpr, AssignExpr.Operator.assign);
//                ASTHelper.addStmt(bodyBlock, assignExpr);
                args.add(methodCallExpr);
            }
        }
        MethodCallExpr selfMethodCallExpr = new MethodCallExpr(null, methodEntity.getMethodDeclaration().getName(), args);

        if (methodEntity.getMethodDeclaration().getThrows() != null){
            if (methodEntity.getMethodDeclaration().getType().equals(ASTHelper.VOID_TYPE)){
                Expression methodCall = wrapInTryCatch(selfMethodCallExpr, methodEntity, fields);
                ASTHelper.addStmt(bodyBlock, methodCall);
                ASTHelper.addStmt(bodyBlock, addReturnCode(methodEntity.getMethodDeclaration(), fields, null, null));
            }else {
                String resultType = methodEntity.getMethodDeclaration().getType().toString();
                String resultVar = methodEntity.getMethodDeclaration().getName() + "LambdaResult";
                AssignExpr assignExpr = new AssignExpr(new NameExpr(resultVar), selfMethodCallExpr,
                        AssignExpr.Operator.assign);
                Expression methodCall = wrapInTryCatch(resultType + " " + resultVar, assignExpr, resultVar, methodEntity, fields);
                ASTHelper.addStmt(bodyBlock, methodCall);
                ASTHelper.addStmt(bodyBlock, addReturnCode(methodEntity.getMethodDeclaration(), fields, resultVar, null, null));
//            ASTHelper.addStmt(bodyBlock, returnReplace(methodEntity.getMethodDeclaration(), fields));
            }
        } else {
            if (methodEntity.getMethodDeclaration().getType().equals(ASTHelper.VOID_TYPE)){
                ASTHelper.addStmt(bodyBlock, selfMethodCallExpr);
                ASTHelper.addStmt(bodyBlock, addReturnCode(methodEntity.getMethodDeclaration(), fields));
            }else {
                String resultType = methodEntity.getMethodDeclaration().getType().toString();
                String resultVar = methodEntity.getMethodDeclaration().getName() + "LambdaResult";
                AssignExpr assignExpr = new AssignExpr(new NameExpr(resultType + " " + resultVar), selfMethodCallExpr,
                        AssignExpr.Operator.assign);
                ASTHelper.addStmt(bodyBlock, assignExpr);
                ASTHelper.addStmt(bodyBlock, addReturnCode(methodEntity.getMethodDeclaration(), fields, resultVar));
//            ASTHelper.addStmt(bodyBlock, returnReplace(methodEntity.getMethodDeclaration(), fields));
            }
        }
        method.setBody(bodyBlock);

        List<BodyDeclaration> extraClassMembers = translatedCu.getTypes().get(0).getMembers();
        for (BodyDeclaration member :
                extraClassMembers) {
            if (!(member instanceof FieldDeclaration) & !(member instanceof ConstructorDeclaration)){
                if (member instanceof MethodDeclaration){
                    MethodDeclaration selfMethod = (MethodDeclaration)member;
                    int params1 = 0;
                    if (selfMethod.getParameters() != null){
                        params1 += selfMethod.getParameters().size();
                    }
                    int params2 = 0;
                    if (methodEntity.getMethodDeclaration().getParameters() != null){
                        params2 += methodEntity.getMethodDeclaration().getParameters().size();
                    }
                    boolean isMatched = selfMethod.getName().equals(methodEntity.getMethodDeclaration().getName()) &
                            params1 == params2;
                    if (!isMatched){
                        ASTHelper.addMember(classDeclaration, member);
                    }

                }else {
                    ASTHelper.addMember(classDeclaration, member);
                }

            }
        }
        InvokeClassTranslator invokeClassTranslator = new InvokeClassTranslator(newCU);
        invokeClassTranslator.generateImports();
        ASTHelper.addMember(classDeclaration, methodEntity.getMethodDeclaration());
        return newCU;
    }
    private Expression wrapInTryCatch(Expression expressionToWrap, MethodEntity methodEntity, List<FieldDeclaration> fields){
        String exceptionVarId = "e";
        Expression tryExpression = expressionToWrap;
        if (methodEntity.getMethodDeclaration().getThrows() != null){
            List<NameExpr> exceptionsList = methodEntity.getMethodDeclaration().getThrows();
            String tryBlock = "try{\n " +
                    "           " + expressionToWrap + ";\n" +
                    "       }";
            for (NameExpr exception :
                    exceptionsList) {
                BlockStmt inToCatch = addReturnCode(methodEntity.getMethodDeclaration(), fields, exceptionVarId, exception.getName());
                tryBlock += generateCatchBlock(exception.getName(), exceptionVarId, inToCatch);
            }
            tryExpression = new NameExpr(tryBlock);
        }
        return tryExpression;
    }
    private Expression wrapInTryCatch(String declaration, Expression call, String resultVar, MethodEntity methodEntity, List<FieldDeclaration> fields){
        String exceptionVarId = "e";
        Expression tryExpression = new NameExpr();
        if (methodEntity.getMethodDeclaration().getThrows() != null){
            List<NameExpr> exceptionsList = methodEntity.getMethodDeclaration().getThrows();
            String tryBlock = declaration + " = null; \n" +
                    "       try{\n " +
                    "           " + call + ";\n" +
                    "       }";
            for (NameExpr exception :
                    exceptionsList) {
                BlockStmt inToCatch = addReturnCode(methodEntity.getMethodDeclaration(), fields, resultVar, exceptionVarId, exception.getName());
                tryBlock += generateCatchBlock(exception.getName(), exceptionVarId, inToCatch);
            }
            tryExpression = new NameExpr(tryBlock);
        }
        return tryExpression;
    }
    private String generateCatchBlock(String exception, String varId, BlockStmt catchAction){
        return  " catch(" + exception + " " + varId + " ) {\n" +
                "           " + catchAction +
                "       }";
    }
    private BlockStmt returnReplace(MethodDeclaration methodDeclaration, List<FieldDeclaration> fields){
        BlockStmt bodyBlock = new BlockStmt(methodDeclaration.getBody().getStmts());
        List<Statement> statements = bodyBlock.getStmts();
        List<Statement> newStatments = new ArrayList<>();
        for (Statement statement :
                statements) {
            if (statement instanceof ReturnStmt){
                ReturnStmt returnStmt = (ReturnStmt)statement;
                String returnVar = returnStmt.getExpr().toString();
                Expression outputTypeExpr = new NameExpr("OutputType outputType");
                List<Expression> arguments = new ArrayList<>();
                for (FieldDeclaration field:
                        fields) {
                    for (VariableDeclarator var :
                            field.getVariables()) {
                        arguments.add(new NameExpr("this." + var.getId().getName()));
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
    private BlockStmt addReturnCode(MethodDeclaration methodDeclaration, List<FieldDeclaration> fields){
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = generateArguments(fields);
        ClassOrInterfaceType type = new ClassOrInterfaceType("OutputType");

        ObjectCreationExpr objectCreationExpr =
                new ObjectCreationExpr(null, type, arguments);
        AssignExpr assign = new AssignExpr(outputTypeExpr, objectCreationExpr, AssignExpr.Operator.assign);
        NameExpr returnExpr = new NameExpr("return outputType");
        BlockStmt result = new BlockStmt();
        ASTHelper.addStmt(result, assign);
        ASTHelper.addStmt(result, returnExpr);
        return result;
    }
    private BlockStmt addReturnCode(MethodDeclaration methodDeclaration, List<FieldDeclaration> fields, String returnVar){
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = generateArguments(fields);
        arguments.add(new NameExpr(returnVar));
        ClassOrInterfaceType type = new ClassOrInterfaceType("OutputType");

        ObjectCreationExpr objectCreationExpr =
                new ObjectCreationExpr(null, type, arguments);
        AssignExpr assign = new AssignExpr(outputTypeExpr, objectCreationExpr, AssignExpr.Operator.assign);
        NameExpr returnExpr = new NameExpr("return outputType");
        BlockStmt result = new BlockStmt();
        ASTHelper.addStmt(result, assign);
        ASTHelper.addStmt(result, returnExpr);
        return result;
    }
    private BlockStmt addReturnCode(MethodDeclaration methodDeclaration, List<FieldDeclaration> fields, String exception, String exceptionType){
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = generateArguments(fields);
        arguments.add(new NameExpr(exception));
        arguments.add(new NameExpr("\"" + exceptionType + "\""));
        ClassOrInterfaceType type = new ClassOrInterfaceType("OutputType");

        ObjectCreationExpr objectCreationExpr =
                new ObjectCreationExpr(null, type, arguments);
        AssignExpr assign = new AssignExpr(outputTypeExpr, objectCreationExpr, AssignExpr.Operator.assign);
        NameExpr returnExpr = new NameExpr("return outputType");
        BlockStmt result = new BlockStmt();
        ASTHelper.addStmt(result, assign);
        ASTHelper.addStmt(result, returnExpr);
        return result;
    }
    private BlockStmt addReturnCode(MethodDeclaration methodDeclaration, List<FieldDeclaration> fields,
                                    String returnVar, String exception, String exceptionType){
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = generateArguments(fields);
        arguments.add(new NameExpr(returnVar));
        arguments.add(new NameExpr(exception));
        arguments.add(new NameExpr("\"" + exceptionType + "\""));
        ClassOrInterfaceType type = new ClassOrInterfaceType("OutputType");

        ObjectCreationExpr objectCreationExpr =
                new ObjectCreationExpr(null, type, arguments);
        AssignExpr assign = new AssignExpr(outputTypeExpr, objectCreationExpr, AssignExpr.Operator.assign);
        NameExpr returnExpr = new NameExpr("return outputType");
        BlockStmt result = new BlockStmt();
        ASTHelper.addStmt(result, assign);
        ASTHelper.addStmt(result, returnExpr);
        return result;
    }
    private List<Expression> generateArguments(List<FieldDeclaration> fields){
        List<Expression> arguments = new ArrayList<>();
        for (FieldDeclaration field:
                fields) {
            if (!ModifierSet.isFinal(field.getModifiers())){
                for (VariableDeclarator var :
                        field.getVariables()) {
                    arguments.add(new NameExpr("this." + var.getId().getName()));
                }
            }
        }
        return arguments;
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
