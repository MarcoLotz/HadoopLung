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
package com.marcolotz.lung.io.inputFormat;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

/**
 * A solution to the small files problem. This classes aglutinates local images
 * in order make a block size input split.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class MultipleFilesInputFormat extends
		CombineFileInputFormat<NullWritable, BytesWritable> {

	public MultipleFilesInputFormat() {
		super();
		// TODO: Read the max split size from the configuration file.
		setMaxSplitSize(134217728); // 128 MB, default block size Cloudera YARN.
	}

	@Override
	protected boolean isSplitable(JobContext context, Path file) {
		return false;
	}

	/**
	 * Creates a CombineFileRecordReader to read each file assigned to this
	 * InputSplit. Note, that unlike ordinary InputSplits, split must be a
	 * CombineFileSplit, and therefore is expected to specify multiple files.
	 * 
	 * @param split
	 *            The InputSplit to read. Throws an IllegalArgumentException if
	 *            this is not a CombineFileSplit.
	 * @param context
	 *            The context for this task.
	 * @return a CombineFileRecordReader to process each file in split. It will
	 *         read each file with a WholeFileRecordReader.
	 * @throws IOException
	 *             if there is an error.
	 */
	@Override
	public RecordReader<NullWritable, BytesWritable> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException {

		if (!(split instanceof CombineFileSplit)) {
			throw new IllegalArgumentException(
					"split must be a CombineFileSplit");
		}
		return new CombineFileRecordReader<NullWritable, BytesWritable>(
				(CombineFileSplit) split, context,
				MultipleFilesRecordReader.class);
	}

}

/*
 * public MultipleFilesInputFormat() { super(); // TODO: Read the max split size
 * from the configuration file. setMaxSplitSize(134217728); // 128 MB, default
 * block size on YARN. }
 * 
 * @Override protected boolean isSplitable(JobContext context, Path file) {
 * return false; }
 * 
 * @Override public RecordReader<LongWritable, BytesWritable>
 * createRecordReader( InputSplit split, TaskAttemptContext context) throws
 * IOException {
 * 
 * CombineFileRecordReader<LongWritable, BytesWritable> reader = new
 * CombineFileRecordReader<LongWritable, BytesWritable>( (CombineFileSplit)
 * split, context, MultipleFilesRecordReader.class);
 * 
 * return reader; }
 */

/*
 * public class CombineInputFormat extends CombineFileInputFormat {
 * 
 * @Override public RecordReader createRecordReader(InputSplit split,
 * TaskAttemptContext context) throws IOException { return new
 * CombineFileRecordReader((CombineFileSplit) split, context,
 * MultiFileRecordReader.class); }
 * 
 * @Override protected boolean isSplitable(JobContext context, Path file) {
 * return true; } }
 */
