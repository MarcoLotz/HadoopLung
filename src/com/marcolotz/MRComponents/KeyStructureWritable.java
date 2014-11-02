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
package com.marcolotz.MRComponents;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import ij.plugin.DICOM;
import ij.util.DicomTools;

/**
 * The key structure used by mapper and reducers. It is of paramount importance
 * that the Key element do not know an image, since it is desirable for it to be
 * as small as possible.
 * 
 * @author Marco Aurelio Lotz
 * 
 */
public class KeyStructureWritable implements
		WritableComparable<KeyStructureWritable> {

	/* ======================== DICOM info ======================= */

	// Unique identifier for the series that the image belongs to.
	private String seriesInstanceUID;

	private String studyDate;
	private String seriesDate;

	private String studyTime;
	private String seriesTime;

	private String modality;

	private String manufacturer;
	private String institutionName;
	private String institutionAddress;

	private String stationName;
	private String studyDescription;

	private String patientsName;
	private String patientsID;

	private String bodyPartExamined;
	private String sliceThickness;
	private String kVP;

	/***
	 * gives the distance between two adjacent slices (perpendicular to the
	 * image plane). More detailed info at:
	 * http://stackoverflow.com/questions/14930222
	 * /how-to-calculate-space-between-dicom-slices-for-mpr
	 */
	private String spaceBetweenSlices;

	// The size of a pixel (in mm).
	private String pixelSpacing;

	/* =========================================================== */

	public KeyStructureWritable() {
	};

	public KeyStructureWritable(DICOM image) {
		generateMetadata(image);
	}

	private void generateMetadata(DICOM image) {
		seriesInstanceUID = DicomTools.getTag(image,
				DICOMTags.SeriesInstanceUID);

		studyDate = DicomTools.getTag(image, DICOMTags.StudyDate);
		seriesDate = DicomTools.getTag(image, DICOMTags.SeriesDate);

		studyTime = DicomTools.getTag(image, DICOMTags.StudyTime);
		seriesTime = DicomTools.getTag(image, DICOMTags.SeriesTime);

		modality = DicomTools.getTag(image, DICOMTags.Modality);

		manufacturer = DicomTools.getTag(image, DICOMTags.Manufacturer);

		institutionName = DicomTools.getTag(image, DICOMTags.InstitutionName);
		institutionAddress = DicomTools.getTag(image,
				DICOMTags.InstitutionAddress);

		stationName = DicomTools.getTag(image, DICOMTags.StationName);
		studyDescription = DicomTools.getTag(image, DICOMTags.StudyDescription);

		patientsName = DicomTools.getTag(image, DICOMTags.PatientsName);
		patientsID = DicomTools.getTag(image, DICOMTags.PatientsID);

		bodyPartExamined = DicomTools.getTag(image, DICOMTags.BodyPartExamined);
		sliceThickness = DicomTools.getTag(image, DICOMTags.SliceThickness);
		kVP = DicomTools.getTag(image, DICOMTags.KVP);
		spaceBetweenSlices = DicomTools.getTag(image,
				DICOMTags.SpaceBetweenSlices);

		pixelSpacing = DicomTools.getTag(image, DICOMTags.PixelSpacing);
	}

	public String getSeriesInstanceUID() {
		return seriesInstanceUID;
	}

	public String getStudyDate() {
		return studyDate;
	}

	public String getSeriesDate() {
		return seriesDate;
	}

	public String getStudyTime() {
		return studyTime;
	}

	public String getSeriesTime() {
		return seriesTime;
	}

	public String getModality() {
		return modality;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getInstitutionName() {
		return institutionName;
	}

	public String getInstitutionAddress() {
		return institutionAddress;
	}

	public String getStationName() {
		return stationName;
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public String getPatientsName() {
		return patientsName;
	}

	public String getPatientsID() {
		return patientsID;
	}

	public String getBodyPartExamined() {
		return bodyPartExamined;
	}

	public String getSliceThickness() {
		return sliceThickness;
	}

	public String getkVP() {
		return kVP;
	}

	public String getPixelSpacing() {
		return pixelSpacing;
	}

	/**
	 * @return the spaceBetweenSlices
	 */
	public String getSpaceBetweenSlices() {
		return spaceBetweenSlices;
	}

	@Override
	public void write(DataOutput out) throws IOException {

		SerializerConverter.writeString(seriesInstanceUID, out);

		SerializerConverter.writeString(studyDate, out);
		SerializerConverter.writeString(seriesDate, out);

		SerializerConverter.writeString(studyTime, out);
		SerializerConverter.writeString(seriesTime, out);

		SerializerConverter.writeString(modality, out);

		SerializerConverter.writeString(manufacturer, out);

		SerializerConverter.writeString(institutionName, out);
		SerializerConverter.writeString(institutionAddress, out);

		SerializerConverter.writeString(stationName, out);
		SerializerConverter.writeString(studyDescription, out);

		SerializerConverter.writeString(patientsName, out);
		SerializerConverter.writeString(patientsID, out);

		SerializerConverter.writeString(bodyPartExamined, out);
		SerializerConverter.writeString(sliceThickness, out);
		SerializerConverter.writeString(kVP, out);
		SerializerConverter.writeString(spaceBetweenSlices, out);

		SerializerConverter.writeString(pixelSpacing, out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {

		seriesInstanceUID = SerializerConverter.readString(in);

		studyDate = SerializerConverter.readString(in);
		seriesDate = SerializerConverter.readString(in);

		studyTime = SerializerConverter.readString(in);
		seriesTime = SerializerConverter.readString(in);

		modality = SerializerConverter.readString(in);

		manufacturer = SerializerConverter.readString(in);

		institutionName = SerializerConverter.readString(in);
		institutionAddress = SerializerConverter.readString(in);

		stationName = SerializerConverter.readString(in);
		studyDescription = SerializerConverter.readString(in);

		patientsName = SerializerConverter.readString(in);
		patientsID = SerializerConverter.readString(in);

		bodyPartExamined = SerializerConverter.readString(in);
		sliceThickness = SerializerConverter.readString(in);
		kVP = SerializerConverter.readString(in);
		spaceBetweenSlices = SerializerConverter.readString(in);

		pixelSpacing = SerializerConverter.readString(in);
	}
	
	@Override
	public int compareTo(KeyStructureWritable comparedKeyStruct) {

		Text seriesInstanceHere = new Text(this.getSeriesInstanceUID());
		Text seriesInstanceCompared = new Text(
				comparedKeyStruct.getSeriesInstanceUID());

		return seriesInstanceHere.compareTo(seriesInstanceCompared);
	}

	/***
	 * The default partitioner is the HashPartitioner, which uses the hashCode
	 * method to determine which reducer to send the K,V pair to. For this
	 * reason, objects with the same key should give the same hash value.
	 * 
	 * In this implementation the hashCode is given by the SeriesInstanceUID
	 * Hash.
	 */
	@Override
	public int hashCode() {
		Text hashText = new Text(getSeriesInstanceUID());
		return hashText.hashCode();
	}
}
