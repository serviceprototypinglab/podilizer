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
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <h1> Represents .java file with class inside</h1>
 */
public class ClassEntity implements Serializable {
    private Path path;
    private List<MethodEntity> functions;
    private List<FieldDeclaration> fields;
    private CompilationUnit cu;

    public ClassEntity(ClassEntity classEntity) {
        this.path = classEntity.getPath();
        this.fields = classEntity.getFields();
        this.functions = classEntity.getFunctions();
        this.cu = classEntity.getCu();
    }

    /**
     * Transforms List<MethodDeclaration> into List<MethodEntity>
     *
     * @param inputList you want to transform
     * @return the list of {@code MethodEntity}
     */
    private List<MethodEntity> listMethodEntityTransformer(List<MethodDeclaration> inputList) {
        List<MethodEntity> result = new ArrayList<>();
        Iterator<MethodDeclaration> iterator = inputList.iterator();
        while (iterator.hasNext()) {
            result.add(new MethodEntity(iterator.next(), this));
        }
        return result;
    }

    public ClassEntity(Path path) {
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

    /**
     * Visits methods in the class
     */
    private class MethodVisitor extends VoidVisitorAdapter {
        private List<MethodDeclaration> methodDeclarationList = new ArrayList<>();

        /**
         * Override method, saves visited methods into the List
         *
         * @param n
         * @param arg
         */
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            methodDeclarationList.add(n);

            super.visit(n, arg);
        }

        /**
         * @return the List with Method declarations
         */
        public List<MethodDeclaration> getMethodDeclarationList() {
            return methodDeclarationList;
        }
    }

    /**
     * Visits fields in the class
     */
    private class FieldsVisitor extends VoidVisitorAdapter {
        private List<FieldDeclaration> fieldDeclarationList = new ArrayList<>();

        /**
         * Saves visited fields into List
         *
         * @param n
         * @param arg
         */
        @Override
        public void visit(FieldDeclaration n, Object arg) {
            fieldDeclarationList.add(n);
            super.visit(n, arg);
        }

        /**
         * @return the List with Field declarations
         */
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

    /**
     * Looks for main method
     *
     * @return method 'main' in the class if it exists, if not - null
     */
    public MethodEntity getMainMethod() {
        MethodEntity result = null;
        Iterator<MethodEntity> methodEntityIterator = functions.iterator();
        while (methodEntityIterator.hasNext()) {
            MethodEntity function = methodEntityIterator.next();
            if (function.getMethodDeclaration().getName() != null && function.getMethodDeclaration().getName().equals("main")) {
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
