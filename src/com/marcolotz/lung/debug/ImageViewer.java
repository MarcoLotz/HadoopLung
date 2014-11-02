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
package com.marcolotz.lung.debug;

import ij.ImagePlus;
import ij.gui.ImageCanvas;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * It's a simple interface to a JFrame object. Done for modularity purposes.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class ImageViewer {

	private JFrame imageFrame;

	public ImageViewer() {
		imageFrame = new JFrame();

	}
	
	public void setImage(BufferedImage srImage) {
		System.out.print("Adding image to the Jframe...");
		cleanFrame();
		JLabel picLabel = new JLabel(new ImageIcon(srImage));
		imageFrame.add(picLabel);
		configure(srImage.getWidth(), srImage.getHeight());
		System.out.println("[OK]");
	}

	public void setImage(ImagePlus srImage) {
		System.out.print("Adding image to the Jframe...");
		cleanFrame();
		ImageCanvas ic = new ImageCanvas(srImage);
		imageFrame.add(ic);
		configure(srImage.getWidth(), srImage.getHeight());
		System.out.println("[OK]");
	}

	private void configure(int width, int height) {
		imageFrame.setBackground(Color.BLACK);
		imageFrame.setSize(512, 512);
		imageFrame.setVisible(true);
	}

	private void cleanFrame() {
		imageFrame.getContentPane().removeAll();
	}
}
