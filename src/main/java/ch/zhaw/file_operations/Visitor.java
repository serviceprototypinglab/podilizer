package ch.zhaw.file_operations;

public class Visitor {

    public static void main(String[] args) throws Exception {
        if (args.length < 1 | args.length > 3) {
            showUsage();
            return;
        }
        String mod = args[0];
        NewProjectCreator newProjectCreator;
        switch (mod) {
            case "-t":
                if (args.length == 3){
                    newProjectCreator = new NewProjectCreator(args[1], args[2], false);
                    newProjectCreator.copyProject();
                    break;
                }
                if (args.length == 2){
                    newProjectCreator = new NewProjectCreator(args[1], false);
                    newProjectCreator.copyProject();
                    break;
                }
                newProjectCreator = new NewProjectCreator(false);
                newProjectCreator.copyProject();
                break;
            case "-tu":
                if (args.length == 3){
                    newProjectCreator = new NewProjectCreator(args[1], args[2], true);
                    newProjectCreator.copyProject();
                    break;
                }
                if (args.length == 2){
                    newProjectCreator = new NewProjectCreator(args[1], true);
                    newProjectCreator.copyProject();
                    break;
                }
                newProjectCreator = new NewProjectCreator(true);
                newProjectCreator.copyProject();
                break;
            case "help":
                showHelp();
                break;
            default:
                showUsage();
                break;
        }
    }

    private static void showUsage() {
        System.out.println("Usage: java -jar <built archive name> <option> [[option attribute]...]\n");
    }
    private static void showHelp(){
        String helpString = "Options:\n" +
                "\t -t  - translate the project using path configurations from config file\n" +
                "\t -t <result directory path> - translate the project from current path to <result directory path>\n" +
                "\t -t <source directory path> <result directory path> - translate the project from <source directory path> " +
                "to <result directory path>\n" +
                "\n\t Option '-tu' does the same as option '-t' but uploads created Lambda Functions after translation";
        showUsage();
        System.out.println(helpString);
    }
}
