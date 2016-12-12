package ch.zhaw.file_operations;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.firstLetterToUpperCase;
import static ch.zhaw.file_operations.UtilityClass.getFieldNameFromParam;

public class LambdaFunction {
    private MethodEntity methodEntity;
    private CompilationUnit translatedCu;
    private CompilationUnit newCU;
    private List<FieldDeclaration> fields;

    public LambdaFunction() {

    }

    public LambdaFunction(MethodEntity methodEntity, CompilationUnit translatedCu) {
        this.methodEntity = methodEntity;
        this.translatedCu = translatedCu;
        this.newCU = new CompilationUnit();
        this.fields = methodEntity.getClassEntity().getFields();
    }

    public void create() {
        CompilationUnit cu = methodEntity.getClassEntity().getCu();
        newCU = new CompilationUnit();
        List<ImportDeclaration> imports;
        if (methodEntity.getClassEntity().getCu().getImports() != null) {
            imports = new ArrayList<>(methodEntity.getClassEntity().getCu().getImports());
        } else {
            imports = new ArrayList<>();
        }

        if (!imports.containsAll(createImports())) {
            imports.addAll(createImports());
        }
        if (cu.getPackage() != null) {
            String packageName = cu.getPackage().getName().toString() + ".*";
            ImportDeclaration selfImport = new ImportDeclaration(new NameExpr(packageName), false, false);
            if (!imports.contains(selfImport)) {
                imports.add(selfImport);
            }
        }
        newCU.setImports(imports);
        newCU.setPackage(new PackageDeclaration(new NameExpr(Constants.FUNCTION_PACKAGE)));
        ClassOrInterfaceDeclaration classDeclaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "LambdaFunction");
        if (methodEntity.getParentClass().getExtends() != null) {
            classDeclaration.setExtends(methodEntity.getParentClass().getExtends());
        }
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        for (FieldDeclaration field :
                fields) {
            ASTHelper.addMember(classDeclaration, field);
        }
        ClassOrInterfaceDeclaration type = (ClassOrInterfaceDeclaration) translatedCu.getTypes().get(0);
        List<ClassOrInterfaceType> implementsList = new ArrayList<>();
        implementsList.add(new ClassOrInterfaceType("RequestHandler<InputType, OutputType>"));
        if (type.getImplements() != null) {
            implementsList.addAll(type.getImplements());
        }
        classDeclaration.setImplements(implementsList);
        if (type.getExtends() != null) {
            classDeclaration.setExtends(type.getExtends());
        }
        ASTHelper.addTypeDeclaration(newCU, classDeclaration);
        MethodDeclaration method =
                new MethodDeclaration(ModifierSet.PUBLIC, new ClassOrInterfaceType("OutputType"), "handleRequest");
        ASTHelper.addMember(classDeclaration, method);

