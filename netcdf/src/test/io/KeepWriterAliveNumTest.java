package test.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ucar.nc2.NetcdfFileWriter;

public class KeepWriterAliveNumTest {

	public static Map<String, NetcdfFileWriter> writerMap = new HashMap<>();
	public static void main(String[] args)
	{
		String dir = "/home/petrel/Documents/random_many";
		File dirFile = new File(dir);
		File[] dirFiles = dirFile.listFiles();
		int num = 0;
		for(File tmpFile: dirFiles)
		{
			String tmpPath = tmpFile.getAbsolutePath();
			if(tmpPath.endsWith(".nc"))
			{
				try
				{
					NetcdfFileWriter writer = NetcdfFileWriter.openExisting(tmpPath);
					writerMap.put(tmpPath, writer);
					System.out.println("aliveNum: " + num);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			num++;
		}
		writerMap.forEach((k, v)->
		{
			System.out.println(k+" : "+v);
		});
	}
}
