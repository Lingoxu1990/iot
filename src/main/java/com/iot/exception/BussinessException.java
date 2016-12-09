package com.iot.exception;

/**
 * Created by xiongwenhui on 16/5/10.
 */
/**
 * 系统业务异常
 */
public class BussinessException extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = 2332608236621015980L;

    private String code;
    private String message;

    public BussinessException() {
        super();
    }

    public BussinessException(String message) {
        super(message);
    }

    public BussinessException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public BussinessException(Throwable cause) {
        super(cause);
    }

    public BussinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BussinessException(String code, String message, Throwable cause) {
        super(cause);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}





