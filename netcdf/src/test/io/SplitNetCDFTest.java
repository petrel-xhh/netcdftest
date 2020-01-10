package test.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class SplitNetCDFTest {

	private static int blockLength = 1000;
	public static void main(String[] args)
	{
		test2();
	}
	public static void test2()
	{
		String inputPath = "/home/petrel/Documents/random4.nc4";
		String outputDir = "/home/petrel/Documents/random4";
		int[] dimSplitNum = {20, 20};
		long time0 = System.currentTimeMillis();
		splitData(inputPath, dimSplitNum, outputDir, NetcdfFileWriter.Version.netcdf3);
		long time1 = System.currentTimeMillis();
		System.out.println("time used:" + (time1 - time0));
		
	}
	public static void test1()
	{
		getSectionSpecs(new int[] {320, 640}, new int[] {2, 3}, null).forEach(System.out::println);
		String sectionSpec = "160:319,639:639";
		String[] dimArray = sectionSpec.split(",");
		getDimSectionSpecs(dimArray, null).forEach(subSectionSpec->
		{
			System.out.println(subSectionSpec+"\t"+Arrays.toString(getOrigin(sectionSpec, subSectionSpec)));
		});
	}
	public static void splitData(String inputPath, int[] dimSplitNum, String outputDir, NetcdfFileWriter.Version version)
	{
		String inputFileName = new java.io.File(inputPath).getName().split("\\.")[0];
		File outputDirFile = new File(outputDir);
		if(!outputDirFile.exists())
		{
			outputDirFile.mkdir();
		}
		try(NetcdfFileWriter writer = NetcdfFileWriter.openExisting(inputPath))
		{
			List<Variable> varList = writer.getNetcdfFile().getVariables();
			
			for(Variable var : varList)
			{
				String varName = var.getFullName();
				int[] varShape = var.getShape();
				if(varShape.length == 1)
				{
					continue;
				}
				if(varShape.length > dimSplitNum.length)
				{
					throw new Exception("dimension number of var " + varName + " is bigger than dimSplitNum.length");
				}
				//start to split
				List<String> sectionSpecs = getSectionSpecs(varShape, dimSplitNum, null);
				sectionSpecs.forEach(System.out::println);
				int index = 0;
				for(String sectionSpec : sectionSpecs)
				{
					System.out.println("spliting variable: " + varName + ", section " + index);
					String[] dimArray = sectionSpec.split(",");
					List<Dimension> dims = var.getDimensions();
					index++;
					String outputPath = outputDir + File.separator + inputFileName + "_" + varName + "_" + index + ".nc";
					try(NetcdfFileWriter exportWriter = NetcdfFileWriter.createNew(version, outputPath, null))
					{
						//add Dimension and Dimension-Variable, exportVariable
						for(int i=0;i<dims.size();i++)
						{
							Dimension tmpDim = dims.get(i);
							String tmpDimName = tmpDim.getFullName();
							String[] tmpDimSectionSpecArray = dimArray[i].split(":");
							int tmpDimLenghtlength = Integer.parseInt(tmpDimSectionSpecArray[1]) - Integer.parseInt(tmpDimSectionSpecArray[0]) + 1;
							Variable tmpDimVariable = writer.findVariable(tmpDimName);
							exportWriter.addDimension(null, tmpDimName, tmpDimLenghtlength);
							Variable tmpExportDimVariable = exportWriter.addVariable(null, tmpDimName, tmpDimVariable.getDataType(), tmpDimName);
							tmpDimVariable.getAttributes().forEach(att->
							{
								tmpExportDimVariable.addAttribute(att);
							});
						}
						Variable exportVariable = exportWriter.addVariable(null, varName, var.getDataType(), var.getDimensionsString());
						var.getAttributes().forEach(att->
						{
							exportVariable.addAttribute(att);
						});
						System.out.println(exportWriter.toString());
						exportWriter.create();
						
						//write data to Variables
						for(int i=0;i<dims.size();i++)
						{
							Dimension tmpDim = dims.get(i);
							String tmpDimName = tmpDim.getFullName();
							String tmpDimSectionSpec = dimArray[i];
							Variable tmpDimVariable = writer.findVariable(tmpDimName);
							Array tmpDimVariableArray = tmpDimVariable.read(tmpDimSectionSpec);
							int[] origin = {0};
							exportWriter.write(tmpDimName, origin, tmpDimVariableArray);
						}
						List<String> subSectionSpecs = getDimSectionSpecs(dimArray, null);
						for(int i=0;i<subSectionSpecs.size();i++)
						{
							System.out.println("\twriting " + (i+1) + "/" + subSectionSpecs.size());
							String subSectionSpec = subSectionSpecs.get(i);
							Array tmpExportVariableArray = var.read(subSectionSpec);
							int[] origin = getOrigin(sectionSpec, subSectionSpec);
							exportWriter.write(varName, origin, tmpExportVariableArray);
						}
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * get the origin of when write to new nc file.
	 * @param sectionSpec
	 * @param subSectionSpec
	 * @return
	 */
	public static int[] getOrigin(String sectionSpec, String subSectionSpec)
	{
		String[] dimSectionSpecStrs = sectionSpec.split(",");
		String[] dimSubSectionSpecStrs = subSectionSpec.split(",");
		int[] origin = new int[dimSectionSpecStrs.length];
		for(int i=0;i<dimSectionSpecStrs.length;i++)
		{
			int baseIndex = Integer.parseInt(dimSectionSpecStrs[i].split(":")[0]);
			int index = Integer.parseInt(dimSubSectionSpecStrs[i].split(":")[0]);
			origin[i] = index - baseIndex;
		}
		return origin;
			
	}
	public static List<String> getDimSectionSpecs(String[] dimArray, List<String> lastSectionSpecs)
	{
		if(dimArray.length == 0)
		{
			return lastSectionSpecs;
		}
		else
		{
			List<String> result = new ArrayList<>(); 
			String tmpDimSectionSpec = dimArray[0];
			String[] tmpRangeStr = tmpDimSectionSpec.split(":");
			int tmpFrom = Integer.parseInt(tmpRangeStr[0]);
			int tmpTo = Integer.parseInt(tmpRangeStr[1]);
			//fromIndex can be equal to toIndex, witch means only read one line
			for(int i=tmpFrom;i<=tmpTo;i+=blockLength)
			{
				int tmpSubFrom = i;
				int tmpSubTo = (i + blockLength) > tmpTo ? tmpTo : i + blockLength;
				String tmpDimSubSectionSpec = tmpSubFrom + ":" + tmpSubTo + ",";
				if(dimArray.length == 1)
				{
					tmpDimSubSectionSpec = tmpDimSubSectionSpec.substring(0, tmpDimSubSectionSpec.length() -1);
				}
				if(lastSectionSpecs == null || lastSectionSpecs.isEmpty())
				{
					result.add(tmpDimSubSectionSpec);
				}
				else
				{
					for(String lastSectionSpec : lastSectionSpecs)
					{
						result.add(lastSectionSpec + tmpDimSubSectionSpec);
					}
				}
			}
			String[] nextDimArray = Arrays.copyOfRange(dimArray, 1, dimArray.length);
			result = getDimSectionSpecs(nextDimArray, result);
			return result;
		}
		
	}
	
	private static List<String> getSectionSpecs(int[] varShape, int[] dimSplitNum, List<String> splitExtends)
	{
		if(varShape.length == 0)
		{
			return splitExtends;
		}
		else
		{
			int tmpVarLength = varShape[0];
			int tmpDimSplitNum = dimSplitNum[0];
			int tmpSplitShape = tmpVarLength / tmpDimSplitNum;
			List<String> result = new ArrayList<String>();
			for(int i=0;i<tmpVarLength;i+=tmpSplitShape)
			{
				int tmpFromIndex = i;
				int tmpToIndex = (i + tmpSplitShape) > tmpVarLength ? tmpVarLength-1 : i + tmpSplitShape - 1;
				String tmpDimSectionSpec = tmpFromIndex + ":" + tmpToIndex + ",";
				if(varShape.length == 1)
				{
					tmpDimSectionSpec = tmpDimSectionSpec.substring(0, tmpDimSectionSpec.length() - 1);
				}
				if(splitExtends == null || splitExtends.isEmpty())
				{
					result.add(tmpDimSectionSpec);
				}
				else
				{
					for(String tmpLastDimSectionSpec : splitExtends)
					{
						result.add(tmpLastDimSectionSpec + tmpDimSectionSpec);
					}
				}
			}
			int[] nextVarShape = Arrays.copyOfRange(varShape, 1, varShape.length);
			int[] nextDimSplitNum = Arrays.copyOfRange(dimSplitNum, 1, dimSplitNum.length);
			result = getSectionSpecs(nextVarShape, nextDimSplitNum, result);
			return result;
		}
	}
}
