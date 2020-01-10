package test.io;

import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class WriteNetCDFTest {

	public static void main(String[] args)
	{
		/*
		String path = "/home/petrel/Documents/random4.nc4";
		int resolution = 30; //resolution in meters
		int degreesNorth = 30;	//3 degrees_north
		int blockNum = 100; 
		DataType dataType = DataType.SHORT;
		String valueType = "Random";	//Random|Single|Linear
		double singleValue = 3.14;
		int degreesEast = degreesNorth * 2;
		*/
		//generateData(30, 60, 120, DataType.SHORT, "Random", 0, 1000, "/media/petrel/新加卷/random7.nc", NetcdfFileWriter.Version.netcdf3c);
		//generateData(30, 20, 20, DataType.SHORT, "Random", 0, 1000, "/home/petrel/Documents/random8.nc", NetcdfFileWriter.Version.netcdf3);
		//generateData(30, 20, 30, DataType.SHORT, "Random", 0, 1000, "/home/petrel/Documents/random9.nc", NetcdfFileWriter.Version.netcdf4);
		//generateData(30, 20, 20, DataType.SHORT, "Single", 1.34356, 1000, "/home/petrel/Documents/random10.nc", NetcdfFileWriter.Version.netcdf3);
		generateManyData();
	}//
	public static void generateManyData()
	{
		for(int i=660;i<10000;i++)
		{
			generateData(3000, 1, 1, DataType.SHORT, "Random", 0, 100, "/home/petrel/Documents/random_many/random_"+ i +".nc", NetcdfFileWriter.Version.netcdf4);
		}
	}
	public static void generateData(int resolution, int degreesNorth, int degreesEast, DataType dataType,
			String valueType, double singleValue, int blockNum, String path, NetcdfFileWriter.Version version)
	{

		int latLength = (degreesNorth * 111 * 1000 / resolution);
		int lonLength = (degreesEast * 111 * 1000 / resolution);
		try
		{
			
			NetcdfFileWriter writer = NetcdfFileWriter.createNew(version, path, null);//.createNew(NetcdfFileWriter.Version.netcdf4, path);
			
			long time1 = System.currentTimeMillis();
			Dimension dimLat = writer.addDimension("lat", latLength);
			Dimension dimLon = writer.addDimension("lon", lonLength);
			Variable varLat = writer.addVariable("lat", DataType.FLOAT, "lat");
			varLat.addAttribute(new Attribute("units", "degrees_north"));
			Variable varLon = writer.addVariable("lon", DataType.FLOAT, "lon");
			varLon.addAttribute(new Attribute("units", "degrees_east"));
			List<Dimension> varRandomDims = new ArrayList<>();
			varRandomDims.add(dimLat);
			varRandomDims.add(dimLon);
			Variable varRandom = writer.addVariable("random", dataType, varRandomDims);
			varRandom.addAttribute(new Attribute("units", "meters"));
			writer.create();
			
			//write lat, lon
			Array arrLat = Array.factory(DataType.FLOAT, new int[] {latLength});
			Array arrLon = Array.factory(DataType.FLOAT, new int[] {lonLength});
			float stepLat = degreesNorth * 1.0f / latLength;
			float stepLon = degreesEast * 1.0f / lonLength;
			for(int i=0;i<latLength;i++)
			{
				arrLat.setFloat(i, i * stepLat - 90);
			}
			for(int i=0;i<lonLength;i++)
			{
				arrLon.setFloat(i, i * stepLon - 180);
			}
			int[] origin = {0};
			writer.write("lat", origin, arrLat);
			writer.write("lon", origin, arrLon);
			//write random
			for(int i=0;i<latLength;i+=blockNum)
			{
				System.out.println("writing random : " +i+" / "+latLength+" columns");
				int from = i;
				int length = i + blockNum > latLength ? latLength - i : blockNum;
				Array arrRandom = Array.factory(dataType, new int[] {length, lonLength});
				//ArrayFloat arrRandom = new ArrayFloat.D2(length, lonLength);
				Index indRandom = arrRandom.getIndex();
				for(int j=0;j<length;j++)
				{
					for(int k=0;k<lonLength;k++)
					{
						double value = 0;
						switch(valueType)
						{
						case "Random":
							value = ((Math.cos(from + j) * 3 + Math.sin(k) * 2 + (float)Math.random()) * 1000);
							break;
						case "Single":
							value = singleValue;
							break;
						case "Linear":
							value = ((from + j) * k);
							break;
						}
						
						if(DataType.SHORT == dataType)
						{
							arrRandom.setShort(indRandom.set(j, k), (short)value);
						}
						else if(DataType.FLOAT == dataType)
						{
							arrRandom.setFloat(indRandom.set(j, k), (float)value);
						}
						
					}
				}
				origin = new int[]{from, 0};
				writer.write("random", origin, arrRandom);
			}
			writer.close();
			
			long time2 = System.currentTimeMillis();
			System.out.println("time used: "+(time2 - time1));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
