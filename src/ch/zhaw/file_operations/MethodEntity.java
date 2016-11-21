package ch.zhaw.file_operations;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MethodEntity {
    private MethodDeclaration methodDeclaration;
    private List<MethodCallExpr> methodCallExprs;
    private ClassEntity classEntity;



    public List<MethodCallExpr> getMethodCallExprs() {
        return methodCallExprs;
    }

    public ClassEntity getClassEntity() {
        return classEntity;
    }

    public MethodEntity(MethodDeclaration methodDeclaration, ClassEntity classEntity) {
        this.methodDeclaration = methodDeclaration;
        this.classEntity = classEntity;
        MethodCallsVisitor methodCallsVisitor = new MethodCallsVisitor();
        methodCallsVisitor.visit(methodDeclaration, null);
        methodCallExprs = methodCallsVisitor.getMethodCallExprs();
    }

    public ClassOrInterfaceDeclaration getParentClass(){
        try {
            ClassOrInterfaceDeclaration result = (ClassOrInterfaceDeclaration)methodDeclaration.getParentNode();
            return result;
        }catch (ClassCastException e){
            ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr)methodDeclaration.getParentNode();
            // TODO: 11/17/16 prevent anonymous classes processing
            //this = new MethodEntity(new MethodDeclaration(), new ClassEntity(Paths.get("asdasd")));
        }
        return null;
    }

    public MethodDeclaration getMethodDeclaration() {
        return methodDeclaration;
    }

    public void setMethodDeclaration(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }
    //visits methods calls
    private class MethodCallsVisitor extends VoidVisitorAdapter {
        private List<MethodCallExpr> methodCallExprs = new ArrayList<>();
        @Override
        public void visit(MethodCallExpr n, Object arg) {
            methodCallExprs.add(n);
            super.visit(n, arg);
        }

        public List<MethodCallExpr> getMethodCallExprs() {
            return methodCallExprs;
        }
    }

}
