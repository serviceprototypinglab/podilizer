package ch.zhaw.file_operations;

import java.io.File;

public class Visitor {

    public static void main(String[] args) throws Exception {
        if (args[0].equals("-help")) {
            showHelp();
            return;
        }
        if (args.length > 5) {
            showUsage();
            return;
        }
        String confPath;
        if (contains(args)) {
            confPath = args[args.length - 1];
        } else {
            confPath = new File(".").getAbsolutePath();
        }


        String mod = args[0];
        NewProjectCreator newProjectCreator;
        switch (mod) {
            case "-t":
                if (args.length == 5) {
                    newProjectCreator = new NewProjectCreator(args[1], args[2], false, confPath);
                    newProjectCreator.copyProject();
                    break;
                }
                if (args.length == 4) {
                    newProjectCreator = new NewProjectCreator(args[1], false, confPath);
                    newProjectCreator.copyProject();
                    break;
                }
                if (args.length == 1) {
                    newProjectCreator = new NewProjectCreator(false, confPath);
                    newProjectCreator.copyProject();
                    break;
                }
            case "-tu":
                if (args.length == 5) {
                    newProjectCreator = new NewProjectCreator(args[1], args[2], true, confPath);
                    newProjectCreator.copyProject();
                    break;
                }
                if (args.length == 4) {
                    newProjectCreator = new NewProjectCreator(args[1], true, confPath);
                    newProjectCreator.copyProject();
                    break;
                }
                newProjectCreator = new NewProjectCreator(true, confPath);
                newProjectCreator.copyProject();
                break;
            default:
                showUsage();
                break;
        }
    }

    private static void showUsage() {
        System.out.println("\nUsage: \n\tjava -jar <built archive name> <option> [[option attribute]...] [-conf <configs folder path>]\n" +
                "\nFor more information run with option '-help'\n");
    }

    private static void showHelp() {
        String helpString = "Options:\n" +
                "\t '-t'  - translate the project using path configurations from config file\n" +
                "\t '-t <result directory path>' - translate the project from current path to <result directory path>\n" +
                "\t '-t <source directory path> <result directory path>' - translate the project from <source directory path> " +
                "to <result directory path>\n" +
                "\n Use option '-tu' instead of '-t' to upload created Lambda Functions after translation";
        showUsage();
        System.out.println(helpString);
    }

    private static boolean contains(String[] array) {
        boolean result = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals("-conf")) {
                result = true;
            } else {
                result = false;
            }
        }
        return result;
    }
}
