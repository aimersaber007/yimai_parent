package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //根据商品的skuid查询sku商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);//返回item商品信息
        //判断商家信息是否存在
        if (item==null){//不存在
            throw new RuntimeException("商品不存在");
        }
        //判断商品的状态是否为通过
        if (!item.getStatus().equals("1")){//不为1，表示未通过
            throw new RuntimeException("商品状态未通过");
        }
        //获取商家id
        String sellerId = item.getSellerId();//商家id
        //根据商家id判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.1如购物车列表不存在该商家的购物车
        if (cart==null){
            cart = new Cart();//4.2新建购物车
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item,num);
            //创建购物车明细列表
            List orderItemList = new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.3将新建的购车对象添加到购物车列表中
            cartList.add(cart);
        }else {
            //如购物车列表存在该商家的购物车
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId); //查询购物车明细列表中是否存在该商品
            //如果没有。新增购物车明细
            if (orderItem==null){
                orderItem = createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else {//有。在原购物车明细上增加数量num，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
                //如果数量操作小于0，移除
                if (orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
                //如果移除后cart的明细数量为0，将cart移除
                if (cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }





        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据"+username);
        List<Cart> cartList =(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for (Cart cart:cartList2){
            for (TbOrderItem orderItem:cart.getOrderItemList()){
                cartList1 = addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家id查询购物车列表
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart:cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建订单明细
     * @param item sku
     * @param num 数量
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if (num<=0){
            throw new RuntimeException("数量不能为0");
        }
        TbOrderItem orderItem = new TbOrderItem();

        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    /**
     * 根据商品明细id查询
     * @param orderItemList
     * @param itemId
     * @return
     */
    public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem:orderItemList) {
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }
}
