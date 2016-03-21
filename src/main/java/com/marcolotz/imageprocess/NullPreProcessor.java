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

import ij.ImagePlus;

/**
 * This class basically return its input value. It is used as a wildcard in
 * order to keep the modularity of the code.
 * 
 * If an user in the future desires to produce a preprocess module, it can be
 * easily applied as a plug-in.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class NullPreProcessor extends ImageProcessingClass {

	@Override
	protected ImagePlus process(ImagePlus inputImage) {
		// Makes no process on the original image.
		return this.getInput();
	}
}
