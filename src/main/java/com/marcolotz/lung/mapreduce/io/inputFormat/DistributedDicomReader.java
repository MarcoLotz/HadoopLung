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

import com.marcolotz.lung.mapreduce.MRComponents.KeyStructureWritable;
import com.marcolotz.lung.mapreduce.MapperComponents.ImageStructure;

import ij.plugin.DICOM;

/**
 * DICOM specialized format reader. Reads the image data and also the metadata
 * inside the DICOM image. It is a variation of the DicomReader class, without
 * verbose and with a different constructor.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class DistributedDicomReader {

	private DICOM source = new DICOM();

	// Time Variables
	private float startTime;
	private float totalLatency;

	// DICOM series info, used as the main Map and Reduce key.
	private KeyStructureWritable keyStructure;

	// DICOM image info, used as the Map value.
	ImageStructure imageStructure;

	public DistributedDicomReader(DICOM image) {

		startTime = System.currentTimeMillis();

		source = image;

		if (source != null) {
			keyStructure = new KeyStructureWritable(source);

			imageStructure = new ImageStructure(source);
		} else {
			// TODO: Make a throw exception here.
			// System.out.println("Problem reading the DICOM image.");
		}

		totalLatency = System.currentTimeMillis() - startTime;
	}

	@Override
	public String toString() {
		String buffer = new String("");
		buffer = "Source address: ";
		buffer = buffer + "Total latency Image Reading (ms): "
				+ this.totalLatency + "\n";
		return buffer;
	}

	public float getLatency() {
		return this.totalLatency;
	}

	public DICOM getImage() {
		return this.source;
	}

	public KeyStructureWritable getKeyStructure() {
		return this.keyStructure;
	}

	public ImageStructure getImageStructure() {
		return this.imageStructure;
	}
}
