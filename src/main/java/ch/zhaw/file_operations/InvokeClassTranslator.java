package ch.zhaw.file_operations;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.List;

public class InvokeClassTranslator {
    private CompilationUnit compilationUnit;

    public InvokeClassTranslator(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    public void generateImports() {
        compilationUnit.setImports(createImports());
    }

    private List<ImportDeclaration> createImports() {
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
        ImportDeclaration imd9 = new ImportDeclaration();
        imd9.setName(new NameExpr("java.nio.ByteBuffer"));
        ImportDeclaration imd10 = new ImportDeclaration();
        imd10.setName(new NameExpr("java.nio.charset.Charset"));
        imports.add(imd1);
        imports.add(imd2);
        imports.add(imd3);
        imports.add(imd4);
        imports.add(imd5);
        imports.add(imd6);
        imports.add(imd7);
        imports.add(imd8);
        imports.add(imd9);
        imports.add(imd10);
        if (compilationUnit.getImports() != null) {
            imports.addAll(compilationUnit.getImports());
        }
        return imports;
    }

    public void addBufferByteReaderMethod() {
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
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
}
