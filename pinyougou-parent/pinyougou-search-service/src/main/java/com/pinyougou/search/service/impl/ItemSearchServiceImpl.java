package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
	
	@Autowired
	private SolrTemplate solrTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public Map<String, Object> search(Map searchMap) {
		
		Map<String, Object> map = new HashMap<>();
		/*//添加查询条件
		SimpleQuery query = new SimpleQuery();
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);
		//把查询出来的结果添加到map结合中;
		System.out.println(scoredPage.getTotalElements());
		map.put("rows", scoredPage.getContent());*/
		
		
		
		//高亮显示;
		map.putAll(searchList(searchMap));
		
		//根据关键字查询商品分类的名字;
		List categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
		//查询品牌和规格信息;
		if (categoryList.size()>0) {
			map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
		}
		
		//查询品牌和规格列表;
		String categoryName=(String)searchMap.get("category");
		if(!"".equals(categoryName)){//如果有分类名称
			map.putAll(searchBrandAndSpecList(categoryName));			
		}else{//如果没有分类名称，按照第一个查询
			if(categoryList.size()>0){
				map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
			}
		}
		
		return map;
	}	
	
	private Map searchList(Map searchMap){
		
		Map<String, Object> map = new HashMap<>();
		
		HighlightQuery query = new SimpleHighlightQuery();
		//设置高亮域;
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		//设置高亮显示的前缀和后缀;
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		highlightOptions.setSimplePostfix("</em>");
		//设置高亮选项,向Query查询条件中添加查询条件;
		query.setHighlightOptions(highlightOptions);
		
		//按照关键字进行查询数据;
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		
		
		//按照分类进行查询;
		if(!"".equals(searchMap.get("category"))){			
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		
		//按照品牌进行查询;
		if(!"".equals(searchMap.get("brand"))){			
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//过滤规格;
		if(searchMap.get("spec")!=null){
			Map<String,String> specMap= (Map) searchMap.get("spec");
			for(String key:specMap.keySet() ){
				Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );
				FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);				
			}			
		}
		
		//按照价格进行删选;
		//接收来自前端的信息;
		if (!"".equals(searchMap.get("price"))) {
			//对接收过来的字符串进行分割;
			String[] price = ((String)searchMap.get("price")).split("-");
			//System.out.println(Arrays.toString(price));
			//根据价格区间进行删选数据;
			if (!"0".equals(price[0])) {
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria );
				query.addFilterQuery(filterQuery);
			}
			if (!"*".equals(price[1])) {
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		
		//分页查询;
		Integer pageNo = (Integer) searchMap.get("pageNo");
		Integer pageSize = (Integer) searchMap.get("pageSize");
		//设置默认值;
		if (pageNo==null) {
			pageNo=1;
		}
		if (pageSize == null) {
			pageSize = 20;
		}
		
		query.setOffset((pageNo-1)*pageSize);
		query.setRows(pageSize);
		
		
		//进行排序查询;
		String sortValue = (String) searchMap.get("sort");
		String sortField = (String) searchMap.get("sortField");
		if (sortValue!=null && sortValue.equals("")) {
			if(sortValue.equals("ASC")){
				Sort sort = new	Sort(Sort.Direction.ASC, "item_"+sortField);
			}
			if (sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
			}
		}
		
		
		//遍历进行高亮处理的对象集合;
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query , TbItem.class);
		for (HighlightEntry<TbItem> h : page.getHighlighted()) {
			TbItem item = h.getEntity();
			//判断highlightentry中的数据是否为空?
			if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
				//设置高亮的结果;
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		
		//返回查询总页数和总记录数;
		map.put("total", page.getTotalElements());
		map.put("totalPage", page.getTotalPages());
		
		return map;
	}
	
	//根据关键字查询出分类的名字category;
	private List searchCategoryList(Map searchMap){
		
		//对从页面接收到的keywords进行去空格处理;
		String keywords = ((String) searchMap.get("keywords")).replace(" ", "");
		searchMap.put("keywords", keywords);
		
		List<String> list = new ArrayList<>();
		//按照关键字进行查询;
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//设置分组选项;
		//根据指定字段进行分组;
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		//将分组信息添加到查询条件Query中;
		query.setGroupOptions(groupOptions);
		//得到分组页;
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query , TbItem.class);
		
		//根据列名得到分组结果;
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		
		//得到分组结果的入口页;
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : content) {
			list.add(entry.getGroupValue());
		}
		return list;
	}
	
	//根据分类名称查询redis中的数据;
	private Map searchBrandAndSpecList(String category){
		Map  map = new HashMap<>();
		
		//根据分类名称查询模板id;
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		
		if (typeId != null ) {
			
			//判断typeId是否为null;如果不为null的话,就跟据typeId进行查询品牌名称和规格信息;
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			//将查询出来的数据存储到返回的map集合中;
			map.put("brandList", brandList);
			
			//查询规格信息;
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			//将数据存储到map集合中;
			map.put("specList", specList);
		}
			
		return map;
	}

	
	//将数据存储到索引库(solr)中;
	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteList(List goodsIds) {
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria);
		//将满足条件的数据删除;
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
	
}
