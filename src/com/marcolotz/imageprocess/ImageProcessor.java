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
