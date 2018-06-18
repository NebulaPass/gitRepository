package com.pinyougou.page.service;

public interface ItemPageService {

	//生成商品详情静态页面;
	public boolean genItemHtml(Long goodsId);
	
	//删除商品静态页面;
	public void deleteItemHtml(Long [] goodsIds);
}
