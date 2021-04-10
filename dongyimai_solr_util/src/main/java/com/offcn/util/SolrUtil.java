package com.offcn.util;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importTItemData(){
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //已经审核
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(example);

        //遍历全部通过审核的商品列表数据
        for (TbItem item :itemList) {
            System.out.println(item.getTitle());

            //读取规格数据，将字符串转换为json数据
            Map<String,Object> specMap = JSON.parseObject(item.getSpec(),Map.class);
            //创建一个新的Map集合存储转换后的拼音类型
            Map<String,Object> mapPinyin = new HashMap<>();
            //遍历，将将汉字转拼音
            for (String key:specMap.keySet()) {
                mapPinyin.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            //将转换的拼音类型放入map集合中
            item.setSpecMap(mapPinyin);
        }
        //保存数据到solr中
        solrTemplate.saveBeans(itemList);
        //提交事务
        solrTemplate.commit();

        System.out.println("导入成功");
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        SolrUtil solrUtil =(SolrUtil) context.getBean("solrUtil");
        solrUtil.importTItemData();

        Map map = new HashMap();
    }
}
