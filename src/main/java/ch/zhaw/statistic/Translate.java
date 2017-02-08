package ch.zhaw.statistic;

import ch.zhaw.time.TranslationTimer;

public class Translate {
    private static int lambdaFunctionsNumber;
    private static float percentage;

    public static int getLambdaFunctionsNumber() {
        return lambdaFunctionsNumber;
    }

    public static void setLambdaFunctionsNumber(int lambdaFunctionsNumber) {
        Translate.lambdaFunctionsNumber = lambdaFunctionsNumber;
    }

    public static void setPercentage(float percentage) {
        Translate.percentage = percentage;
    }

    public static void displayTranslationStatistic(){
        System.out.println("[Functions translation statistic:]\n\n  - " + lambdaFunctionsNumber + " Lambda Function projects were created.\n");
        System.out.println("  Finished in: " + TranslationTimer.getFormattedTime() + "\n");
    }
}
