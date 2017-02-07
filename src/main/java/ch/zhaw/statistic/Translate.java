package ch.zhaw.statistic;

public class Translate {
    private static int lambdaFunctionsNumber;

    public static int getLambdaFunctionsNumber() {
        return lambdaFunctionsNumber;
    }

    public static void setLambdaFunctionsNumber(int lambdaFunctionsNumber) {
        Translate.lambdaFunctionsNumber = lambdaFunctionsNumber;
    }

    public static void displayTranslationStatistic(){
        System.out.println("[Functions translation statistic:]\n\n  - " + lambdaFunctionsNumber + " Lambda Function projects were created.\n");
    }
}
