package edu.construct3d.parser;

import java.io.BufferedReader;
import java.io.IOException;

import edu.construct3d.models.Material;
import edu.construct3d.models.Model;
import edu.construct3d.util.BaseFileUtil;





public class MtlParser { 

	private BaseFileUtil fileUtil;
	
	public MtlParser(Model model, BaseFileUtil fileUtil) {
		this.fileUtil = fileUtil;
	}

	/**
	 * parses the given material definition
	 * @param line
	 */
	public void parse(Model model, BufferedReader is) {
		Material curMat = null;
		String line;
		try {
			for (line = is.readLine();
			line != null; 
			line = is.readLine())
			{
				line = Util.getCanonicalLine(line).trim();
				if (line.length() > 0) {
					if (line.startsWith("newmtl ")) {
						// specular color
						String mtlName = line.substring(7);
						curMat = new Material(mtlName);
						model.addMaterial(curMat);
					} else if(curMat == null) {
						//if the current material is not set, there is no need to parse anything
					} else if (line.startsWith("# ")) {
						//ignore comments
					} else if (line.startsWith("Ka ")) {
						//ambient color
						String endOfLine = line.substring(3);
						curMat.setAmbient(parseTriple(endOfLine));
					} else if (line.startsWith("Kd ")) {
						// diffuse color
						String endOfLine = line.substring(3);
						curMat.setDiffuse(parseTriple(endOfLine));
					} else if (line.startsWith("Ks ")) {
						// specular color
						String endOfLine = line.substring(3);
						curMat.setSpecular(parseTriple(endOfLine));
					} else if (line.startsWith("Ns ")) {
						// specular color range
						String endOfLine = line.substring(3);
						curMat.setShininess(Float.parseFloat(endOfLine));
					} else if (line.startsWith("Tr ")) {
						// Alpha
						String endOfLine = line.substring(3);
						curMat.setAlpha(Float.parseFloat(endOfLine));
					} else if (line.startsWith("d ")) {
						// Alpha
						String endOfLine = line.substring(2);
						curMat.setAlpha(Float.parseFloat(endOfLine));
					} else if(line.startsWith("map_Kd ")) {
						//limited texture support
						String imageFileName = line.substring(7);
						curMat.setFileUtil(fileUtil);
						curMat.setBitmapFileName(imageFileName);
					} else if(line.startsWith("mapKd ")) {
						//limited texture support
						String imageFileName = line.substring(6);
						curMat.setFileUtil(fileUtil);
						curMat.setBitmapFileName(imageFileName);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static float[] parseTriple(String str) {
		String[] colorVals = str.split(" ");
		float[] colorArr = new float[]{
				Float.parseFloat(colorVals[0]),
				Float.parseFloat(colorVals[1]),
				Float.parseFloat(colorVals[2])};
		return colorArr;
		
	}
	
}
