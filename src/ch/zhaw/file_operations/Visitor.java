package ch.zhaw.file_operations;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;

public class Visitor {
    public static ArrayList<Path> paths = new ArrayList<Path>();
    public static void main(String[] args) throws Exception{
        Path startDir;
        String pattern = "*.java";

        if (args.length != 0){
            if(args.length != 1){
                usage();
            }
            startDir = Paths.get(args[0]);
        }else{
            startDir = Paths.get("/home/dord/IdeaProjects/");
        }
        Finder finder = new Finder(pattern);
        Files.walkFileTree(startDir, finder);

        Iterator iterator = paths.iterator();
        while(iterator.hasNext()){
            FileInputStream in = new FileInputStream(iterator.next().toString());

            CompilationUnit cu
                    ;
            try {
                cu = JavaParser.parse(in);
            }finally {
                in.close();
            }
            new MethodVisitor().visit(cu, null);
        }


    }
    static void usage(){
        System.err.println("java -jar methodvisitor.jar <path>");
        System.exit(-1);


    }

    private static class MethodVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            System.out.println(n);
            System.out.println("---------------------------------------------------------------------");
            super.visit(n, arg);
        }
    }
    public static class Finder extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher;



        Finder(String pattern){
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }

        void  find(Path file){
            Path name = file.getFileName();
            if(name != null && matcher.matches(name)){
                paths.add(file);
                System.out.println(file);
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            find(file);
            return FileVisitResult.CONTINUE;
        }
    }
}
