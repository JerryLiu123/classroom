package com.classroom.wnn.task;


public class TestTaskWork extends Task {

	private int value;
	
	public TestTaskWork(int value) {
		super();
		// TODO Auto-generated constructor stub
		this.value = value;
	}

	
	public TestTaskWork() {
		super();
	}


	@Override
	public Task[] taskCore() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean needExecuteImmediate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String info() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("--------------------------------"+this.value);
	}


	@Override
	protected boolean useDb() {
		// TODO Auto-generated method stub
		return false;
	}

}
