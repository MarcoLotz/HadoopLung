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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.apache.hadoop.conf.Configuration;

import ij.ImagePlus;
import ij.plugin.Duplicator;

/***
 * An abstract class that should be extended by class that is going to process
 * images. The {@link ImageProcessor} uses it when calling the processing
 * classes.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class ImageProcessingClass {

	Configuration conf;

	ImagePlus input;
	ImagePlus output;

	/* For time benchmarks */
	float startTime;
	float totalLatency;

	public void setInput(ImagePlus input) {
		this.input = input;
	}

	public ImagePlus getInput() {
		return this.input;
	}

	public void setOutput(ImagePlus output) {
		this.output = output;
	}

	public ImagePlus getOutput() {
		return this.output;
	}

	private void startTimer() {
		this.startTime = System.currentTimeMillis();
	}

	private void stopTimer() {
		this.totalLatency = System.currentTimeMillis() - this.startTime;
	}

	public float getLatency() {
		return this.totalLatency;
	}

	public void run(ImagePlus input) {
		setInput(input);

		startTimer();

		// Runs the class defined process instruction
		System.out.println("\n Starting " + this.getClass().getName());
		setOutput(process(input));

		stopTimer();
		System.out.println(this.getClass().getName() + " finished");
		System.out.println("Total processing time (ms): " + this.getLatency());
	}

	/***
	 * User Defined processing method. This should be implemented in a
	 * user-defined way in order to keep the modularity of the code.
	 * 
	 * @param inputImage
	 * @return Processed image from that module.
	 */
	protected ImagePlus process(ImagePlus inputImage) {
		// * Do nothing */
		return getInput();
	}

	/***
	 * Generates a binary mask the originally is a clone of the input image.
	 * 
	 * @param input
	 * @return the new allocated Mask
	 */
	protected BufferedImage cloneBufferedImage(BufferedImage input) {
		ColorModel cModel = input.getColorModel();
		boolean isAlphaPremultiplied = input.isAlphaPremultiplied();
		WritableRaster maskRaster = input.copyData(null);

		BufferedImage Mask = new BufferedImage(cModel, maskRaster,
				isAlphaPremultiplied, null);

		return Mask;
	}

	/***
	 * Since the object.clone for ImageJ only copies bare data (doesnt copy the
	 * image properties as calibration, info, etc), one needs to use the
	 * Duplicator method for this.
	 * 
	 * Reference:http://imagej.1557.x6.nabble.com/Copying-an-8-bit-image-
	 * td3686474.html
	 * 
	 * @param sourceImage
	 * @return ImagePlus a copy of the image
	 */
	protected ImagePlus duplicate(ImagePlus sourceImage) {
		return new Duplicator().run(sourceImage);
	}

	public void setConfiguration(Configuration conf) {
		this.conf = conf;
	}
}
