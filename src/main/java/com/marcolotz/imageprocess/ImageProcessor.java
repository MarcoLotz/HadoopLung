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

import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;

import ij.ImagePlus;
import ij.blob.Blob;

/**
 * Manages all the image processing. The modules that are going to be used in
 * the processing parts should be defined on the constructor.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class ImageProcessor {

	Configuration conf;
	// Define any parameter.
	private float startTime;
	private float totalLatency;

	/*
	 * ImageProcessor class is actually an interface, allowing user defined
	 * classes to be used.
	 */

	// Pre-Processing Class.
	private ImageProcessingClass preProcessor;

	// Lung Extractor Class
	private ImageProcessingClass lungExtractor;

	// Processing Class.
	private ImageProcessingClass nodeCandidatesDetector;

	// sequence:
	// pre-processing -> lung extraction -> node candidates detector

	// Image info
	private String imageName;

	/* Images: */

	/* The input of the pre processor */
	ImagePlus inputImage;

	/* output of the pre processor */
	ImagePlus preProcessedImage;

	/* the lung segmented image */
	ImagePlus lungExtractedImage;

	/* Image with the detected nodules */
	ImagePlus nodeCandidatesImage;

/**
 * Image processor default constructor
 * @param pre
 * @param lung
 * @param node
 * @param configuration
 */
	public ImageProcessor(Class<? extends ImageProcessingClass> pre,
			Class<? extends ImageProcessingClass> lung,
			Class<? extends ImageProcessingClass> node,
			Configuration configuration) {
		startTime = 0;
		totalLatency = 0;

		imageName = "no Image";

		this.conf = configuration;

		setPreProcessorClass(pre);
		setLungExtractorClass(lung);
		setNodeCandidatesDetectorClass(node);

		configureProcessors(this.conf);

		// Make any possible definition that one may want.
	}

	private void configureProcessors(Configuration conf) {
		preProcessor.setConfiguration(conf);
		lungExtractor.setConfiguration(conf);
		nodeCandidatesDetector.setConfiguration(conf);
	}

	/***
	 * Main method. Defines the behavior of the ImageProcessor.
	 */

	public void run() {
		startTimer();

		// Put user define process sequence here.

		preProcessedImage = preProcess(inputImage);
		try {
			lungExtractedImage = LungExtraction(preProcessedImage);
			nodeCandidatesImage = DetectNodesCandidates(lungExtractedImage);
		} catch (Exception e) {
			System.out.println(e.toString());
		}

		// Stop user defined process sequence here.
		stopTimer();
	}

	private void stopTimer() {
		this.totalLatency = System.currentTimeMillis() - this.startTime;
	}

	private void startTimer() {
		this.startTime = System.currentTimeMillis();
	}

	public float getLatency() {
		return this.totalLatency;
	}

	@Override
	public String toString() {
		String buffer = new String();
		buffer = buffer + "Image Processor Info: \n";
		buffer = buffer + "Image processed: " + this.imageName + "\n";
		buffer = buffer + "Total Latency: " + getLatency() + "\n";

		return buffer;
	}

	public void setPreProcessorClass(Class<? extends ImageProcessingClass> pre) {
		try {
			preProcessor = pre.newInstance();
		} catch (Exception e) {
			System.out.println("Error in PreProcessor instantiation");
			e.printStackTrace();
		}
	}

	public void setNodeCandidatesDetectorClass(
			Class<? extends ImageProcessingClass> nod) {
		try {
			nodeCandidatesDetector = nod.newInstance();
		} catch (Exception e) {
			System.out.println("Error in NodeCandidates instantiation");
			e.printStackTrace();
		}
	}

	public void setLungExtractorClass(Class<? extends ImageProcessingClass> lung) {
		try {
			lungExtractor = lung.newInstance();
		} catch (Exception e) {
			System.out.println("Error in Lung Extractor instantiation");
			e.printStackTrace();
		}
	}

	public void setInput(ImagePlus image) {
		this.inputImage = image;
	}

	public ImagePlus getInputImage() {
		return this.inputImage;
	}

	public ImagePlus getPreProcessedImage() {
		return this.preProcessedImage;
	}

	public ImagePlus getLungExtractedImage() {
		return this.lungExtractedImage;
	}

	public ImagePlus preProcess(ImagePlus input) {
		// There must be a lung extractor
		if (preProcessor == null) {
			throw new UnsupportedOperationException("No preprocessor");
		} else {
			preProcessor.run(inputImage);
			return preProcessor.getOutput();
		}
	}

	public ImagePlus LungExtraction(ImagePlus preProcessedImage)
			throws UnsupportedOperationException {
		// There must be a lung extractor

		if (this.lungExtractor == null) {
			throw new UnsupportedOperationException("No lung Extractor");
		} else {
			lungExtractor.run(preProcessedImage);
			return lungExtractor.getOutput();
		}
	}

	public ImagePlus DetectNodesCandidates(ImagePlus lungExtractedImage)
			throws UnsupportedOperationException {
		// There must be a Node Detector class
		if (this.nodeCandidatesDetector == null) {
			throw new UnsupportedOperationException("No node detection");
		} else {
			nodeCandidatesDetector.run(lungExtractedImage);
			return nodeCandidatesDetector.getOutput();
		}
	}

	/***
	 * Returns an Array list with all the blobs that can be nodules TODO: Find a
	 * way to do not force the CandidatesDetector to be of type
	 * GrayNodeCandidates.
	 * 
	 * @return an array of blobs
	 */
	public ArrayList<Blob> getNodeCandidates() {
		GrayNoduleCandidates processor = (GrayNoduleCandidates) nodeCandidatesDetector;
		return processor.getCandidatesList();
	}
}
