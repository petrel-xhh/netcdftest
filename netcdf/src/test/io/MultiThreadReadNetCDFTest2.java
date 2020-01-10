package test.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

public class MultiThreadReadNetCDFTest2 {

	public static Map<String, NetcdfFileWriter> writerMap = new HashMap<>();
	public final static Map<Integer, Map<String, List<Long>>> usedTimeMap = new HashMap<>();
	public static void main(String[] args)
	{
		readTest();
	}
	public static void readTest()
	{
		String path = "/home/petrel/Documents/random4.nc4";
		String varName = "random";
		
		String[] sectionSpecArr = {"4000:4000, 5000:5999", "4000:4000, 5000:5999"};
		int times = 1000;
		int[] threadNumArr = {10, 20};
		for(int i=0;i<threadNumArr.length;i++)
		{
			Map<String, List<Long>> sectionUsedTimeMap = new HashMap<>();
			usedTimeMap.put(i, sectionUsedTimeMap);
			for(int j=0;j<sectionSpecArr.length;j++)
			{
				sectionUsedTimeMap.put(sectionSpecArr[j], Collections.synchronizedList(new ArrayList<>()));
				readTest(path, varName, sectionSpecArr[j], times, threadNumArr[i]);
				
			}
		}
		usedTimeMap.forEach((threadNum, sectionUsedTimeMap)->
		{
			sectionUsedTimeMap.forEach((sectionSepc, usedTimeList)->
			{
				long totalUsedTime = 0;
				for(int i=0;i<usedTimeList.size();i++)
				{
					totalUsedTime += usedTimeList.get(i);
				}
				float avgUsedTime = totalUsedTime * 1.0f/ usedTimeList.size();
				System.out.println("threadNum:"+threadNum+",sectionSpec:"+sectionSepc+",avgUsedTime:"+avgUsedTime);
			});
		});
		
	}
	public static void readTest(String path, String varName, String sectionSpec, int times, int threadNum)
	{
		List<Thread> threadList = new ArrayList<>();
		for(int i=0;i<threadNum;i++)
		{
			Thread thread = new Thread("T"+i) {
				@Override
				public void run()
				{
					//String resultString = "threadNum:"+threadNum+","+MultiThreadReadNetCDFTest2.readData(path, varName, sectionSpec, times);
					//System.out.println(resultString);
					long usedTime = MultiThreadReadNetCDFTest2.readData(path, varName, sectionSpec, times);
					MultiThreadReadNetCDFTest2.usedTimeMap.get(threadNum).get(sectionSpec).add(usedTime);
				}
			};
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			threadList.add(thread);
		}
		threadList.forEach(thread->
		{
			thread.start();
		});
	}
	public static long readData(String path, String varName, String sectionSpec, int times)
	{
		if(!writerMap.containsKey(path))
		{
			NetcdfFileWriter writer;
			try {
				writer = NetcdfFileWriter.openExisting(path);
				writerMap.put(path, writer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		}
		NetcdfFileWriter writer = writerMap.get(path);
		long time0 = System.currentTimeMillis();
		try
		{
			Variable var = writer.findVariable(varName);
			//long time1 = System.currentTimeMillis();
			for(int i = 0;i < times; i++)
			{
				var.read(sectionSpec);
			}
			long time2 = System.currentTimeMillis();
			//String resultString = ("file:"+path+",varName:"+varName+",sectionSpec:"+sectionSpec+",times:"+times+",openFileTime:"+(time1-time0)+",readDataTime:"+(time2-time1));
			//return resultString;
			return time2-time0;
		}catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}
}
