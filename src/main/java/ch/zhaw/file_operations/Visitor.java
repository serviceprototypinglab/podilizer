package ch.zhaw.file_operations;

public class Visitor {
    private final int finalField = 0;
    public static void main(String[] args) throws Exception{
        NewProjectCreator newProjectCreator = new NewProjectCreator();
        newProjectCreator.copyProject();
    }


}
