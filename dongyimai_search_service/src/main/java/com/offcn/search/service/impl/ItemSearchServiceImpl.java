package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemCat;
import com.offcn.pojo.TbItemCatExample;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map = new HashMap<>();
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        map.put("rows",page.getContent());

        //多关键字查询
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        //查询列表 调用私有方法searchList
        map.putAll(searchList(searchMap));

        //根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);

        map.put("categoryList",categoryList);


        String category = (String) searchMap.get("category");
        //查询品牌和规格列表
        if(!StringUtils.isEmpty(category)){
            map.putAll(searchBrandSpecList(category));
        }else{
            if (categoryList.size()>0){
                Map map1 = searchBrandSpecList((String) categoryList.get(0));
                map.putAll(map1);
            }
        }

        return map;
    }

    /**
     * 查询高亮列表集合
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){

        Map map = new HashMap();

        //创建高亮查询器
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //创造高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置需要高亮显示字段
        highlightOptions.addField("item_title");
        //设置需要高亮的字段
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //关联高亮选项到高亮查询器对象
        query.setHighlightOptions(highlightOptions);
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联高亮选项到查询选择器
        query.addCriteria(criteria);

        //按照分类筛选
        if (!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //7.2按照品牌进行筛选 ，品牌过滤
        if (!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));

            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);

            query.addFilterQuery(filterQuery);
        }
        //7.3 过滤规格
        if (null!=searchMap.get("spec")){
            Map<String,String> specMap = (Map) searchMap.get("spec");
            for (String key:specMap.keySet()) {
                //将动态域转换
                Criteria filterCriteria = new Criteria("item_spec_"+ Pinyin.toPinyin(key,"").toLowerCase()).is(specMap.get(key));

                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);

                query.addFilterQuery(filterQuery);
            }
        }

        //价格筛选
        if (!"".equals(searchMap.get("price"))){
            String[] prices = ((String) searchMap.get("price")).split("-");
            //如果起点区间不等于0
            if (!prices[0].equals("0")){
                //将大于0的价格存入构造器中
                Criteria filterCriteria = new Criteria("item_price").greaterThan(prices[0]);

                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);

                query.addFilterQuery(filterQuery);
            }
            //如果区间不等于*
            if (!prices[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(prices[1]);

                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);

                query.addFilterQuery(filterQuery);
            }
        }
        //分页查询
        //提取页码
        Integer pageNo =(Integer) searchMap.get("pageNo");
        if (pageNo==null){
            //默认是第一页
            pageNo=1;
        }
        Integer pageSize =(Integer) searchMap.get("pageSize");
        if (pageSize==null){
            pageSize=20;
        }
        query.setOffset((pageNo-1)*pageSize);//从第几条记录查询
        query.setRows(pageSize);//设置查询条数

        //4.1.1排序
        String sortValue = (String) searchMap.get("sort");//asc DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (sortValue!=null && !sortValue.equals("")){
            //升序
            if (sortValue.equals("ASC")){
                Sort sort =new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            //降序
            if ("DESC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        //发出带高亮数据查询请求
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query,TbItem.class);
        //获取查询对象结果集合
        //List<TbItem> list = highlightPage.getContent();
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
        //循环集合对象
        for (HighlightEntry<TbItem> highlightEntry:highlighted) {
            //获取到对象TBitem的高亮集合
            TbItem item = highlightEntry.getEntity();
            //List<HighlightEntry.Highlight> highlights = highlightPage.getHighlights(item);
            //循环高亮集合
            if (highlightEntry.getHighlights().size()>0 && highlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                //获取到第一个字段的高亮对象
               List<HighlightEntry.Highlight> highlightList =  highlightEntry.getHighlights();
                List<String> snippletsList = highlightList.get(0).getSnipplets();
                //List<String> highlightSnipplets = highlights.get(0).getSnipplets();
                System.out.println("高亮"+snippletsList.get(0));

                item.setTitle(snippletsList.get(0));
            }
        }

        map.put("rows",highlightPage.getContent());

        //2.2返回总页数
        map.put("totalPages",highlightPage.getTotalPages());
        //返回总记录数
        map.put("total",highlightPage.getTotalElements());

        return map;
    }

    private List searchCategoryList(Map searchMap){

        List<String> list = new ArrayList<>();
        //创建查询对象
        Query query = new SimpleQuery();
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");

        query.setGroupOptions(groupOptions);
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //将查询的条件重新设置
        query.addCriteria(criteria);

        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query,TbItem.class);
        //根据列得到分组数据
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry:content) {
            list.add(entry.getGroupValue());
        }
        return list;
    }

    private Map searchBrandSpecList(String category){
        Map map = new HashMap();
        Long typeId =(Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (null!=typeId){
            List brandList =(List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            List specList =(List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }
        return map;
    }

    @Override
    //导入sku
    public void importList(List<TbItem> list) {

        for (TbItem item:list) {
            System.out.println(item.getTitle());
            //将json数据转换为map,从数据库中查询spec数据
            Map<String,Object> specMap = JSON.parseObject(item.getSpec(),Map.class);

            Map<String,Object> map = new HashMap<String,Object>();
            //遍历
            for (String key:specMap.keySet()) {
                //进行拼音转化，重新存入到新集合
                map.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            item.setSpecMap(map);
        }

        solrTemplate.saveBeans(list);
        solrTemplate.commit();
        System.out.println("新增商品导入成功");

    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品id"+goodsIdList);
        //创建查询选择器
        Query query = new SimpleQuery();

        Criteria criteria = new Criteria("item_goods_id").in(goodsIdList);
        //重新设置
        query.addCriteria(criteria);
        //执行删除
        solrTemplate.delete(query);
        //提交事务
        solrTemplate.commit();
    }

}
