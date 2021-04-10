package com.offcn.entity;

import java.io.Serializable;

/**
 * @Auther: lhq
 * @Date: 2020/8/12 14:47
 * @Description: 更新数据信息自定义实体类
 */
public class Result implements Serializable {

    private boolean success;  //更新标识
    private String message;  //更新信息

    public Result(){

    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
