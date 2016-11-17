package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.expr.NameExpr;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JavaProjectEntity {
    private String pattern = "*.java";
    private Path location;
    private List<ClassEntity> classEntities;
    private List<NameExpr> methodEntities;
    private int mainCount = 0;


    public JavaProjectEntity(Path location){
        this.location = location;
        Finder finder = new Finder(pattern);
        try {
            Files.walkFileTree(location, finder);
            classEntities = finder.getFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        methodEntities = findAllMethods(classEntities);
    }

    public class Finder extends SimpleFileVisitor<Path> {
        private PathMatcher matcher;
        private List<ClassEntity> files = new ArrayList<>();


        Finder(String pattern){
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }
        //calls when visits a file
        void  find(Path file){
            Path name = file.getFileName();
            if(name != null && matcher.matches(name)){
                files.add(new ClassEntity(file));
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            find(file);
            return FileVisitResult.CONTINUE;
        }
        public List<ClassEntity> getFiles(){
            return files;
        }
    }

    public Path getLocation() {
        return location;
    }

    public List<ClassEntity> getClassEntities() {
        return classEntities;
    }

    private List<NameExpr> findAllMethods(List<ClassEntity> classEntities){
        Iterator<ClassEntity> iterator = classEntities.iterator();
        List<NameExpr> result = new ArrayList<>();
        while(iterator.hasNext()){
                List<MethodEntity> methodEntities = iterator.next().getFunctions();
                for (int i = 0; i < methodEntities.size(); i++){
                    result.add(methodEntities.get(i).getMethodDeclaration().getNameExpr());
            }
        }
        return result;
    }
    public List<MethodEntity> getStaticMethods(){
        List<MethodEntity> result = new ArrayList<>();
        for (ClassEntity classEntity:
                classEntities) {
            for (MethodEntity method:
                    classEntity.getFunctions()) {
                if (ModifierSet.isStatic(method.getMethodDeclaration().getModifiers())){
                    result.add(method);
                }
            }

        }
        return result;
    }

    public ClassEntity getMainClass() throws TooManyMainMethodsException {
        Iterator<ClassEntity> classEntityIterator = classEntities.iterator();
        ClassEntity result = null;
        while (classEntityIterator.hasNext()){
            ClassEntity classEntity = classEntityIterator.next();
            //System.out.println(classEntity.getMainMethod());
            if (classEntity.getMainMethod() != null){
                result = classEntity;
                mainCount++;
            }
        }
        if (mainCount > 1){
            throw new TooManyMainMethodsException();
        }
            mainCount = 0;
            return result;
    }
    public void printClasses(){
        Iterator<ClassEntity> classEntityIterator = classEntities.iterator();
        while (classEntityIterator.hasNext()){
            System.out.println(classEntityIterator.next().getPath());
        }
    }

    public List<NameExpr> getMethodEntities() {
        return methodEntities;
    }

    @Override
    public String toString() {
        return "JavaProjectEntity{" +
                "location=" + location +
                ", classEntities=" + classEntities +
                '}';
    }
}
