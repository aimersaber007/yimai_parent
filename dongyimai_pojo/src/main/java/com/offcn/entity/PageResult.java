package com.offcn.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: lhq
 * @Date: 2020/8/12 11:39
 * @Description: 分页的自定义实体类
 */
public class PageResult implements Serializable {

    private Long total;   //总记录数
    private List rows;      //分页之后的集合


    //注意：如果声明有参的构造方法，则一定要声明无参的构造方法
    public PageResult(){

    }


    public PageResult(Long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
