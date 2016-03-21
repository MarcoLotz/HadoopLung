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

package com.marcolotz.lung.io.inputFormat;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * Writes the whole file as a value for a Null key. This implementation is
 * described in the Tom White Hadoop book.
 * 
 * This WholeFileRecordReader may the compatible with the
 * {@link MultipleFilesInputFormat}, but no checks were performed so far.
 * 
 * @author Marco Aurelio Lotz
 * 
 */

class WholeFileRecordReader extends RecordReader<NullWritable, BytesWritable> {
	private FileSplit fileSplit;
	private Configuration conf;
	private BytesWritable value = new BytesWritable();
	private boolean processed = false;

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		this.fileSplit = (FileSplit) split;
		this.conf = context.getConfiguration();
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		/* if that record reader for that input split was not called yet */
		if (!processed) {
			byte[] contents = new byte[(int) fileSplit.getLength()];

			Path file = fileSplit.getPath();

			// Reads from the conf file what is the desired file system.
			FileSystem fs = file.getFileSystem(conf);

			FSDataInputStream in = null;

			try {
				in = fs.open(file);
				IOUtils.readFully(in, contents, 0, contents.length);
				value.set(contents, 0, contents.length);
			} finally {
				IOUtils.closeStream(in);
			}

			processed = true;
			return true;
		}
		return false;
	}

	@Override
	public NullWritable getCurrentKey() throws IOException,
			InterruptedException {
		return NullWritable.get();
	}

	@Override
	public BytesWritable getCurrentValue() throws IOException,
			InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException {
		return processed ? 1.0f : 0.0f;
	}

	@Override
	public void close() throws IOException {
		/*
		 * The file should closed right after it has been read. Thus performs
		 * nothing.
		 */
	}

}
