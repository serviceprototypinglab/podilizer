package ch.zhaw.file_operations;

import japa.parser.ast.CompilationUnit;

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
