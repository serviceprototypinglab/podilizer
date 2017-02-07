package ch.zhaw.statistic;

public class Parse {
    private static int filesNumber;
    private static int methodsNumber;

    public static int getFilesNumber() {
        return filesNumber;
    }

    public static int getMethodsNumber() {
        return methodsNumber;
    }

    public static void setFilesNumber(int filesNumber) {
        Parse.filesNumber = filesNumber;
    }

    public static void setMethodsNumber(int methodsNumber) {
        Parse.methodsNumber = methodsNumber;
    }

    public static void displayFilesStatistic(){
        System.out.println("[Code analysis statistic:]\n\n  - " + filesNumber + " '.java' files was/were found in the input project directory.\n");
    }
    public static void displayMethodsStatistic(){
        System.out.println("[Code decomposition statistic:]\n\n  - " + methodsNumber + " methods was/were found in the input java project.\n");
    }
}
