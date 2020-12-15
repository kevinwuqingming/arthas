package com.mechanist;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

public class EventReceiverThread extends EventThread {

	private static Logger logger = LoggerFactory.getLogger(EventReceiverThread.class);

	private static EventReceiverThread instance = new EventReceiverThread();

	public static EventReceiverThread getInstance() {
		if(instance.isAlive()){
			return instance;
		}
		else{
			instance = new EventReceiverThread();
			return instance;
		}
	}

	public EventReceiverThread() {
		super("GameEventReceiverThread");
	}


	@Override
	public void beforeRunEvents() {

	}

	@Override
	public void afterRunEvents() {
	}

}
