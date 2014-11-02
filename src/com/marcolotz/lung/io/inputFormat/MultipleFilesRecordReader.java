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