        Parameter param1 = ASTHelper.createParameter(new ClassOrInterfaceType("InputType"), "inputType");
        Parameter param2 = ASTHelper.createParameter(new ClassOrInterfaceType("Context"), "context");
        ASTHelper.addParameter(method, param1);
        ASTHelper.addParameter(method, param2);
        BlockStmt bodyBlock = new BlockStmt();
        ArrayList<String> fieldsNames = new ArrayList<>();
        for (FieldDeclaration field :
                fields) {
            if (UtilityClass.isFieldAccessible(methodEntity.getMethodDeclaration(), field)) {
                for (VariableDeclarator var :
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
        addReturnBlock(bodyBlock, fieldsNames);
        method.setBody(bodyBlock);

        List<BodyDeclaration> extraClassMembers = translatedCu.getTypes().get(0).getMembers();
        for (BodyDeclaration member :
                extraClassMembers) {
            if (!(member instanceof FieldDeclaration) & !(member instanceof ConstructorDeclaration)) {
                if (member instanceof MethodDeclaration) {
                    MethodDeclaration selfMethod = (MethodDeclaration) member;
                    int params1 = 0;
                    if (selfMethod.getParameters() != null) {
                        params1 += selfMethod.getParameters().size();
                    }
                    int params2 = 0;
                    if (methodEntity.getMethodDeclaration().getParameters() != null) {
                        params2 += methodEntity.getMethodDeclaration().getParameters().size();
                    }
                    boolean isMatched = selfMethod.getName().equals(methodEntity.getMethodDeclaration().getName()) &
                            params1 == params2;
                    if (!isMatched) {
                        ASTHelper.addMember(classDeclaration, member);
                    }

                } else {
                    ASTHelper.addMember(classDeclaration, member);
                }

            }
        }
        InvokeClassTranslator invokeClassTranslator = new InvokeClassTranslator(newCU);
        invokeClassTranslator.generateImports();
        ASTHelper.addMember(classDeclaration, methodEntity.getMethodDeclaration());
    }

    private void addReturnBlock(BlockStmt bodyBlock, ArrayList<String> fieldsNames) {
        List<Parameter> parameters = methodEntity.getMethodDeclaration().getParameters();
        List<Expression> args = new ArrayList<>();
        if (parameters != null) {
            for (Parameter parameter :
                    parameters) {
                MethodCallExpr methodCallExpr =
                        new MethodCallExpr(new NameExpr("inputType"), "get" +
                                firstLetterToUpperCase(getFieldNameFromParam(parameter.getId(), fieldsNames).getId().getName()));
                args.add(methodCallExpr);
            }
        }
        MethodCallExpr selfMethodCallExpr = new MethodCallExpr(null, methodEntity.getMethodDeclaration().getName(), args);

        if (methodEntity.getMethodDeclaration().getThrows() != null) {
            if (methodEntity.getMethodDeclaration().getType().equals(ASTHelper.VOID_TYPE)) {
                Expression methodCall = wrapInTryCatch(selfMethodCallExpr, methodEntity);
                ASTHelper.addStmt(bodyBlock, methodCall);
                ASTHelper.addStmt(bodyBlock, addReturnCode(null, null));
            } else {
                String resultType = methodEntity.getMethodDeclaration().getType().toString();
                String resultVar = methodEntity.getMethodDeclaration().getName() + "LambdaResult";
                AssignExpr assignExpr = new AssignExpr(new NameExpr(resultVar), selfMethodCallExpr,
                        AssignExpr.Operator.assign);
                Expression methodCall = wrapInTryCatch(resultType + " " + resultVar, assignExpr, resultVar, methodEntity);
                ASTHelper.addStmt(bodyBlock, methodCall);
                ASTHelper.addStmt(bodyBlock, addReturnCode(resultVar, null, null));
            }
        } else {
            if (methodEntity.getMethodDeclaration().getType().equals(ASTHelper.VOID_TYPE)) {
                ASTHelper.addStmt(bodyBlock, selfMethodCallExpr);
                ASTHelper.addStmt(bodyBlock, addReturnCode());
            } else {
                String resultType = methodEntity.getMethodDeclaration().getType().toString();
                String resultVar = methodEntity.getMethodDeclaration().getName() + "LambdaResult";
                AssignExpr assignExpr = new AssignExpr(new NameExpr(resultType + " " + resultVar), selfMethodCallExpr,
                        AssignExpr.Operator.assign);
                ASTHelper.addStmt(bodyBlock, assignExpr);
                ASTHelper.addStmt(bodyBlock, addReturnCode(resultVar));
            }
        }
    }

    private Expression wrapInTryCatch(Expression expressionToWrap, MethodEntity methodEntity) {
        String exceptionVarId = "e";
        Expression tryExpression = expressionToWrap;
        if (methodEntity.getMethodDeclaration().getThrows() != null) {
            List<NameExpr> exceptionsList = methodEntity.getMethodDeclaration().getThrows();
            String tryBlock = "try{\n " +
                    "           " + expressionToWrap + ";\n" +
                    "       }";
            for (NameExpr exception :
                    exceptionsList) {
                BlockStmt inToCatch = addReturnCode(exceptionVarId, exception.getName());
                tryBlock += generateCatchBlock(exception.getName(), exceptionVarId, inToCatch);
            }
            tryExpression = new NameExpr(tryBlock);
        }
        return tryExpression;
    }

    private Expression wrapInTryCatch(String declaration, Expression call, String resultVar, MethodEntity methodEntity) {
        String exceptionVarId = "e";
        Expression tryExpression = new NameExpr();
        if (methodEntity.getMethodDeclaration().getThrows() != null) {
            List<NameExpr> exceptionsList = methodEntity.getMethodDeclaration().getThrows();
            String tryBlock = declaration + " = null; \n" +
                    "       try{\n " +
                    "           " + call + ";\n" +
                    "       }";
            for (NameExpr exception :
                    exceptionsList) {
                BlockStmt inToCatch = addReturnCode(resultVar, exceptionVarId, exception.getName());
                tryBlock += generateCatchBlock(exception.getName(), exceptionVarId, inToCatch);
            }
            tryExpression = new NameExpr(tryBlock);
        }
        return tryExpression;
    }

    private String generateCatchBlock(String exception, String varId, BlockStmt catchAction) {
        return " catch(" + exception + " " + varId + " ) {\n" +
                "           " + catchAction +
                "       }";
    }

    private BlockStmt addReturnCode() {
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = generateArguments();
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

    private BlockStmt addReturnCode(String returnVar) {
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = generateArguments();
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

    private BlockStmt addReturnCode(String exception, String exceptionType) {
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = generateArguments();
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

    private BlockStmt addReturnCode(String returnVar, String exception, String exceptionType) {
        Expression outputTypeExpr = new NameExpr("OutputType outputType");
        List<Expression> arguments = generateArguments();
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

    private List<Expression> generateArguments() {
        List<Expression> arguments = new ArrayList<>();
        for (FieldDeclaration field :
                fields) {
            if (!ModifierSet.isFinal(field.getModifiers())) {
                for (VariableDeclarator var :
                        field.getVariables()) {
                    arguments.add(new NameExpr("this." + var.getId().getName()));
                }
            }
        }
        return arguments;
    }

    private List<ImportDeclaration> createImports() {
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

    private BlockStmt returnReplace(MethodDeclaration methodDeclaration, List<FieldDeclaration> fields) {
        BlockStmt bodyBlock = new BlockStmt(methodDeclaration.getBody().getStmts());
        List<Statement> statements = bodyBlock.getStmts();
        List<Statement> newStatments = new ArrayList<>();
        for (Statement statement :
                statements) {
            if (statement instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) statement;
                String returnVar = returnStmt.getExpr().toString();
                Expression outputTypeExpr = new NameExpr("OutputType outputType");
                List<Expression> arguments = new ArrayList<>();
                for (FieldDeclaration field :
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
            } else {
                newStatments.add(statement);
            }
        }
        bodyBlock = new BlockStmt(newStatments);
        return bodyBlock;
    }

    public CompilationUnit getNewCU() {
        return newCU;
    }
}
