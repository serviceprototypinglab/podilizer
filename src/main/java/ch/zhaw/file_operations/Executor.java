package ch.zhaw.file_operations;

import javafx.util.Pair;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Executor {
    private Options options = new Options();
    private HelpFormatter helpFormatter;
    private Pair<String, String> optionT = new Pair<>("t", "translate");
    private Pair<String, String> optionB = new Pair<>("b", "build");
    private Pair<String, String> optionU = new Pair<>("u", "upload");
    private long time;

    public Executor(){
        options.addOption(Option.builder(optionT.getKey()).longOpt(optionT.getValue()).numberOfArgs(2).
                desc("Translate the project. Takes two arguments: input and output projects paths").build());
        options.addOption(Option.builder(optionB.getKey()).longOpt(optionB.getValue()).hasArg().numberOfArgs(2).
                desc("Build the project. Takes two arguments:" +
                        " translated project to build path and path of configured pom.xml file").build());
        options.addOption(Option.builder(optionU.getKey()).longOpt(optionU.getValue()).hasArg().numberOfArgs(2).
                desc("Upload the project. Takes two arguments: translated and built project path and .yml conf file path").build());
        options.addOption(Option.builder("help").hasArg(false).desc("output help text").numberOfArgs(0).build());
        helpFormatter = new HelpFormatter();
    }

    public void executeWithArguments(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = commandLineParser.parse(options, args);
            if (getArgNames(cmd).size() == 0){
                System.err.println("\nThere is no options to run. Please use -help for details.\n");
            } else {
                for (String option :
                        getArgNames(cmd)) {
                    optionCheck(cmd, option);
                }
            }

        } catch (AlreadySelectedException e){
            System.out.println("\nToo many options are used. It's allowed to use only one option\n");
            helpFormatter.printHelp("Podilizer", options, true);
            System.exit(1);
        }
        catch (ParseException e) {
            System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
            helpFormatter.printHelp("Podilizer", options, true);
            System.exit(1);
        }
        if (cmd.hasOption("help")){
            helpFormatter.printHelp("Podilizer", options, true);
            System.exit(1);
        }
        String t = optionT.getKey();
        String b = optionB.getKey();
        String u = optionU.getKey();
        if (cmd.hasOption(t) & cmd.hasOption(b) & cmd.hasOption(u)){
            translateWithArgs(cmd);
            buildWithArgs(cmd);
            uploadWithArgs(cmd);
            return;
        }
        if (cmd.hasOption(t) & cmd.hasOption(u)){
            translateWithArgs(cmd);
            System.out.println("You can not upload functions without building the projects, please use -b option");
            return;
        }
        if (cmd.hasOption(t) & cmd.hasOption(b)){
            translateWithArgs(cmd);
            buildWithArgs(cmd);
            return;
        }
        if (cmd.hasOption(b) & cmd.hasOption(u)){
            buildWithArgs(cmd);
            uploadWithArgs(cmd);
            return;
        }
        if (cmd.hasOption(t)){
            translateWithArgs(cmd);
            return;
        }
        if (cmd.hasOption(b)){
            buildWithArgs(cmd);
            return;
        }
        if (cmd.hasOption(u)){
            uploadWithArgs(cmd);
        }
    }
    private List<String> getArgNames(CommandLine cmd){
        List<String> result = new ArrayList<>();
        Option[] options = cmd.getOptions();
        for (int i = 0; i < options.length; i++){
            result.add(options[i].getOpt());
        }
        return result;
    }
    private void optionCheck(CommandLine cmd, String option){
        if (cmd.getOptionProperties(option) == null &&
                cmd.getOptionProperties(option).getProperty(cmd.getOptionValue(option)).equals("true")){
            System.err.println("\nWrong usage.\n");
            helpFormatter.printHelp("Podilize", options, true);
            System.exit(1);
        }
    }
    private void translateWithArgs(CommandLine cmd){
        System.out.println("\n---Translation started---\n");
        startCount();
        String inPath = cmd.getOptionValues(optionT.getKey())[0];
        String outPAth = cmd.getOptionValues(optionT.getKey())[1];

        Translator translator = new Translator(inPath, outPAth);
        translator.translate();
        System.out.println("\n---Translation finished in " + calculateTime() + "---\n");
    }
    private void buildWithArgs(CommandLine cmd){
        System.out.println("\n---Building started.---\n");
        startCount();
        String outPath = cmd.getOptionValues(optionB.getKey())[0];
        String pomPath = cmd.getOptionValues(optionB.getKey())[1];

        Builder builder = new Builder(outPath, pomPath);
        builder.build();
        System.out.println("\n---Building finished in " + calculateTime() + "---\n");
    }
    private void uploadWithArgs(CommandLine cmd){
        System.out.println("\n---Lambda functions creating started---\n");
        startCount();
        String outPath = cmd.getOptionValues(optionU.getKey())[0];
        String confPath = cmd.getOptionValues(optionU.getKey())[1];

        LambdaCreator lambdaCreator = new LambdaCreator(outPath, confPath);
        lambdaCreator.create();
        System.out.println("\n---Lambda functions creating finished in " + calculateTime() + "---\n");
    }
    private void startCount(){
        time = System.currentTimeMillis();
    }
    private String calculateTime(){
        long millis = System.currentTimeMillis() - time;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        long milliseconds = millis - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);
        return String.format("%02d min,%02d sec,%03d millisec", minutes,
                seconds, milliseconds);
    }
}
