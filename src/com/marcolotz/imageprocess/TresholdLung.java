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
package com.marcolotz.imageprocess;

import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.process.ByteProcessor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Iterator;

import mmorpho.Constants;
import mmorpho.MorphoProcessor;
import mmorpho.StructureElement;

/**
 * Threshold processing module. Extracts the lungs first using a mask based in
 * the gray value (HU scale) of the pixels. Later performs an opening
 * (morphological operator) and then selects the desired connected components.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class TresholdLung extends ImageProcessingClass {

	private BufferedImage binaryMask;

	// A 8 bit depth version of the DICOM file
	private BufferedImage eightBitDICOM;

	// For debug purposes:
	//private ImageViewer imgDebug = new ImageViewer();

	/**
	 * Gray Threshold defined in the reference papers in order to extract lung
	 * parenchyma and background. It's based in the Hounsfield scale.
	 */
	private static final int GrayThreshold = -375;

	/***qq
	 * Offset of the calibration device
	 */
	private static final int CalibrationOffset = 1000;

	/***
	 * Selects the colour of the background on the output image
	 */
	private static final int outputBackGroundColour = 2000;

	// Conversion referenced at:
	// http://imagej.1557.x6.nabble.com/Hounsfield-Unit-conversion-td3686965.html

	private static final int GrayThresholdNormalized = GrayThreshold
			+ CalibrationOffset;

	private static final int circleRadius = 4;

	@Override
	protected ImagePlus process(ImagePlus inputImage) {
		
		/* Generates an 8 bits version of the input image */
		eightBitDICOM = inputImage.getBufferedImage();

		/*
		 * Generates a mask from the eightBitDicom .The 8 bits depth is used for
		 * the mask since the morphological operator and the blob processor
		 * require this exact bit depth. The content of the mask itself is not
		 * used for the output image. Thus, the output image still has 16 bits
		 * depth.
		 */
		this.binaryMask = cloneBufferedImage(eightBitDICOM);

		// Generates the gray threshold mask
		GrayThresholdSegmentate(inputImage);

		// Morphological closing on the Mask
		closeMask(circleRadius);

		// Select the connected components that don't touch
		// the margin from the mask and removes holes.
		removeBackground();

		// Extracts the Lungs using the mask
		this.output = extractLungs16Bits(inputImage);

		return this.output;
	}

	/***
	 * updates the value of the binaryMask (8 bits binary mask) attribute based
	 * on the pixel intensity and the threshold used on the original image.
	 * 
	 * @param input
	 */
	private void GrayThresholdSegmentate(ImagePlus input) {
		WritableRaster maskRaster = binaryMask.getRaster();

		int[] mArray = new int[1];
		int[] pixelValue = new int[1];

		for (int ycoord = 0; ycoord < input.getHeight(); ycoord++) {
			for (int xcoord = 0; xcoord < input.getWidth(); xcoord++) {

				// Gets pixel value
				pixelValue = input.getPixel(xcoord, ycoord);

				// If higher than the threshold, set as black:
				if (pixelValue[0] > GrayThresholdNormalized) {
					mArray[0] = 0;
					maskRaster.setPixel(xcoord, ycoord, mArray);
				} else {
					// Otherwise sets as white:
					mArray[0] = 255;
					maskRaster.setPixel(xcoord, ycoord, mArray);
				}
			}
		}
	}

	/***
	 * Performs a morphological closing on the binaryMask.
	 * 
	 * @param circleRadius
	 *            : Circle Radius in pixels.
	 */
	private void closeMask(int circleRadius) {
		/* A closing is a dilation operation followed by an erosion */

		ByteProcessor bp = new ByteProcessor(this.binaryMask);

		StructureElement se = new StructureElement(Constants.CIRCLE, 0,
				circleRadius, Constants.OFFSET0);
		MorphoProcessor mp = new MorphoProcessor(se);

		mp.close(bp);

		binaryMask = bp.getBufferedImage();
	}

	/***
	 * Separates lungs from background using a connected components method. It
	 * assumes that the components that are background touch the margins. Also,
	 * it removes any holes that may be inside the components that do not touch
	 * the margins. Updates the Object Binary Mask with only the lung values
	 * (only components that do not touch the margin).
	 */

	private void removeBackground() {

		Blob blob;

		/*
		 * Defines the colour that the blob will be displayed when using the
		 * draw method
		 */

		Blob.setDefaultColor(Color.WHITE);

		// Generates a new processor with the dimensions of the binary mask??
		ByteProcessor NewProcessor = new ByteProcessor(binaryMask.getWidth(),
				binaryMask.getHeight());

		ImagePlus ip = new ImagePlus("Closed Mask", this.binaryMask);
		ManyBlobs mb = new ManyBlobs(ip);

		// Define as background (not an object) the black areas (0);
		mb.setBackground(0);
		mb.findConnectedComponents();

		System.out.println("Found " + mb.size() + " components!");

		// Removes the blobs that touch the margin

		int xcoord = 0;
		int ycoord = 0;

		// Looks for blobs that contain pixels in the margins:
		for (xcoord = 0; xcoord < this.binaryMask.getWidth(); xcoord++) {

			for (ycoord = 0; ycoord < this.binaryMask.getHeight(); ycoord++) {

				// If the coordinate is a margin value:
				if ((xcoord == 0) || (xcoord == this.binaryMask.getWidth())
						|| (ycoord == 0)
						|| (ycoord == this.binaryMask.getHeight())) {
					blob = mb.getSpecificBlob(xcoord, ycoord);
					// If there is actually a blob with that pixel, remove from
					// the list.
					if (blob != null) {
						mb.remove(blob);
					}
				}
			}
		}

		/*
		 * Print only those blobs that don't touch the margin on a new mask.
		 * Draw those blobs without holes.
		 */

		Iterator<Blob> blobIter = mb.listIterator(0);
		while (blobIter.hasNext()) {
			blobIter.next().draw(NewProcessor, 0);
		}

		// Updates the object binaryMask attribute.
		binaryMask = NewProcessor.getBufferedImage();
	}

	/***
	 * Uses the current mask to extract only the lungs. The returned image has 8
	 * bits depth.
	 */
	@SuppressWarnings("unused")
	private ImagePlus extractLungs8Bits() {
		int xcoord = 0;
		int ycoord = 0;

		int[] maskPixelValue = new int[1];
		int[] InputPixelValue = new int[1];

		int[] tmpMask = new int[1];
		int[] tmpInput = new int[1];

		BufferedImage bufferedOutput = cloneBufferedImage(eightBitDICOM);

		WritableRaster maskRaster = binaryMask.getRaster();
		WritableRaster inputRaster = eightBitDICOM.getRaster();
		WritableRaster outputRaster = bufferedOutput.getRaster();

		for (ycoord = 0; ycoord < binaryMask.getHeight(); ycoord++) {
			for (xcoord = 0; xcoord < binaryMask.getWidth(); xcoord++) {
				tmpMask[0] = 0;
				tmpInput[0] = 0;

				maskPixelValue = maskRaster.getPixel(xcoord, ycoord, tmpMask);

				// It is in 8 bits depth
				if (maskPixelValue[0] == 255) {
					InputPixelValue = inputRaster.getPixel(xcoord, ycoord,
							tmpInput);
				} else {
					// Originally was 0, but 255 makes the visualization easier.
					InputPixelValue[0] = outputBackGroundColour;
				}

				outputRaster.setPixel(xcoord, ycoord, InputPixelValue);
			}
		}

		ImagePlus output = new ImagePlus("Extracted Lung", bufferedOutput);

		return output;
	}

	/***
	 * Uses the current mask to extract only the lungs from the original image.
	 * The returned image has 16 bits depth.
	 */

	private ImagePlus extractLungs16Bits(ImagePlus inputImage) {

		// Generates a new image with all the original image metadata.
		ImagePlus BufferedOutput = duplicate(inputImage);

		ij.process.ImageProcessor imgProcessor = BufferedOutput.getProcessor();

		int xcoord = 0;
		int ycoord = 0;

		int[] maskPixelValue = new int[1];
		int[] inputPixelValue = new int[1];

		int[] tmpMask = new int[1];
		int[] tmpInput = new int[1];

		WritableRaster maskRaster = binaryMask.getRaster();

		for (ycoord = 0; ycoord < binaryMask.getHeight(); ycoord++) {
			for (xcoord = 0; xcoord < binaryMask.getWidth(); xcoord++) {
				tmpMask[0] = 0;
				tmpInput[0] = 0;

				maskPixelValue = maskRaster.getPixel(xcoord, ycoord, tmpMask);

				// Assuming the the bitDepth of the mask is 8.
				if (maskPixelValue[0] == 255) {
					inputPixelValue = inputImage.getPixel(xcoord, ycoord);
				} else {
					// Originally was 0, but 255 makes the visualization easier.
					inputPixelValue[0] = outputBackGroundColour;
				}
				imgProcessor.set(xcoord, ycoord, inputPixelValue[0]);
			}
		}

		return BufferedOutput;
	}
}
