package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;
import japa.parser.ast.expr.MethodCallExpr;

import java.nio.file.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardCopyOption.*;

public class Visitor {
    //public static ArrayList<Path> paths = new ArrayList<Path>();
    public static void main(String[] args) throws Exception{
        JavaProjectEntity javaProjectEntity = new JavaProjectEntity(Paths.get("/home/dord/IdeaProjects/service_tooling_initiative"));
        try{
            System.out.println(javaProjectEntity.getMainClass().getMainMethod());
            javaProjectEntity.getMainClass().getMainMethod();
            System.out.println("================================================================================================");
            System.out.println(javaProjectEntity.getMainClass().getMainMethod().getMethodCallExprs());
            //MethodCallExpr methodCallExpr = javaProjectEntity.getMainClass().getMainMethod().getMethodCallExprs().get(0);

            JarBuilder jarBuilder = new JarBuilder();
            //jarBuilder.createProjTree("/home/dord/Templates/emptyTestDirectory1");
            jarBuilder.mvnBuild();



        }catch (TooManyMainMethodsException e){
            System.err.print("There is more then one main method, please define a path to the single project");
            System.exit(-1);
        }

    }
}
;