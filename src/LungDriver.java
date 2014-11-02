/*******************************************************************************
 * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * 
 * Copyright (c) 2014 Marco Aurelio Barbosa Fagnani Gomes Lotz (marcolotz.com)
 * 
 * The source code in this document is licensed under Creative Commons
 * Attribution-NonCommercial-ShareAlike 4.0 International License. You must 
 * credit the author of the source code in the way specified by the author or
 * licenser (but not in a way to suggest that the author or licenser has given 
 * you allowance to you or to your use of the source code). If you modify,
 * transform or create using this source code as basis, you can only distribute
 * the new source code under the same license or a similar license to this one.
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * To see a copy of the license, access:
 * creativecommons.org/licenses/by-nc-sa/4.0/legalcode
 ******************************************************************************/
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Main Launcher for the Hadoop Application. The XML file with the
 * configurations is added here.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class LungDriver extends Configured implements Tool {

	/* Block executed when the class is created */
	static {
		/*
		 * The lungConfiguration.xml contains all the user defined
		 * configurations desired for a Hadoop job Don't forget to include it on
		 * the class path
		 */
		Configuration.addDefaultResource("./lungConfiguration.xml");
	}

	@Override
	public int run(String[] args) throws Exception {

		Configuration conf = getConf();

		String author = conf.get("com.marcolotz.author");
		String jobName = conf.get("mapreduce.job.name");
		String inputPath = conf.get("mapred.input.dir");
		String outputPath = conf.get("mapred.output.dir");

		System.out.println("\nApplication author: " + author + "\n");

		System.out
				.println("Configurations stored at: conf/lungConfiguration.xml");
		System.out.println("Input path: " + inputPath);
		System.out.println("Output path: " + outputPath);

		/* For non-standard operation (i.e. with arguments) */
		if (args.length != 0) {

			/* terminates the program if there is an incorrect input */
			if (processInputs(args, conf) != 0) {
				return 1;
			}
		}

		System.out.println("Bottom Threshold for nodules candidates detection:"
				+ conf.getInt(
						"com.marcolotz.grayNoduleCandidates.bottomThreshold",
						110));
		System.out
				.println("Top Threshold for nodules candidates detection:"
						+ conf.getInt(
								"com.marcolotz.grayNoduleCandidates.topThreshold",
								120));

		System.out.print("Cleaning output path: ");
		cleanOutputPath(conf, outputPath);

		System.out.print("Configuring the job " + jobName + ": ");

		/* Makes a new job */
		// The classic Job constructor is deprecated.
		Job job = Job.getInstance(conf);

		/*
		 * This method sets the jar file in which each node will look for the
		 * Mapper and Reducer classes.
		 */
		job.setJarByClass(this.getClass());

		System.out.println("[DONE]\n");

		// Submits the job to the cluster
		System.out.println("Distributing the job:");
		return job.waitForCompletion(true) ? 0 : 1;
	}

	/***
	 * Method only called by the application when arguments are used
	 * 
	 * @param args
	 * @param conf
	 * @return the System status
	 */
	private int processInputs(String[] args, Configuration conf) {
		if (args.length != 2) {
			printUsage();
			return 1;
		}

		int bottomThreshold = Integer.parseInt(args[0]);
		int topThreshold = Integer.parseInt(args[1]);

		if (bottomThreshold > topThreshold) {
			System.err
					.println("Wrong treshold usage. The bottom limit must be under the top timit.");
			printUsage();
			return 1;
		}

		conf.setInt("com.marcolotz.grayNoduleCandidates.bottomThreshold",
				bottomThreshold);
		conf.setInt("com.marcolotz.grayNoduleCandidates.topThreshold",
				topThreshold);

		return 0;
	}

	private void printUsage() {
		System.err
				.println("Wrong usage. One can use the no arguments calling for 110-120 gray threshold values or:");
		System.err.println(LungDriver.class.getSimpleName()
				+ " [bottom Gray threshold] [top GrayThreshold]");

	}

	private void cleanOutputPath(Configuration conf, String outputPath) {
		try {
			FileSystem fs = FileSystem.get(conf);
			Path output = new Path(outputPath);
			fs.delete(output, true);
		} catch (IOException e) {
			System.err.println("Failed to delete temporary path");
			e.printStackTrace();
		}

		System.out.println("[DONE]\n");
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new LungDriver(), args);
		System.exit(exitCode);
	}
}
