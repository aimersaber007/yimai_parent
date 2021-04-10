package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;


    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
	/*public void add(TbGoods goods) {
		goodsMapper.insert(goods);		
	}*/
    public void add(Goods goods) {
        //设置未申请状态
        goods.getGoods().setAuditStatus("0");
        goodsMapper.insert(goods.getGoods());
        //设置id
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //插入商品扩展数据
        goodsDescMapper.insert(goods.getGoodsDesc());

        saveItemList(goods);


    }

    private void setItemValues(Goods goods, TbItem item) {
        //商品的sku编号
        item.setGoodsId(goods.getGoods().getId());
        //商家编号
        item.setSellerId(goods.getGoods().getSellerId());
        //商品分类编号（三级分类编号）
        item.setCategoryid(goods.getGoods().getCategory3Id());
        //创建日期
        item.setCreateTime(new Date());
        //修改日期
        item.setUpdateTime(new Date());
        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());
        //图片地址
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));
        }
    }

    private void saveItemList(Goods goods){

        if ("1".equals(goods.getGoods().getIsEnableSpec())) {

            for (TbItem item : goods.getItemList()) {

                String title = goods.getGoods().getGoodsName();

                Map<String, Object> specMap = JSON.parseObject(item.getSpec());

                for (String key : specMap.keySet()) {

                    title += specMap.get(key) + " ";
                }
                item.setTitle(title);

                setItemValues(goods, item);

                itemMapper.insert(item);
            }

        } else {

            TbItem item = new TbItem();

            item.setTitle(goods.getGoods().getGoodsName());

            item.setPrice(goods.getGoods().getPrice());

            item.setStatus("1");

            item.setIsDefault("1");

            item.setNum(9999);

            item.setSpec("{}");

            setItemValues(goods, item);

            itemMapper.insert(item);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //设置未申请状态，修改了需要重新审核
        goods.getGoods().setAuditStatus("0");
        //保存商品表
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //保存商品扩展表
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //删除原有的sku列表数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(example);
        //添加新的sku列表数据
        saveItemList(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {

        //设置TBgoods表的id
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);

        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //查询条件商品id
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(example);

        Goods goods = new Goods();
        goods.setGoods(tbGoods);
        goods.setGoodsDesc(tbGoodsDesc);
        goods.setItemList(itemList);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            //修改商品的状态
            goods.setIsDelete("1");
            //更新
            goodsMapper.updateByPrimaryKey(goods);
            //修改商品sku状态为删除
            List<TbItem> itemList = findItemListByGoodsIdAndStatus(ids,"1");
            for (TbItem tbItem:itemList){
                tbItem.setStatus("3");
                itemMapper.updateByPrimaryKey(tbItem);
            }
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                //精确查询
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            /*if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }*/
            criteria.andIsDeleteIsNull();
        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        //循环商品id
        for (Long id:ids){
            //根据商品id获取商品信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            //修改商品状态
            goods.setAuditStatus(status);
            //更新商品信息到数据库
            goodsMapper.updateByPrimaryKey(goods);
            //修改sku状态
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(id);
            List<TbItem> itemList = itemMapper.selectByExample(example);
            if(!CollectionUtils.isEmpty(itemList)){
                for (TbItem item:itemList){
                    item.setStatus(status);
                    itemMapper.updateByPrimaryKey(item);
                }
            }

        }
    }

    @Override
    public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status) {

        TbItemExample example = new TbItemExample();

        TbItemExample.Criteria criteria = example.createCriteria();

        criteria.andGoodsIdIn(Arrays.asList(goodsIds));

        criteria.andStatusEqualTo(status);

        return itemMapper.selectByExample(example);
    }


}
