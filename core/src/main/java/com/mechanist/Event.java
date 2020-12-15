package com.mechanist;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

public abstract class Event implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(Event.class);

	public final static int EVENT_STATE_0 = 0;
	public final static int EVENT_STATE_1 = 1;
	public final static int EVENT_STATE_2 = 2;
	public final static int EVENT_STATE_3 = 3;
	public final static int EVENT_STATE_4 = 4;

	private int eventState = EVENT_STATE_0;

	public Event() {
	}

	public void run() {
		runEvent();
	}

	private void runEvent() {
		try {
			switch (eventState) {
			case EVENT_STATE_0:
				handleEvent();
				break;
			case EVENT_STATE_1:
				handleEvent1();
				break;
			case EVENT_STATE_2:
				handleEvent2();
				break;
			case EVENT_STATE_3:
				handleEvent3();
				break;
			case EVENT_STATE_4:
				handleEvent4();
				break;
			}
		} catch (Exception ex) {
			handleException(ex);
		}
	}

	public void changeEventState(int state) {
		this.eventState = state;
	}

	public void handleEvent() throws Exception {
	}

	public void handleEvent1() throws Exception {
	}

	public void handleEvent2() throws Exception {
	}

	public void handleEvent3() throws Exception {
	}

	public void handleEvent4() throws Exception {
	}

	public void handleException(Exception ex) {
		logger.error("gameEvent",Thread.currentThread().getName(), ex);
	}

}
