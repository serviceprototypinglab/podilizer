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
        compilationUnit.setImports(createImports(methodEntity.getClassEntity()));
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

        List<FieldDeclaration> staticFields = getOnlyStaticFields(methodEntity.getClassEntity().getFields());
        Expression outputTypeExpr = new NameExpr("InputType inputType");
        List<Expression> arguments = new ArrayList<>();
        for (FieldDeclaration field :
                staticFields) {
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
        ClassOrInterfaceType type = new ClassOrInterfaceType("InputType");
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
        NameExpr invoke = new NameExpr("OutputType outputType = byteBufferToString(\n" +
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
                staticFields){
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
    private List<ImportDeclaration> createImports(ClassEntity classEntity) {
        ArrayList<ImportDeclaration> imports = new ArrayList<>();
        ImportDeclaration imd1 = new ImportDeclaration();
        imd1.setName(new NameExpr("com.amazonaws.auth.AWSCredentials"));
        ImportDeclaration imd2 = new ImportDeclaration();
        imd2.setName(new NameExpr("com.amazonaws.auth.BasicAWSCredentials"));
        ImportDeclaration imd3 = new ImportDeclaration();
        imd3.setName(new NameExpr("com.amazonaws.regions.Region"));
        ImportDeclaration imd4 = new ImportDeclaration();
        imd4.setName(new NameExpr("com.amazonaws.regions.Regions"));
        ImportDeclaration imd5 = new ImportDeclaration();
        imd5.setName(new NameExpr("com.amazonaws.services.lambda.AWSLambdaClient"));
        ImportDeclaration imd6 = new ImportDeclaration();
        imd6.setName(new NameExpr("com.amazonaws.services.lambda.model.InvokeRequest"));
        ImportDeclaration imd7 = new ImportDeclaration();
        imd7.setName(new NameExpr("com.fasterxml.jackson.core.JsonProcessingException"));
        ImportDeclaration imd8 = new ImportDeclaration();
        imd8.setName(new NameExpr("com.fasterxml.jackson.databind.ObjectMapper"));
        imports.add(imd1);
        imports.add(imd2);
        imports.add(imd3);
        imports.add(imd4);
        imports.add(imd5);
        imports.add(imd6);
        imports.add(imd7);
        imports.add(imd8);
        List<ImportDeclaration> oldImports = classEntity.getCu().getImports();
        for (ImportDeclaration importD :
                oldImports) {
            if (!imports.contains(importD)) {
                imports.add(importD);
            }
        }
        return imports;
    }
    public CompilationUnit addBufferByteReaderMethod(CompilationUnit compilationUnit){
        ClassOrInterfaceType type = new ClassOrInterfaceType("String");
        MethodDeclaration declaration =
                new MethodDeclaration(ModifierSet.STATIC + ModifierSet.PUBLIC, type, "byteBufferToString");
        ClassOrInterfaceType param1Type = new ClassOrInterfaceType("ByteBuffer");
        Parameter param1 = new Parameter(param1Type, new VariableDeclaratorId("buffer"));
        ClassOrInterfaceType param2Type = new ClassOrInterfaceType("Charset");
        Parameter param2 = new Parameter(param2Type, new VariableDeclaratorId("charset"));
        ASTHelper.addParameter(declaration, param1);
        ASTHelper.addParameter(declaration, param2);
        BlockStmt methodBodyStmt = new BlockStmt();
        NameExpr bodyString = new NameExpr("byte[] bytes;\n" +
                "        if (buffer.hasArray()) {\n" +
                "            bytes = buffer.array();\n" +
                "        } else {\n" +
                "            bytes = new byte[buffer.remaining()];\n" +
                "            buffer.get(bytes);\n" +
                "        }\n" +
                "        return new String(bytes, charset)");
        ASTHelper.addStmt(methodBodyStmt, bodyString);
        declaration.setBody(methodBodyStmt);
        ASTHelper.addMember(compilationUnit.getTypes().get(0), declaration);
        return compilationUnit;
    }
}
