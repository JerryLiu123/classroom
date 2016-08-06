package com.classroom.wnn.task;

public class TaskImportTest extends Task {

	private String i;
	
//	@Autowired
//	private ThreadPool taskPool;
	
	public TaskImportTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TaskImportTest(String i) {
		super();
		this.i = i;
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//super.run();
		boolean flag = true;
		while(flag){
			System.err.println("进入线程"+this.getTaskId());
			try {
				
				//this.add(i);
				Thread.sleep(8000);
				//nosecureAdd(i);
				flag = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
//		ServletContext servletContext = webApplicationContext.getServletContext();  
//		ApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
//		RedisThreadPool taskPool = (RedisThreadPool) ac.getBean("redisThreadPool");
//		Map<String, Boolean> map = taskPool.getFinishWorks();
//		map.put(this.getTaskId()+"", true);
//		taskPool.setFinishWorks(map);
		System.err.println("线程"+this.getTaskId()+"结束");
	}
	
	private void add(String value){
		synchronized (TaskImportTest.class){
			for(int c=0;c<1000;c++){
				System.out.print(value);
			}
		}
	}

	@Override
	public Task[] taskCore() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean useDb() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean needExecuteImmediate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String info() {
		// TODO Auto-generated method stub
		return "这是线程"+this.getTaskId();
	}


	public String getI() {
		return i;
	}


	public void setI(String i) {
		this.i = i;
	}
}
