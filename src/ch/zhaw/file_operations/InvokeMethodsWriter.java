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
            System.out.println(cu);
            invokeClassTranslator.generateImports();
            UtilityClass.writeCuToFile(classEntity.getPath().toString(), invokeClassTranslator.getCompilationUnit());
        }
    }

}
