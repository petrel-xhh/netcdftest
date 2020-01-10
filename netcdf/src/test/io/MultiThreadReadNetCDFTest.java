package test.io;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadReadNetCDFTest {

	public static void main(String[] args)
	{
		readTest();
	}
	//public static void 
	public static void readTest()
	{
		String path = "/home/petrel/Documents/random3.nc4";
		String varName = "random";
		String sectionSpec = "40000:40999,80000:80999";
		int times = 1;

		
		
		
	}
	public static void readTest(String path, String varName, String sectionSpec, int times, final int threadNum)
	{
		
		List<Thread> threadList = new ArrayList<>();
		for(int i=0;i<threadNum;i++)
		{
			threadList.add(new Thread("T"+i) {
				@Override
				public void run()
				{
					//System.out.print(this.getName() + "\t");
					String resultString = "threadNum:"+threadNum+","+ReadNetCDFTest.readTest(path, varName, sectionSpec, times);
					System.out.println(resultString);
				}
			});
		}
		threadList.forEach(thread->
		{
			thread.start();
		});
	}
}
