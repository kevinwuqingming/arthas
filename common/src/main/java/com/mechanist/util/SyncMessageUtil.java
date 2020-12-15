package com.mechanist.util;

import com.google.protobuf.GeneratedMessageV3;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class SyncMessageUtil {
	private static ConcurrentMap<Long, Exchanger<GeneratedMessageV3>> exchangerMap = new ConcurrentHashMap<Long, Exchanger<GeneratedMessageV3>>();
	
	private static AtomicInteger atomicMsgId = new AtomicInteger(0);
	
	/**
	 * 获取异步交互消息的唯一ID
	 * @return    设定文件 
	 * @return long    返回类型
	 */
	public static long getExchangerMessageId() {
		long time1 = (System.currentTimeMillis() ) & 0x1fffffff;
		long time2 = (System.nanoTime() )  & 0x00003fff; 
		int num = atomicMsgId.incrementAndGet();
		long id = time1 << 64 | time2 << 32 | num;
		return id;
	}
	
	private static Exchanger<GeneratedMessageV3> getAndRemoveExchanger(long exchangerId) {
		return exchangerMap.remove(exchangerId);
	}
	
	public static Exchanger<GeneratedMessageV3> getExchanger(long exchangerId) {
		return exchangerMap.get(exchangerId);
	}
	
	public static void addExchanger(long exchagerId) {
		exchangerMap.put(exchagerId, new Exchanger<GeneratedMessageV3>());
	}
	
	public static void addExchanger(long exchangerId, Exchanger<GeneratedMessageV3> exchanger) {
		exchangerMap.put(exchangerId, exchanger);
	}

	public static void exchangeResponse(long exchangerId, GeneratedMessageV3 messageV3){
		Exchanger<GeneratedMessageV3> exchanger = getAndRemoveExchanger(exchangerId);
		if(exchanger != null){
			try {
				exchanger.exchange(messageV3, 3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
	}
}
