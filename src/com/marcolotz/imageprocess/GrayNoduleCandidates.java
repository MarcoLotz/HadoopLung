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

package com.marcolotz.imageprocess;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Iterator;

import ij.ImagePlus;
import ij.process.ByteProcessor;

/**
 * Node Candidates Detector based in Gray level plugin. Once the Lung is
 * extracted from the original image, it uses gray scale analysis to find
 * possible components that are Node candidates. Since the input image has the
 * same 16-bits depth of the original DICOM, the grey level convertion is done
 * using a rule of three.
 * 
 * The candidates will only be taken as a nodule in a post-processing phase,
 * using the criteria defined in the 2003 paper.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class GrayNoduleCandidates extends ImageProcessingClass {

	private BufferedImage binaryMask;
	private BufferedImage eightBitDICOM;
	private ImagePlus output;

	private ArrayList<Blob> nodeCandidatesList;

	/***
	 * The minimum and maximum gray value of regions of interest, according to
	 * the 8-bits depth images suggested on the article
	 */
	int bottomLimit;
	int upperLimit;

	/***
	 * The maximum range is using empiric values of maximum pixel values in a
	 * DICOM image. One can update this value by using a maximum method that
	 * looks for the highest value in all the image pixels
	 */
	static int maximumValueRange = 2235;
	// End of scale value for an 8 bit image.
	static final int maximumValueEightBits = 255;

	/***
	 * Converts from grey level to DICOM using rule of three.
	 */
	int dicomBottomLimit;
	int dicomUpperLimit;

	/***
	 * The minimum size (in pixel) of the components that will be filtered.
	 */
	static final int minimumWidth = 5;
	static final int minimumHeight = 5;

	/***
	 * Mask definitions
	 */
	static final int ROIColour = 255;
	static final int backGroundColour = 0;

	//ImageViewer imgDebug = new ImageViewer();

	@Override
	protected ImagePlus process(ImagePlus inputImage) {
		/* Updates the parameters to match the configuration file*/
		configure();
		
		/* Generates an 8 bits version of the input Image */
		eightBitDICOM = inputImage.getBufferedImage();

		// Allocates a mask. The 8 bits version is used as an input because it
		// is the required depth by the blob detector. The color attributes
		// themselves are never analysed here.
		binaryMask = cloneBufferedImage(eightBitDICOM);

		// Selects only regions that are between the thresholds.
		thresholdSelector(dicomBottomLimit, dicomUpperLimit, inputImage);

		// Selects only the components with dimensions larger than a value
		minimumSizeMask(minimumWidth, minimumHeight);

		//imgDebug.setImage(binaryMask);
		generateOutput();

		return this.output;
	}

	/***
	 * Configures the gray levels used as a threshold 
	 */
	private void configure() {
		bottomLimit = conf.getInt("com.marcolotz.grayNoduleCandidates.bottomThreshold", 110);
		upperLimit = conf.getInt("com.marcolotz.grayNoduleCandidates.topThreshold", 120);
		
		dicomBottomLimit = bottomLimit * maximumValueRange
					/ maximumValueEightBits;
		
		dicomUpperLimit = upperLimit * maximumValueRange
				/ maximumValueEightBits;
	}

	/***
	 * Selects Regions of Interest based on the gray level of the region
	 * 
	 * @param bottomLimit
	 * @param upperLimit
	 */
	private void thresholdSelector(int bottomLimit, int upperLimit,
			ImagePlus inputImage) {
		int xcoord = 0;
		int ycoord = 0;

		int[] maskPixelValue = new int[1];
		int inputPixelValue = 0;

		WritableRaster maskRaster = binaryMask.getRaster();
		ij.process.ImageProcessor inputProcessor = inputImage.getProcessor();

		for (ycoord = 0; ycoord < binaryMask.getHeight(); ycoord++) {
			for (xcoord = 0; xcoord < binaryMask.getWidth(); xcoord++) {

				inputPixelValue = inputProcessor.getPixel(xcoord, ycoord);

				// Since the mask has an 8 bits depth:
				if ((inputPixelValue >= bottomLimit)
						&& (inputPixelValue <= upperLimit)) {
					maskPixelValue[0] = ROIColour;

				} else {
					maskPixelValue[0] = backGroundColour;
				}
				maskRaster.setPixel(xcoord, ycoord, maskPixelValue);
			}
		}
	}

	/***
	 * Verifies connected components (blobs) in the objects mask. Then filters
	 * the blobs based on their minimum height and width.
	 * 
	 * @param minimumWidth
	 * @param minimumHeight
	 */
	private void minimumSizeMask(int minimumWidth, int minimumHeight) {
		// Show blobs in white when using their draw method
		Blob.setDefaultColor(Color.WHITE);

		ImagePlus ip = new ImagePlus("Threshold Candidates", this.binaryMask);
		ManyBlobs mb = new ManyBlobs(ip);

		// Detect the black areas as background.
		mb.setBackground(0);

		mb.findConnectedComponents();

		System.out.println("Found " + mb.size() + " Regions on interest.");

		// Filters for nodes that match the designated size.
		System.out.println("Filtering by size:" + minimumWidth + "x"
				+ minimumHeight);

		Iterator<Blob> blobIter = mb.iterator();

		nodeCandidatesList = new ArrayList<Blob>();

		while (blobIter.hasNext()) {
			Blob analysedBlob = blobIter.next();
			Polygon pol = analysedBlob.getOuterContour();

			double width = pol.getBounds().getWidth();
			double height = pol.getBounds().getHeight();

			// Increment one, since 1 pixel size is considered 0 width by
			// the polygon class.
			width++;
			height++;

			// If it is larger than the desired size, add to the list
			if ((width >= minimumWidth) && (height >= minimumHeight)) {
				System.out.println("Node candidate found!");
				System.out.println("Dimensions: " + width + "x" + height);
				nodeCandidatesList.add(analysedBlob);
			}
		}

		// Generates a new binary mask with only candidates
		ByteProcessor newProcessor = new ByteProcessor(binaryMask.getWidth(),
				binaryMask.getHeight());

		Iterator<Blob> printIter = nodeCandidatesList.iterator();
		while (printIter.hasNext()) {
			printIter.next().draw(newProcessor);
		}

		binaryMask = newProcessor.getBufferedImage();
	}

	/***
	 * Generates a ImagePlus file from the final binary mask
	 */
	public void generateOutput() {
		this.output = new ImagePlus("Candidates Nodes", this.binaryMask);
	}

	/***
	 * Gets the Nodule Candidates List
	 * 
	 * @return The nodules Candidates List
	 */
	public ArrayList<Blob> getCandidatesList() {
		return this.nodeCandidatesList;
	}
}
