package test.io;

import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class ReadNetCDFTest {

	public static void main(String[] args)
	{
		readTest("/home/petrel/Documents/random3.nc4", "random");
		readTest("/home/petrel/Documents/random4.nc4", "random");
	}
	public static void readTest(String path, String varName)
	{
		//99
		readTest(path, varName, "0:0, 0:99");
		readTest(path, varName, "0:0, 80000:80099");
		readTest(path, varName, "40000:40000, 80000:80099");
		readTest(path, varName, "40000:40049, 80000:80099");
		readTest(path, varName, "40000:40099, 80000:80099");//100*100
		//999
		readTest(path, varName, "0:0, 0:999");
		readTest(path, varName, "0:0, 80000:80999");
		readTest(path, varName, "40000:40000, 80000:80999");
		readTest(path, varName, "40000:40049, 80000:80999");
		readTest(path, varName, "40000:40099, 80000:80999");
		readTest(path, varName, "40000:40499, 80000:80999");
		readTest(path, varName, "40000:40999, 80000:80999");//1000*1000
		//9999
		readTest(path, varName, "0:0, 0:9999");
		readTest(path, varName, "0:0, 80000:89999");
		readTest(path, varName, "40000:40000, 80000:89999");
		readTest(path, varName, "40000:40049, 80000:89999");
		readTest(path, varName, "40000:40099, 80000:89999");
		readTest(path, varName, "40000:40499, 80000:89999");
		readTest(path, varName, "40000:40999, 80000:89999");
		readTest(path, varName, "40000:44999, 80000:89999");
		readTest(path, varName, "40000:49999, 80000:89999");
		
	}
	public static void readTest(String path, String varName, String sectionSpec)
	{
		String resultString = readTest(path, varName, sectionSpec, 1000);
		System.out.println(resultString);
		/*for(int i=0;i<5000;i+=100)
		{
			
		}*/
	}
	public static String readTest(String path, String varName, String sectionSpec, int times)
	{
		long time0 = System.currentTimeMillis();
		try(NetcdfFileWriter writer = NetcdfFileWriter.openExisting(path))
		{
			Variable var = writer.findVariable(varName);
			long time1 = System.currentTimeMillis();
			for(int i = 0;i < times; i++)
			{
				var.read(sectionSpec);
			}
			long time2 = System.currentTimeMillis();
			String resultString = ("file:"+path+",varName:"+varName+",sectionSpec:"+sectionSpec+",times:"+times+",openFileTime:"+(time1-time0)+",readDataTime:"+(time2-time1));
			return resultString;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
