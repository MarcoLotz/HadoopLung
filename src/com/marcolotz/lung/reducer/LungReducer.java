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
package com.marcolotz.lung.reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import com.marcolotz.MRComponents.KeyStructureWritable;
import com.marcolotz.MapperComponents.ImageMetadata;
import com.marcolotz.lung.io.outputFormat.SeriesDataWritable;
import com.marcolotz.ReducerComponents.ReducedValueWritable;

/**
 * The reducer used in HadoopLung. It manager series meta informations.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class LungReducer
		extends
		Reducer<KeyStructureWritable, ImageMetadata, SeriesDataWritable, NullWritable> {
	@Override
	protected void reduce(KeyStructureWritable inputKey,
			Iterable<ImageMetadata> values, Context context)
			throws IOException, InterruptedException {

		ReducedValueWritable reducedValue = new ReducedValueWritable();

		Iterator<ImageMetadata> itr = values.iterator();

		ArrayList<ImageMetadata> sortedList = new ArrayList<ImageMetadata>();

		/*
		 * Generates a new list used for sorting
		 * 
		 * Careful: This may load all the values for the same key into memory.
		 * In this application this is not a problem due to the value size, but
		 * may cause failures in other types of applications
		 */
		while (itr.hasNext()) {
			/*
			 * One needs a buffer otherwise the iterator will always send the
			 * same element to the list. This is due to the fact the an iterator
			 * in hadoop behaves a little different than an usual one, since
			 * sometimes the data is on disk and sometimes its on memory.
			 */

			// Clones iterator content
			ImageMetadata buffer = new ImageMetadata(itr.next());

			sortedList.add(buffer);
		}

		// sorted based on the Image Number attribute
		Collections.sort(sortedList);

		Iterator<ImageMetadata> sortedItr = sortedList.iterator();

		/* Add the values the reduced List, once they are ordered */
		while (sortedItr.hasNext()) {
			reducedValue.addToReducedList(sortedItr.next());
		}

		/* Generates the job output structure */
		SeriesDataWritable seriesData = new SeriesDataWritable(inputKey,
				reducedValue);

		/* Emits the structure */
		context.write(seriesData, NullWritable.get());
	}
}
