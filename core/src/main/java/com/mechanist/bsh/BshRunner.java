package com.mechanist.bsh;

import bsh.EvalError;
import bsh.Interpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BshRunner {
    private static Logger logger = LoggerFactory.getLogger(BshRunner.class);
    private Interpreter it = new Interpreter();

    public BshRunner() {
    }

    public Object eval(String statements) throws EvalError {
        try {
            logger.info("BshRunner", "run bsh statements:", statements);
            // check
            String filterdCmd = statements.trim().toLowerCase();
            int foundExit = filterdCmd.indexOf("exit");
            if (foundExit >= 0) {
                Pattern pName = Pattern.compile("exit[\\s]?([\\s]?)[\\s]?");
                Matcher matcherName = pName.matcher(filterdCmd);
                if (matcherName.find()) {
                    String res = "exit() not allowed!\n";
                    return res;
                }
            }
            return it.eval(statements);
        } catch (Exception ex) {
            logger.error("BshRunner", "bsh error:", ex);
            throw ex;
        }
    }
}
