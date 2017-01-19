package ch.zhaw.file_operations;


public class Visitor {

    public static void main(String[] args) throws Exception {
        Executor executor = new Executor();
        executor.executeWithArguments(args);
    }
}
