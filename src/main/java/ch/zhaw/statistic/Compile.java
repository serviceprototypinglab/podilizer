package ch.zhaw.statistic;

public class Compile {
    private static int builtProjects;
    private static int translatedProjectsNumber;

    public static void setTranslatedProjectsNumber(int translatedProjectsNumber) {
        Compile.translatedProjectsNumber = translatedProjectsNumber;
    }

    public static void countProject(){
        builtProjects++;
    }

    public static void displayCompileStatistic(){
        float percentage;
        if (translatedProjectsNumber != 0){
            percentage = (builtProjects/translatedProjectsNumber)*100;
        } else {
            percentage = 0;
        }
        System.out.println("\n[Compilation statistic:]\n");
        System.out.println("  - " + translatedProjectsNumber + " projects were prepared for compiling.\n");
        System.out.println("  - " + builtProjects + " projects were successfully compiled.\n");
        System.out.println("  - " + percentage + "% of projects were successfully compiled.\n");
    }
}
