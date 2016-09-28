package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dord on 9/28/16.
 */
public class MethodEntity {
    private MethodDeclaration methodDeclaration;
    private List<MethodCallExpr> methodCallExprs;
    private ClassEntity classEntity;

    public List<MethodCallExpr> getMethodCallExprs() {
        return methodCallExprs;
    }
    public MethodEntity(MethodDeclaration methodDeclaration, ClassEntity classEntity) {
        this.methodDeclaration = methodDeclaration;
        this.classEntity = classEntity;
        MethodCallsVisitor methodCallsVisitor = new MethodCallsVisitor();
        methodCallsVisitor.visit(classEntity.getCu(), null);
        methodCallExprs = methodCallsVisitor.getMethodCallExprs();
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
