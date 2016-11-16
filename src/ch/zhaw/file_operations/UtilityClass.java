package ch.zhaw.file_operations;

import com.sun.xml.internal.ws.org.objectweb.asm.FieldVisitor;
import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UtilityClass {
    public static void writeToFile(String path, CompilationUnit cu){
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.print(cu);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String firstLetterToUpperCase(String string){
        String first = string.substring(0, 1);
        String second = string.substring(1, string.length());
        first = first.toUpperCase();
        return first + second;
    }
    private static CompilationUnit createInPutType(MethodEntity methodEntity){
        List<Parameter> parameters = methodEntity.getMethodDeclaration().getParameters();
        CompilationUnit inputCu = new CompilationUnit();
        inputCu.setImports(methodEntity.getClassEntity().getCu().getImports());
        ClassOrInterfaceDeclaration declaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "InputType");
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        ASTHelper.addTypeDeclaration(inputCu, declaration);
        for (FieldDeclaration field:
                fields) {
            FieldDeclaration tmp = new FieldDeclaration(ModifierSet.PUBLIC, field.getType(), field.getVariables());
            ASTHelper.addMember(declaration, tmp);
            /*
            ~~ Only static fields processing ~~

            boolean isStaticNonFinal =
                    ModifierSet.isStatic(field.getModifiers()) & !ModifierSet.isFinal(field.getModifiers());
            if (isStaticNonFinal){
                FieldDeclaration tmp = new FieldDeclaration(ModifierSet.PUBLIC, field.getType(), field.getVariables());
                ASTHelper.addMember(declaration, tmp);
            }
            */
        }
        if (parameters != null){
            for (Parameter param :
                    parameters) {
                FieldDeclaration fieldDeclaration =
                        new FieldDeclaration(ModifierSet.PUBLIC, param.getType(), new VariableDeclarator(param.getId()));
                ASTHelper.addMember(declaration, fieldDeclaration);
            }
        }
        return inputCu;
    }
    private static CompilationUnit createOutPutType(MethodEntity methodEntity){
        CompilationUnit outputCu = new CompilationUnit();
        outputCu.setImports(methodEntity.getClassEntity().getCu().getImports());
        ClassOrInterfaceDeclaration declaration =
                new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, "OutputType");
        ASTHelper.addTypeDeclaration(outputCu, declaration);
        List<FieldDeclaration> fields = methodEntity.getClassEntity().getFields();
        for (FieldDeclaration field:
                fields) {
            FieldDeclaration tmp = new FieldDeclaration(ModifierSet.PUBLIC, field.getType(), field.getVariables());
            ASTHelper.addMember(declaration, tmp);
            /*
            ~~ Only static fields processing ~~

            boolean isStaticNonFinal =
                    ModifierSet.isStatic(field.getModifiers()) & !ModifierSet.isFinal(field.getModifiers());
            if (isStaticNonFinal){
                FieldDeclaration tmp = new FieldDeclaration(ModifierSet.PUBLIC, field.getType(), field.getVariables());
                ASTHelper.addMember(declaration, tmp);
            }
            */
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
    public static CompilationUnit createGetSet(CompilationUnit compilationUnit){
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
    private static class FieldsVisitor extends VoidVisitorAdapter {
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

    public static CompilationUnit getInputClass(MethodEntity methodEntity){
        return createGetSet(createInPutType(methodEntity));
    }
    public static CompilationUnit getOutputClass(MethodEntity methodEntity){
        return createGetSet(createOutPutType(methodEntity));
    }
    public static List<FieldDeclaration> getOnlyStaticFields(List<FieldDeclaration> fields) {
        List<FieldDeclaration> staticFields = new ArrayList<>();
        for (FieldDeclaration field :
                fields) {
            if (ModifierSet.isStatic(field.getModifiers())) {
                staticFields.add(field);
            }
        }
        return staticFields;
    }
}
