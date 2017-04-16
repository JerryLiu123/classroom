package task;

public class StartTest {
	public static void main(String[] args) {
		startFun();
	}
	
	static StartTest test = new StartTest();
	static {
		System.out.println("1");
	}	
	{
		System.out.println("2");
	}
	public StartTest() {
		// TODO Auto-generated constructor stub
	
		System.out.println("3");
		System.out.println("a="+a+"    b="+b);
	}
	public static void startFun(){
		System.out.println("4");
		
		String aa;
	}
	int a = 100;
	static int b = 200;
	
}

//class Work {
//	private static String ee = "Work 的私有静态变量";
//	static {
//		System.out.println("Work 的静态方法");
//	}
//	{
//		System.out.println("--------------");
//	}
//	public Work(String name) {
//		// TODO Auto-generated constructor stub
//		System.out.println("Work 的构造方法"+name);
//	}
//}
