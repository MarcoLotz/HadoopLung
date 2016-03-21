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
