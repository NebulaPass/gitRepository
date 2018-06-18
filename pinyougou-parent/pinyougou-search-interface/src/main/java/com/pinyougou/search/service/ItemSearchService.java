package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

	//查询条件和返回值都是map集合;
	public Map<String,Object> search(Map searchMap);
	
	public void importList(List list);
	
	public void deleteList(List goodsIds);
}
