package ch.zhaw.statistic;

public class Upload {
    private static int builtProjectsNumber;
    private  static int lambdaFunctionsCreated;

    public static void setBuiltProjectsNumber(int builtProjectsNumber) {
        Upload.builtProjectsNumber = builtProjectsNumber;
    }

    public static void setLambdaFunctionsCreated(int lambdaFunctionsCreated) {
        Upload.lambdaFunctionsCreated = lambdaFunctionsCreated;
    }
    public static void countCreatedFunctions(){
        lambdaFunctionsCreated++;
    }
    public static void displayUploadStatistic(){
        float percentage;
        if (builtProjectsNumber != 0){
            percentage = (lambdaFunctionsCreated/builtProjectsNumber)*100;
        } else {
            percentage = 0;
        }
        System.out.println("\n[Upload statistic:]\n");
        System.out.println("  - " + builtProjectsNumber + " projects were prepared for uploading.\n");
        System.out.println("  - " + lambdaFunctionsCreated + " projects were successfully uploaded.\n");
        System.out.println("  - " + percentage + "% of the built projects were successfully uploaded.\n");
    }
}
