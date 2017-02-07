package ch.zhaw.statistic;

public class Compile {
    private static int builtProjects;

    public static void countProject(){
        builtProjects++;
    }

    public static void displayCompileStatistic(int translatedProjectsNumber){
        float percentage = (builtProjects/translatedProjectsNumber)*100;
        System.out.println("\n[Compilation statistic:]\n");
        System.out.println("  - " + translatedProjectsNumber + " projects were prepared for compiling.\n");
        System.out.println("  - " + builtProjects + " projects were successfully compiled.\n");
        System.out.println("  - " + percentage + "% of projects were successfully compiled.\n");
    }
}
