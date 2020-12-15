package com.mechanist.msg;


public class MsgCode {
	public final static Object[] SearchClassReq = new Object[] { 1000, "类加载查询" };
    public final static Object[] SearchClassResp = new Object[] { 1001, "类加载查询返回" };
    public final static Object[] RedefineReq = new Object[] { 1002, "进行热更新" };
    public final static Object[] RedefineResp = new Object[] { 1003, "热更新返回" };
    public final static Object[] RunBashReq = new Object[] { 1004, "执行Bsh代码" };
    public final static Object[] RunBashResp = new Object[] { 1005, "执行Bsh代码返回" };
}
