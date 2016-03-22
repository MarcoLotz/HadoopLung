/******************************************************************************
 * Copyright (c) 2002-2016 "Marco Aurelio Barbosa Fagnani Gomes Lotz"
 * [http://www.marcolotz.com]
 *
 * This file is part of Marco Lotz Hadoop Lung solution.
 *
 * Hadoop Lung is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.marcolotz.lung.mapreduce.MRCore;

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
						"com.marcolotz.imageprocess.grayNoduleCandidates.bottomThreshold",
						110));
		System.out
				.println("Top Threshold for nodules candidates detection:"
						+ conf.getInt(
								"com.marcolotz.imageprocess.grayNoduleCandidates.topThreshold",
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

		conf.setInt("com.marcolotz.imageprocess.grayNoduleCandidates.bottomThreshold",
				bottomThreshold);
		conf.setInt("com.marcolotz.imageprocess.grayNoduleCandidates.topThreshold",
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
