package com.iot.exception;

/**
 * Created by xiongwenhui on 16/5/10.
 */

public class ParameterException extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = 2332608236621015980L;

    private String code;
    private String message;

    public ParameterException() {
        super();
    }

    public ParameterException(String message) {
        super(message);
    }

    public ParameterException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ParameterException(Throwable cause) {
        super(cause);
    }

    public ParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParameterException(String code, String message, Throwable cause) {
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
