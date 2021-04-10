package com.offcn.page.service;

public interface ItemPageService {

    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    //删除商品详情页
    public  boolean deleteItemHtml(Long[] goodsIds);
}
