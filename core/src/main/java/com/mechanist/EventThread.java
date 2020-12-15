package com.mechanist;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/***
 * 此线程用于取Event并执行,Event是单独的线程,此线程通过event.run();来执行Event。(串行执行)
 * Event类型包括GameEvent
 *
 * @author Kevin
 *
 */
public class EventThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(EventThread.class);

    private List<Event> events = new ArrayList<Event>();
    private Object lock = new Object();

    private boolean isRun = false;
    private boolean isSleepWhenIdle = true;

    public EventThread(String name) {
        super(name);
    }

    public void run() {
        this.isRun = true;
        do {
            try {
                beforeRunEvents();

                runEvents();

                afterRunEvents();

                if (isSleepWhenIdle) {
                    waitSleep(1);
                }
            } catch (Throwable t) {
                logger.error("GameEventThread", "Exception while run msg", t);
            }

        } while (isRun);

        runEvents();

        waitSleep(1);

        synchronized (this) {
            this.notifyAll();
        }
    }

    public synchronized void waitingForStop() {
        this.isRun = false;
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void beforeRunEvents() {
    }

    public void afterRunEvents() {
    }

    protected void waitSleep(long s) {
        try {
            Thread.sleep(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runEvents() {
        List<Event> es = getEvents();

        if (es == null || es.size() == 0) {
            //如果本次没有事件需要处理，那么接下来休息1毫秒
            isSleepWhenIdle = true;
            return;
        }
        isSleepWhenIdle = false;
        int size = es.size();

        for (int i = 0; i < size; i++) {
            Event event = es.get(i);
            if (event != null) {
                event.run();
            }
        }
    }

    public List<Event> getEvents() {
        List<Event> list = null;

        synchronized (lock) {
            if (events.size() > 0) {
                list = events;
                events = new ArrayList<Event>();
            }
        }

        return list;
    }

    public void pushEvent(Event event) {
        synchronized (lock) {
            if (this.isRun) {
                events.add(event);
            }
        }
    }

    public void pushBatch(List<Event> list) {
        synchronized (lock) {
            if (this.isRun) {
                events.addAll(list);
            }
        }
    }

    public void pushBatch(Event[] array) {
        synchronized (lock) {
            if (this.isRun) {
                for (int i = 0; i < array.length; i++) {
                    events.add(array[i]);
                }
            }
        }
    }

}
