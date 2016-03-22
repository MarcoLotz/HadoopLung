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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.log4j.Logger;

/**
 * The reader that {@link MultipleFilesInputFormat} uses.
 * @author Marco Aurelio Lotz
 *
 */
public class MultipleFilesRecordReader extends
		RecordReader<NullWritable, BytesWritable> {

	private static final Logger LOG = Logger
			.getLogger(MultipleFilesRecordReader.class);

	/** The path to the file to read. */
	private final Path mFileToRead;

	/** The length of this file. */
	private final long mFileLength;

	/** The Configuration. */
	private final Configuration mConf;

	/** Whether this FileSplit has been processed. */
	private boolean isProcessed;

	/** Sequence of bytes that represents a whole file */
	private final BytesWritable fileContent;

	/**
	 * Implementation detail: This constructor is built to be called via
	 * reflection from within CombineFileRecordReader.
	 * 
	 * @param fileSplit
	 *            The CombineFileSplit that this will read from.
	 * @param context
	 *            The context for this task.
	 * @param pathToProcess
	 *            The path index from the CombineFileSplit to process in this
	 *            record.
	 */
	public MultipleFilesRecordReader(CombineFileSplit fileSplit,
			TaskAttemptContext context, Integer pathToProcess) {
		isProcessed = false;

		mFileToRead = fileSplit.getPath(pathToProcess);
		mFileLength = fileSplit.getLength(pathToProcess);

		mConf = context.getConfiguration();

		/* never used in production, just for code integrity */
		assert 0 == fileSplit.getOffset(pathToProcess);

		if (LOG.isDebugEnabled()) {
			LOG.debug("FileToRead is: " + mFileToRead.toString());
			LOG.debug("Processing path " + pathToProcess + " out of "
					+ fileSplit.getNumPaths());

			try {
				FileSystem fs = FileSystem.get(mConf);

				/* never used in production, just for code integrity */
				assert fs.getFileStatus(mFileToRead).getLen() == mFileLength;
			} catch (IOException ioe) {
				LOG.debug("Problem in file length");
			}
		}

		fileContent = new BytesWritable();
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		/*
		 * The file should closed right after it has been read. Thus performs
		 * nothing.
		 */
	}

	/***
	 * There is no key in this case. One can change this in the future to return
	 * the absolute path of the processed file.
	 */
	@Override
	public NullWritable getCurrentKey() throws IOException,
			InterruptedException {
		return NullWritable.get();
	}

	/**
	 * <p>
	 * Returns the current value. If the file has been read with a call to
	 * NextKeyValue(), this returns the contents of the file as a BytesWritable.
	 * Otherwise, it returns an empty BytesWritable.
	 * </p>
	 * 
	 * <p>
	 * Throws an IllegalStateException if initialize() is not called first.
	 * </p>
	 * 
	 * @return A BytesWritable containing the contents of the file to read.
	 * @throws IOException
	 *             never.
	 * @throws InterruptedException
	 *             never.
	 */
	@Override
	public BytesWritable getCurrentValue() throws IOException,
			InterruptedException {
		return fileContent;
	}

	/**
	 * Returns whether the file has been processed or not. Since only one record
	 * will be generated for a file, progress will be 0.0 if it has not been
	 * processed, and 1.0 if it has.
	 * 
	 * @return 0.0 if the file has not been processed. 1.0 if it has.
	 * @throws IOException
	 *             never.
	 * @throws InterruptedException
	 *             never.
	 */
	@Override
	public float getProgress() throws IOException, InterruptedException {
		return (isProcessed) ? (float) 1.0 : (float) 0.0;
	}

	/**
	 * All of the internal state is already set on instantiation. This is a
	 * no-op.
	 * 
	 * @param split
	 *            The InputSplit to read. Unused.
	 * @param context
	 *            The context for this task. Unused.
	 * @throws IOException
	 *             never.
	 * @throws InterruptedException
	 *             never.
	 */
	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		// no-op.
	}

	/**
	 * <p>
	 * If the file has not already been read, this reads it into memory, so that
	 * a call to getCurrentValue() will return the entire contents of this file
	 * as Text, and getCurrentKey() will return the qualified path to this file
	 * as Text. Then, returns true. If it has already been read, then returns
	 * false without updating any internal state.
	 * </p>
	 * 
	 * @return Whether the file was read or not.
	 * @throws IOException
	 *             if there is an error reading the file.
	 * @throws InterruptedException
	 *             if there is an error.
	 */
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (!isProcessed) {
			if (mFileLength > (long) Integer.MAX_VALUE) {
				throw new IOException("File is longer than Integer.MAX_VALUE.");
			}
			byte[] contents = new byte[(int) mFileLength];

			FileSystem fs = mFileToRead.getFileSystem(mConf);

			FSDataInputStream in = null;
			try {
				// Set the contents of this file.
				in = fs.open(mFileToRead);
				IOUtils.readFully(in, contents, 0, contents.length);
				fileContent.set(contents, 0, contents.length);

			} finally {
				IOUtils.closeStream(in);
			}
			isProcessed = true;
			return true;
		}
		return false;
	}

}
