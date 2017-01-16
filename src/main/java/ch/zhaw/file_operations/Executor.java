package ch.zhaw.file_operations;

import javafx.util.Pair;
import org.apache.commons.cli.*;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;

public class Executor {
    private Options options = new Options();
    private HelpFormatter helpFormatter;
    private Pair<String, String> optionT = new Pair<>("t", "translate");
    private Pair<String, String> optionB = new Pair<>("b", "build");
    private Pair<String, String> optionU = new Pair<>("u", "upload");

    public Executor(){
        OptionGroup optionGroup = new OptionGroup();
        optionGroup.addOption(Option.builder(optionT.getKey()).longOpt(optionT.getValue()).numberOfArgs(2).
                desc("translate the project").required().build());
        Arg arg = new Commandline.Argument();
        optionGroup.addOption(Option.builder(optionB.getKey()).longOpt(optionB.getValue()).hasArg().numberOfArgs(2).
                desc("build the project").required().build());
        optionGroup.addOption(Option.builder(optionU.getKey()).longOpt(optionU.getValue()).hasArg().numberOfArgs(2).
                desc("upload the project").required().build());
        optionGroup.addOption(Option.builder("help").hasArg(false).desc("output help text").numberOfArgs(0).build());
        options.addOptionGroup(optionGroup);
        helpFormatter = new HelpFormatter();
    }

    public void executeWithArguments(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = commandLineParser.parse(options, args);
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

        if (cmd.hasOption(optionT.getKey())){
            optionCheck(cmd, optionT.getKey());
            String inPath = cmd.getOptionValues(optionT.getKey())[0];
            String outPAth = cmd.getOptionValues(optionT.getKey())[1];

            Translator translator = new Translator(inPath, outPAth);
            translator.translate();
        }
        if (cmd.hasOption(optionB.getKey())){
            optionCheck(cmd, optionB.getKey());
            String outPath = cmd.getOptionValues(optionB.getKey())[0];
            String pomPath = cmd.getOptionValues(optionB.getKey())[1];

            Builder builder = new Builder(outPath, pomPath);
            builder.build();
        }
        if (cmd.hasOption(optionU.getKey())){
            optionCheck(cmd, optionU.getKey());
            String outPath = cmd.getOptionValues(optionU.getKey())[0];
            String confPath = cmd.getOptionValues(optionU.getKey())[1];

            LambdaCreator lambdaCreator = new LambdaCreator(outPath, confPath);
            lambdaCreator.create();

        }
    }
    private void optionCheck(CommandLine cmd, String option){
        if (cmd.getOptionProperties(option).getProperty(cmd.getOptionValue(option)).equals("true")){
            System.err.println("\nWrong usage.\n");
            helpFormatter.printHelp("Podilize", options, true);
            System.exit(1);
        }
    }
}
