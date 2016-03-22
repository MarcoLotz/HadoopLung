/*******************************************************************************
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

package com.marcolotz.lung.mapreduce.io.inputFormat;

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
