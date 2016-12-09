package com.iot.message;

/**
 * Created by liusheng on 16/2/18.
 */
public class Message extends MessageNoContent{
    //响应代码
    private String code;
    //响应的消息说明体
    private String message;
    //具体的消息响应体
    private Object content;

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

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
