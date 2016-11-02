package ch.zhaw.file_operations;

import ch.zhaw.exceptions.TooManyMainMethodsException;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.Objects;

public class Visitor {
    final static int i = 0;
    static float f,f1;
    double d;
    final String s= "";
    public static void main(String[] args) throws Exception{
        NewProjectCreator newProjectCreator = new NewProjectCreator();
        newProjectCreator.copyProject();
        newProjectCreator.create();
    }


}
