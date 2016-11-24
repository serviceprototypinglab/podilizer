package ch.zhaw.file_operations;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.firstLetterToUpperCase;
import static ch.zhaw.file_operations.UtilityClass.getOnlyStaticFields;

public class InvokeMethodCreator {
    public void createMethodInvoker(MethodEntity methodEntity) {
        CompilationUnit compilationUnit = methodEntity.getClassEntity().getCu();
        BlockStmt bodyBlock = new BlockStmt();
        NameExpr accessIDKeyVarExpr = new NameExpr("static final String awsAccessIdKey = \"" +
                ConfigReader.getConfig().getAwsAccessKeyId() + "\"");
        NameExpr accessSecretKeyVarExpr = new NameExpr("static final String awsSecretAccessKey = \"" +
                ConfigReader.getConfig().getAwsSecretAccessKey() + "\"");
        NameExpr regionNameVarExpr = new NameExpr("static final String regionName = \"" +
                ConfigReader.getConfig().getRegion() + "\"");
        String functionName = "" + methodEntity.getClassEntity().getCu().getPackage().getName() +
                methodEntity.getClassEntity().getCu().getTypes().get(0).getName() +
                methodEntity.getMethodDeclaration().getName();
        NameExpr functionNameVarExpr = new NameExpr("static final String functionName = \"" + functionName + "\"");
        NameExpr regionVarExpr = new NameExpr("static Region region");
        NameExpr credentialsVarExpr = new NameExpr("static AWSCredentials credentials");
        NameExpr lambdaClientVarExpr = new NameExpr("static AWSLambdaClient lambdaClient");
        NameExpr credentialsCreate = new NameExpr("credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)");
        NameExpr lambdaClientCreate = new NameExpr("lambdaClient = (credentials == null) ? new AWSLambdaClient()" +
                " : new AWSLambdaClient(credentials)");
        NameExpr regionCreate = new NameExpr("region = Region.getRegion(Regions.fromName(regionName))");
        NameExpr regionSet = new NameExpr("lambdaClient.setRegion(region)");

        //--creating input \/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

        // List<FieldDeclaration> staticFields = getOnlyStaticFields(methodEntity.getClassEntity().getFields());
        List<FieldDeclaration> allFields = methodEntity.getClassEntity().getFields();
        Expression outputTypeExpr = new NameExpr(getSupportClassPackage(methodEntity) + "InputType inputType");
        List<Expression> arguments = new ArrayList<>();
        for (FieldDeclaration field :
                allFields) {
            for (VariableDeclarator var :
                    field.getVariables()) {
                arguments.add(new NameExpr(var.getId().getName()));
            }
        }
        List<Parameter> params = methodEntity.getMethodDeclaration().getParameters();
        if (params != null){
            for (Parameter param:
                    params) {
                arguments.add(new NameExpr(param.getId().getName()));
            }
        }
        ClassOrInterfaceType type = new ClassOrInterfaceType(getSupportClassPackage(methodEntity) + "InputType");
        ObjectCreationExpr objectCreationExpr =
                new ObjectCreationExpr(null, type, arguments);
        AssignExpr assign = new AssignExpr(outputTypeExpr, objectCreationExpr, AssignExpr.Operator.assign);

        //--creating input /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

        NameExpr jsonCreate = new NameExpr("ObjectMapper objectMapper = new ObjectMapper();\n" +
                "        String json = \"\";\n" +
                "        try {\n" +
                "            json = objectMapper.writeValueAsString(inputType);\n" +
                "        } catch (JsonProcessingException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }");
        NameExpr invokeInit = new NameExpr("InvokeRequest invokeRequest = new InvokeRequest();\n" +
                "            invokeRequest.setFunctionName(FunctionName);\n" +
                "            invokeRequest.setPayload(inputType)");
        NameExpr invoke = new NameExpr(getSupportClassPackage(methodEntity) + "OutputType outputType = byteBufferToString(\n" +
                "                    lambdaClient.invoke(invokeRequest).getPayload(),\n" +
                "                    Charset.forName(\"UTF-8\"),logger)");
        ASTHelper.addStmt(bodyBlock, accessIDKeyVarExpr);
        ASTHelper.addStmt(bodyBlock, accessSecretKeyVarExpr);
        ASTHelper.addStmt(bodyBlock, regionNameVarExpr);
        ASTHelper.addStmt(bodyBlock, functionNameVarExpr);
        ASTHelper.addStmt(bodyBlock, regionVarExpr);
        ASTHelper.addStmt(bodyBlock, credentialsVarExpr);
        ASTHelper.addStmt(bodyBlock, lambdaClientVarExpr);
        ASTHelper.addStmt(bodyBlock, credentialsCreate);
        ASTHelper.addStmt(bodyBlock, lambdaClientCreate);
        ASTHelper.addStmt(bodyBlock, regionCreate);
        ASTHelper.addStmt(bodyBlock, regionSet);
        ASTHelper.addStmt(bodyBlock, assign);
        ASTHelper.addStmt(bodyBlock, jsonCreate);
        ASTHelper.addStmt(bodyBlock, invokeInit);
        ASTHelper.addStmt(bodyBlock, invoke);
        for (FieldDeclaration staticField:
                allFields){
            for (VariableDeclarator var:
                    staticField.getVariables()) {
                NameExpr staticFieldVar = new NameExpr(var.getId().getName());
                MethodCallExpr methodCallExpr =
                        new MethodCallExpr(new NameExpr("outputType"), "get" +
                                firstLetterToUpperCase(var.getId().getName()));
                AssignExpr assignExpr = new AssignExpr(staticFieldVar, methodCallExpr, AssignExpr.Operator.assign);
                ASTHelper.addStmt(bodyBlock, assignExpr);

            }
        }
        methodEntity.getMethodDeclaration().setBody(bodyBlock);
        if (!methodEntity.getMethodDeclaration().getType().equals(ASTHelper.VOID_TYPE)){
            NameExpr returnExpr = new NameExpr("return outputType.get" +
                    firstLetterToUpperCase(methodEntity.getMethodDeclaration().getName() + "Result()"));
            ASTHelper.addStmt(bodyBlock, returnExpr);
        }
    }

    private String getSupportClassPackage(MethodEntity methodEntity){
        String result = "awsl.";
        String packageStr = methodEntity.getClassEntity().getCu().getPackage().getName().toString();
        if (packageStr != null){
            result = result + packageStr + ".";
        }
        String className = methodEntity.getClassEntity().getCu().getTypes().get(0).getName();
        result = result + className + ".";
        MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();
        String functionName = "" + methodDeclaration.getName();
        if (methodDeclaration.getParameters() != null){
            functionName = functionName + methodDeclaration.getParameters().size();
        }
        result = result + functionName + ".";
        return result;
    }
}
