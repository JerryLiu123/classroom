package task;

public class AAAAAA {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		String[] tken = "asdfasfd_0_1".split("_");
//		System.err.println(tken[tken.length-2]);
		new DDag("aaaa");
//		String aa = "123";
//		String bb = new String("123");
//		System.out.println(aa == bb);
//		A a = new A(new String("123"));
//		System.out.println(aa == a.a);
		//staticFun();
	}
	
	static AAAAAA aaaaaa = new AAAAAA();
	static {
		System.out.println("1");
	}
	{
		System.out.println("2");
	}
	public AAAAAA() {
		// TODO Auto-generated constructor stub
		System.out.println("3");
		System.out.println("a="+a+"--b="+b);
	}
	public static void staticFun(){
		System.out.println("4");
	}
	int a = 100;
	static int b = 200;

}
class A {
	String a;
	A(String a){
		this.a = a;
	}
}
class fDag{
	private static String cc = "fDag 的私有静态类变量";
	public static String dd = "fDag 的共有静态变量";
	static{
		System.out.println("fDag 的静态方法区");
	}
	
	public fDag() {
		// TODO Auto-generated constructor stub
		System.out.println(cc);
		System.out.println("fDag 的构造方法");
	}
}

class Work {
	private static String ee = "Work 的私有静态变量";
	static {
		System.out.println("Work 的静态方法");
	}
	{
		System.out.println("--------------");
	}
	public Work(String name) {
		// TODO Auto-generated constructor stub
		System.out.println("Work 的构造方法"+name);
	}
}

class MDag extends fDag{
	private static Work work = new Work("MDag");
	static{
		System.out.println("MDag 的静态方法");
	}
	public MDag(String name) {
		// TODO Auto-generated constructor stub
		System.out.println("MDag 的构造方法"+name);
	}
}
class DDag extends MDag{
	private static Work work = new Work("DDag");
	static{
		System.out.println("DDag 的静态方法");
	}
	public DDag(String name) {
		// TODO Auto-generated constructor stub
		super(name);
		System.out.println("DDag 的构造方法"+name);
	}
}
