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
