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
package com.marcolotz.lung.mapper;

import ij.plugin.DICOM;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.marcolotz.MRComponents.KeyStructureWritable;
import com.marcolotz.MapperComponents.ImageMetadata;
import com.marcolotz.MapperComponents.ImageStructure;
import com.marcolotz.imageprocess.GrayNoduleCandidates;
import com.marcolotz.imageprocess.ImageProcessor;
import com.marcolotz.imageprocess.NullPreProcessor;
import com.marcolotz.imageprocess.TresholdLung;
import com.marcolotz.lung.io.inputFormat.DistributedDicomReader;

/**
 * Mapper used in the HadoopLung application. All the images are processed in
 * the Map phase.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class LungMapper
		extends
		Mapper<NullWritable, BytesWritable, KeyStructureWritable, ImageMetadata> {

	// private static final Log LOG = LogFactory.getLog(LungMapper.class);

	/***
	 * The key generated by the record reader is a NullWritable with no content.
	 * The value is a byte array that represents a DICOM image.
	 * 
	 * @param nothing
	 * @param dicomImage
	 * @param context
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
	protected void map(NullWritable nothing, BytesWritable dicomImage,
			Context context) throws IOException, InterruptedException {

		/***
		 * Local Attributes declarations
		 */
		DistributedDicomReader reader;
		ImageProcessor imageProcessor;

		KeyStructureWritable keyStructure;
		ImageStructure imageStructure;
		ImageMetadata imageMetadata;

		// Generates a DICOM image from the BytesWritable input.
		DICOM image = convertToDICOM(dicomImage);

		// Process image meta information
		// generating a key-value pair for the image
		reader = new DistributedDicomReader(image);

		// Gets the key from the reader.
		keyStructure = reader.getKeyStructure();

		// Gets the image meta data from the reader
		imageStructure = reader.getImageStructure();

		/***
		 * Process the DICOM image: inside the image processor one can use
		 * plugins.
		 */
		imageProcessor = new ImageProcessor(NullPreProcessor.class,
				TresholdLung.class, GrayNoduleCandidates.class,
				context.getConfiguration());
		imageProcessor.setInput(image);
		imageProcessor.run();

		/***
		 * Updates the imageStructure with the nodes detected by the @imageProcessor
		 */
		imageStructure
				.setNodeCandidatesList(imageProcessor.getNodeCandidates());

		/***
		 * Generates the emitted value. The value has Metadata from the image
		 * Structure, including image information and blob data.
		 */
		imageMetadata = new ImageMetadata(imageStructure);

		// Emits the key-value pair
		context.write(keyStructure, imageMetadata);
	}

	/**
	 * Converts from bytesWritable to DICOM image.
	 * 
	 * @param dicomImage
	 * @return DICOM image
	 */
	private DICOM convertToDICOM(BytesWritable dicomImage) {
		byte[] inputContent = dicomImage.getBytes();

		InputStream inputStream = new ByteArrayInputStream(inputContent);

		DICOM convertedImage = new DICOM(inputStream);

		// Gives a generic name to the image file
		convertedImage.run("Dicom Image");

		return convertedImage;
	}

}
