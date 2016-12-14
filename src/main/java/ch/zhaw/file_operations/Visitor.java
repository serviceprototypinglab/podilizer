package ch.zhaw.file_operations;

public class Visitor {

    public static void main(String[] args) throws Exception {
        if (args.length == 1 & args[0].equals("-help")){
            showHelp();
            return;
        }
        if (args.length < 3 | args.length > 5) {
            showUsage();
            return;
        }
        if (!args[args.length - 2].equals("-conf")){
            showUsage();
            return;
        }
        String mod = args[0];
        NewProjectCreator newProjectCreator;
        switch (mod) {
            case "-t":
                if (args.length == 5){
                    newProjectCreator = new NewProjectCreator(args[1], args[2], false, args[4]);
                    newProjectCreator.copyProject();
                    break;
                }
                if (args.length == 4){
                    newProjectCreator = new NewProjectCreator(args[1], false, args[3]);
                    newProjectCreator.copyProject();
                    break;
                }
                newProjectCreator = new NewProjectCreator(false, args[2]);
                newProjectCreator.copyProject();
                break;
            case "-tu":
                if (args.length == 5){
                    newProjectCreator = new NewProjectCreator(args[1], args[2], true, args[4]);
                    newProjectCreator.copyProject();
                    break;
                }
                if (args.length == 4){
                    newProjectCreator = new NewProjectCreator(args[1], true, args[3]);
                    newProjectCreator.copyProject();
                    break;
                }
                newProjectCreator = new NewProjectCreator(true, args[2]);
                newProjectCreator.copyProject();
                break;
            default:
                showUsage();
                break;
        }
    }

    private static void showUsage() {
        System.out.println("\nUsage: \n\tjava -jar <built archive name> <option> [[option attribute]...] -conf <configs folder path>\n" +
                "\nFor more information run with option '-help'\n");
    }
    private static void showHelp(){
        String helpString = "Options:\n" +
                "\t '-t'  - translate the project using path configurations from config file\n" +
                "\t '-t <result directory path>' - translate the project from current path to <result directory path>\n" +
                "\t '-t <source directory path> <result directory path>' - translate the project from <source directory path> " +
                "to <result directory path>\n" +
                "\n Use option '-tu' instead of '-t' to upload created Lambda Functions after translation";
        showUsage();
        System.out.println(helpString);
    }
}
