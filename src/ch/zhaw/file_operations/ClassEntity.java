package ch.zhaw.file_operations;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ClassEntity {;
    private Path path;
    private List<MethodEntity> functions;
    private List<FieldDeclaration> fields;
    private CompilationUnit cu;


    //transforms List<MethodDeclaration> into list<MethodEntity>
    private List<MethodEntity> listMethodEntityTransformer(List<MethodDeclaration> inputList){
        List<MethodEntity> result = new ArrayList<>();
        Iterator<MethodDeclaration> iterator = inputList.iterator();
        while (iterator.hasNext()){
            result.add(new MethodEntity(iterator.next(), this));
        }
        return result;
    }

    public ClassEntity(Path path){
        this.path = path;
        FileInputStream in = null;
        try {
            in = new FileInputStream(path.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            cu = JavaParser.parse(in);
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MethodVisitor methodVisitor = new MethodVisitor();
        methodVisitor.visit(cu, null);
        functions = listMethodEntityTransformer(methodVisitor.getMethodDeclarationList());
        FieldsVisitor fieldsVisitor = new FieldsVisitor();
        fieldsVisitor.visit(cu, null);
        fields = fieldsVisitor.getFieldDeclarationList();
    }

    private class MethodVisitor extends VoidVisitorAdapter {
        private List<MethodDeclaration> methodDeclarationList = new ArrayList<>();
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            methodDeclarationList.add(n);
            super.visit(n, arg);
        }
        public List<MethodDeclaration> getMethodDeclarationList(){
            return methodDeclarationList;
        }
    }
    private class FieldsVisitor extends VoidVisitorAdapter{
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

    public Path getPath() {
        return path;
    }

    public List<MethodEntity> getFunctions() {
        return functions;
    }

    //returns method 'main' in the class if it's exists if not, returns null
    public MethodEntity getMainMethod(){
        MethodEntity result = null;
        Iterator<MethodEntity> methodEntityIterator = functions.iterator();
        while (methodEntityIterator.hasNext()){
            MethodEntity function = methodEntityIterator.next();
            if (function.getMethodDeclaration().getName() != null && function.getMethodDeclaration().getName().equals("main")){
                result = function;
            }
        }
        return result;
    }

    public List<FieldDeclaration> getFields() {
        return fields;
    }

    public CompilationUnit getCu() {
        return cu;
    }

    @Override
    public String toString() {
        return "ClassEntity{" +
                "path=" + path +
                ", functions=" + functions +
                '}';
    }
}
