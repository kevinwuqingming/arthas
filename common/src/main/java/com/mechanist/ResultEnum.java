package com.mechanist;

public class ResultEnum {
    public enum RedefineEnum{
        SUCCESS,
        CLASS_NOT_FOUND,
        REDEFINE_EXCEPTION,
        REPLACE_FILE_EXCEPTION,
        GET_RESULT_TIMEOUT,
        GET_RESULT_INTERRUPTED_EXCEPTION
    }

    public enum BshResultEnum{
        SUCCESS,
        RUN_BASH_EXCEPTION,
        RUN_BASH_TIME_OUT,
    }
}
