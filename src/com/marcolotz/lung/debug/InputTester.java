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
package com.marcolotz.lung.debug;

import ij.plugin.DICOM;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;

import com.marcolotz.lung.io.inputFormat.WholeFileInputFormat;

/**
 * Class used for debuging the input given by the record reader.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
@SuppressWarnings("deprecation")
public class InputTester {

	public InputTester() {
	};

	/***
	 * Method used for local testing the record reader and the Input format. It
	 * generates an input split from the local file system file.
	 * 
	 * @param filePath
	 */
	public void localTest(String filePath) {
		DICOM image;
		Configuration testConf = new Configuration(false);

		/* Reads the local file system */
		testConf.set("fs.default.name", "file:///");

		File testFile = new File(filePath);

		Path path = new Path(testFile.getAbsoluteFile().toURI());
		FileSplit split = new FileSplit(path, 0, testFile.length(), null);

		InputFormat<NullWritable, BytesWritable> inputFormat = ReflectionUtils
				.newInstance(WholeFileInputFormat.class, testConf);
		TaskAttemptContext context = new TaskAttemptContextImpl(testConf,
				new TaskAttemptID());

		try {
			RecordReader<NullWritable, BytesWritable> reader = inputFormat
					.createRecordReader(split, context);
			while (reader.nextKeyValue()) {
				/* get the bytes array */
				BytesWritable inputBytesWritable = (BytesWritable) reader
						.getCurrentValue();
				byte[] inputContent = inputBytesWritable.getBytes();

				/* Check for Correct value */
				// generateLocalOutput("path/to/output");
				
				InputStream is = new ByteArrayInputStream(inputContent);

				image = new DICOM(is);
				image.run("Dicom Test");
				
				/* Prints the bytes as an ImagePlus image */
				ImageViewer debug = new ImageViewer();
				debug.setImage(image);
			}
		} catch (Exception e) {

		}
	}

	/***
	 * Writes the byte content of the input stream to the local file system.
	 * @param filePath
	 * @param outputContent
	 */
	public void generateLocalOutput(String filePath, byte[] outputContent) {
		try {
			FileOutputStream output = new FileOutputStream(new File(filePath));
			IOUtils.write(outputContent, output);
		} catch (IOException e) {
			System.err.println("Error during debug output process.");
			e.printStackTrace();
		}
	}
}
