package ch.zhaw.file_operations;

import japa.parser.ASTHelper;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.file_operations.UtilityClass.firstLetterToUpperCase;

public class InvokeMethodCreator {
    private MethodEntity methodEntity;
    private String newPath;

    public InvokeMethodCreator(MethodEntity methodEntity, String newPath) {
        this.methodEntity = methodEntity;
        this.newPath = newPath;
    }

    public void createMethodInvoker() {
        MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();
        BlockStmt bodyBlock = new BlockStmt();
        NameExpr accessIDKeyVarExpr = new NameExpr("String awsAccessKeyId = \"\"");
        NameExpr accessSecretKeyVarExpr = new NameExpr("String awsSecretAccessKey = \"\"");
        NameExpr regionNameVarExpr = new NameExpr("String regionName = \"\"");
        BlockStmt thenIf = new BlockStmt();
        ASTHelper.addStmt(thenIf, new NameExpr("awsAccessKeyId = System.getenv(\"awsAccessKeyId\")"));
        ASTHelper.addStmt(thenIf, new NameExpr("awsSecretAccessKey = System.getenv(\"awsSecretAccessKey\")"));
        ASTHelper.addStmt(thenIf, new NameExpr("regionName = System.getenv(\"awsRegion\")"));

        NameExpr conditionSide1 = new NameExpr("System.getenv(\"awsAccessKeyId\")");
        NameExpr conditionSide2 = new NameExpr("null");
        NameExpr compare = new NameExpr(conditionSide1 + " != " + conditionSide2);
        // TODO: 6/8/17 fix issue with conf file path and the following lambda name issue
//        String confPath = newPath + "/jyaml.yml";
        String confPath = "../jyaml.yml";
        NameExpr elseExpr = new NameExpr("try {\n" +
                "            awsAccessKeyId = Yaml.loadType(new File(\"" + confPath +"\"), AWSConfEntity.class).getAwsAccessKeyId();\n" +
                "            awsSecretAccessKey = Yaml.loadType(new File(\"" + confPath +"\"), AWSConfEntity.class).getAwsSecretAccessKey();\n" +
                "            regionName = Yaml.loadType(new File(\"" + confPath + "\"), AWSConfEntity.class).getAwsRegion();\n" +
                "        } catch (FileNotFoundException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }");
        BlockStmt elseStmt = new BlockStmt();
        ASTHelper.addStmt(elseStmt, elseExpr);
        IfStmt ifStmt = new IfStmt(compare, thenIf, elseStmt);

        String functionName = UtilityClass.generateLambdaName(methodEntity);
        NameExpr functionNameVarExpr = new NameExpr("String functionName = \"" + functionName + "\"");
        NameExpr regionVarExpr = new NameExpr("Region region");
        NameExpr credentialsVarExpr = new NameExpr("AWSCredentials credentials");
        NameExpr lambdaClientVarExpr = new NameExpr("AWSLambdaClient lambdaClient");
        NameExpr credentialsCreate = new NameExpr("credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)");
        NameExpr lambdaClientCreate = new NameExpr("lambdaClient = (credentials == null) ? new AWSLambdaClient()" +
                " : new AWSLambdaClient(credentials)");
        NameExpr regionCreate = new NameExpr("region = Region.getRegion(Regions.fromName(regionName))");
        NameExpr regionSet = new NameExpr("lambdaClient.setRegion(region)");

        //--creating input \/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

        // List<FieldDeclaration> staticFields = getOnlyStaticFields(methodEntity.getClassEntity().getFields());
        List<FieldDeclaration> allFields = methodEntity.getClassEntity().getFields();
        Expression outputTypeExpr = new NameExpr(getSupportClassPackage(methodEntity) + "InputType inputType");
        //InputType instance arguments
        List<Expression> argumentsIT = new ArrayList<>();
        for (FieldDeclaration field :
                allFields) {
            if (UtilityClass.isFieldAccessible(methodDeclaration, field)) {
                for (VariableDeclarator var :
                        field.getVariables()) {
                    argumentsIT.add(new NameExpr("this." + var.getId().getName()));
                }
            }
        }
        List<Parameter> params = methodEntity.getMethodDeclaration().getParameters();
        if (params != null) {
            for (Parameter param :
                    params) {
                argumentsIT.add(new NameExpr(param.getId().getName()));
            }
        }

        ClassOrInterfaceType type = new ClassOrInterfaceType(getSupportClassPackage(methodEntity) + "InputType");
        ObjectCreationExpr objectCreationExpr =
                new ObjectCreationExpr(null, type, argumentsIT);
        AssignExpr assign = new AssignExpr(outputTypeExpr, objectCreationExpr, AssignExpr.Operator.assign);

        //--creating input /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

        NameExpr jsonCreate = new NameExpr("ObjectMapper objectMapper = new ObjectMapper();\n" +
                "        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);\n" +
                "        String json = \"\";\n" +
                "        try {\n" +
                "            json = objectMapper.writeValueAsString(inputType);\n" +
                "        } catch (JsonProcessingException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "       " + getSupportClassPackage(methodEntity) + "OutputType outputType = null");
        NameExpr invokeInit = new NameExpr("try {\n" +
                "            InvokeRequest invokeRequest = new InvokeRequest();\n" +
                "            invokeRequest.setFunctionName(functionName);\n" +
                "            invokeRequest.setPayload(json)");
        NameExpr invoke = new NameExpr("outputType = objectMapper.readValue(byteBufferToString(\n" +
                "                    lambdaClient.invoke(invokeRequest).getPayload(),\n" +
                "                    Charset.forName(\"UTF-8\"))," + getSupportClassPackage(methodEntity) + "OutputType.class)");
        NameExpr tryEnd = new NameExpr("} catch(Exception e) {\n" +
                "          \n" +
                "            }");
        ASTHelper.addStmt(bodyBlock, accessIDKeyVarExpr);
        ASTHelper.addStmt(bodyBlock, accessSecretKeyVarExpr);
        ASTHelper.addStmt(bodyBlock, regionNameVarExpr);
        ASTHelper.addStmt(bodyBlock, ifStmt);
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
        ASTHelper.addStmt(bodyBlock, tryEnd);
        for (FieldDeclaration field :
                allFields) {
            if (!ModifierSet.isFinal(field.getModifiers())) {
                for (VariableDeclarator var :
                        field.getVariables()) {
                    NameExpr staticFieldVar = new NameExpr("this." + var.getId().getName());
                    MethodCallExpr methodCallExpr =
                            new MethodCallExpr(new NameExpr("outputType"), "get" +
                                    firstLetterToUpperCase(var.getId().getName()));
                    AssignExpr assignExpr = new AssignExpr(staticFieldVar, methodCallExpr, AssignExpr.Operator.assign);
                    ASTHelper.addStmt(bodyBlock, assignExpr);

                }
            }
        }
        if (methodDeclaration.getThrows() != null) {
            String exceptionTypeChecking = "";
            for (NameExpr exception :
                    methodDeclaration.getThrows()) {
                String exceptionName = exception.getName();
                exceptionTypeChecking += "if (outputType.getLambdaException().getClass().getSimpleName().equals(\"" + exceptionName + "\")){\n" +
                        "               throw (" + exceptionName + ")outputType.getLambdaException();\n" +
                        "           }\n         ";
            }
            NameExpr throwException = new NameExpr("if(outputType.getLambdaException() != null){\n" +
                    "           " + exceptionTypeChecking +
                    "           }");
            ASTHelper.addStmt(bodyBlock, throwException);
        }
        methodDeclaration.setBody(bodyBlock);

        if (!methodDeclaration.getType().equals(ASTHelper.VOID_TYPE)) {
            NameExpr returnExpr = new NameExpr("return outputType.get" +
                    firstLetterToUpperCase(methodDeclaration.getName() + "Result()"));
            ASTHelper.addStmt(bodyBlock, returnExpr);
        }
    }

    /**
     * Generates the package names for Input and Output types of certain function
     *
     * @param methodEntity source method for function
     * @return the {@code String} of package name
     */
    private String getSupportClassPackage(MethodEntity methodEntity) {
        String result = "awsl.";
        if (methodEntity.getClassEntity().getCu().getPackage() != null) {
            String packageStr = methodEntity.getClassEntity().getCu().getPackage().getName().toString();
            result = result + packageStr + ".";
        }
        String className = methodEntity.getClassEntity().getCu().getTypes().get(0).getName();
        result = result + className + ".";
        MethodDeclaration methodDeclaration = methodEntity.getMethodDeclaration();
        String functionName = "" + methodDeclaration.getName();
        if (methodDeclaration.getParameters() != null) {
            functionName = functionName + methodDeclaration.getParameters().size();
        }
        result = result + functionName + ".";
        return result;
    }
}
