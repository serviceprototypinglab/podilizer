package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.Objects;

public class Visitor {
    public static void main(String[] args) throws Exception{
        JavaProjectEntity javaProjectEntity = new JavaProjectEntity(Paths.get(ConfigReader.getConfig().getPath()));

        JarUploader jarUploader = new JarUploader(ConfigReader.getConfig().getFileName(), "/home/dord/LambdaA.zip", "example.LambdaA::handleRequest", 30, 1024);
        jarUploader.uploadFunction();

        try{
            System.out.println(javaProjectEntity.getMainClass());

            /* Build jar

            JarBuilder jarBuilder = new JarBuilder();
            jarBuilder.createJar("/home/dord/Templates/emptyTestDirectory/");
            */

        }catch (TooManyMainMethodsException e){
            System.err.print("There is more then one main method, please define a path to the single project");
            System.exit(-1);
        }

    }

}
