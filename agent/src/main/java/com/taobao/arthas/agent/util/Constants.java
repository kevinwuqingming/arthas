package com.taobao.arthas.agent.util;

import java.io.File;

/**
 * @author ralf0131 2016-12-28 16:20.
 */
public class Constants {

    private Constants() {
    }

    /**
     * Spy的全类名
     */
    public static final String SPY_CLASSNAME = "java.arthas.Spy";

    /**
     * 中断提示
     */
    public static final String Q_OR_CTRL_C_ABORT_MSG = "Press Q or Ctrl+C to abort.";

    /**
     * 空字符串
     */
    public static final String EMPTY_STRING = "";

    /**
     * 命令提示符
     */
    public static final String DEFAULT_PROMPT = "$ ";


    /**
     * 方法执行耗时
     */
    public static final String COST_VARIABLE = "cost";

    public static final String CMD_HISTORY_FILE = System.getProperty("user.home") + File.separator + ".arthas" + File.separator + "history";

}
