package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.Node;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import java.util.List;

public class InvokeMethodsWriter {
    JavaProjectEntity resultProject;

    public InvokeMethodsWriter(JavaProjectEntity resultProject) {
        this.resultProject = resultProject;
    }
    public void write(){
        List<ClassEntity> classEntityList = resultProject.getClassEntities();
        for (ClassEntity classEntity :
                classEntityList) {
            CompilationUnit cu = classEntity.getCu();
            ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
            int i = 0;
            for (Node node :
                    cu.getTypes().get(0).getChildrenNodes()) {
                if (node instanceof ClassOrInterfaceDeclaration){
                    i++;
                }
            }
            if (!declaration.isInterface() & i == 0){
                InvokeClassTranslator invokeClassTranslator = new InvokeClassTranslator(cu);
                invokeClassTranslator.addBufferByteReaderMethod();
                invokeClassTranslator.generateImports();
                List<MethodEntity> methodEntityList = classEntity.getFunctions();
                for (MethodEntity methodEntity :
                        methodEntityList) {
                    //if the method has more then one lien of code
                    if (methodEntity.getMethodDeclaration().getBody().getStmts().size() > 1){
                        InvokeMethodCreator invokeMethodCreator = new InvokeMethodCreator(methodEntity);
                        invokeMethodCreator.createMethodInvoker();
                    }
                }
                UtilityClass.writeCuToFile(classEntity.getPath().toString(), invokeClassTranslator.getCompilationUnit());
            }
        }
    }

}
