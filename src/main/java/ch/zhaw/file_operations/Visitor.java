package ch.zhaw.file_operations;

public class Visitor {

    public static void main(String[] args) throws Exception{
        if (args.length != 1){
            System.out.println("Wrong execution parameters! \n" +
                    "Usage: java -jar <built archive name> translate");
            return;
        }
        String mod = args[0];
        NewProjectCreator newProjectCreator;
        switch (mod){
            case "translate":  newProjectCreator = new NewProjectCreator(false);
                newProjectCreator.copyProject();
                break;
            case "upload":  newProjectCreator = new NewProjectCreator(true);
                newProjectCreator.copyProject();
                break;
            default: System.out.println("Wrong execution parameters! \n" +
                    "Usage: java -jar <built archive name> translate");
                break;
        }
    }
}
