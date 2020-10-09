package hu.fzsombor;

import hu.fzsombor.backend.WorkloadParser;
import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Options options = new Options();

        // TODO: maybe better to separate with commas
        Option workloadsOpt = new Option("wl", "workloads", true, "Workloads passed as a list of workload_dir=percentage separated by whitespaces");
        workloadsOpt.setArgs(Option.UNLIMITED_VALUES);
        workloadsOpt.setRequired(true);
        options.addOption(workloadsOpt);

        Option formatOpt = new Option("f", "format", true, "File format. One of the following text, parquet, orc or kudu");
        formatOpt.setRequired(true);
        options.addOption(formatOpt);

        Option sizeOpt = new Option("s", "size", true, "Size of the total space can be used to run the benchmark. Recommended value is 80% of total free HDFS space.");
        sizeOpt.setRequired(true);
        options.addOption(sizeOpt);

        Option durationOpt = new Option("d", "duration", true, "The duration of the benchmark. Workloads will restart if the total elapsed time is lower than the duration. After duration elapsed, no new workloads will start, but the already running ones will finish before exiting");
        durationOpt.setRequired(true);
       options.addOption(durationOpt);


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String workloads = cmd.getOptionValue("wl");
            List<String> workloadList = Arrays.asList(workloads.split("\\s*"));
            String fileFormat = cmd.getOptionValue("f");
            // TODO: make the default value the 80% of the cluster space from the DB
            int size = Integer.parseInt(cmd.getOptionValue("s"));
            int duration = Integer.parseInt(cmd.getOptionValue("d"));
            // TODO: change this variable with global vars
            String format = cmd.getOptionValue("f");


            WorkloadParser workloadParser = new WorkloadParser();

            for (String workload: workloadList) {
                workloadParser.readAndExecuteWorkload("workloads/"+workload+"/workload.xml", size, duration, format );
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Big data benchmark framework backend", options);
            System.exit(1);
        }



    }
}
