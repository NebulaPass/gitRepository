package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.druid.support.json.JSONParser;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

@Component
public class SolrUtil {
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private SolrTemplate solrTemplate;
	
	//批量导入商品数据
	public void importItemData(){
		//从数据库中查询数据;
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//查询已审核的;
		List<TbItem> itemList = itemMapper.selectByExample(example);
		for (TbItem item : itemList) {
			Map specMap = JSON.parseObject(item.getSpec(),Map.class);
			item.setSpecMap(specMap);
			System.out.println(item.getTitle());
		}
		
		//将查询出来的数据存储到solr中;
		solrTemplate.saveBeans(itemList);
		solrTemplate.commit();
	}
	
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
		solrUtil.importItemData();
	}
}
