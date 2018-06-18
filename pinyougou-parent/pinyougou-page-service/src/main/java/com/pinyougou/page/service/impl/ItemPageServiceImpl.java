package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;


import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;


@Service
public class ItemPageServiceImpl implements ItemPageService {
	
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	
	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbItemMapper itemMapper;

	@Override
	public boolean genItemHtml(Long goodsId) {
		//创建配置对象;
		Configuration configuration = freeMarkerConfig.getConfiguration();
		try {
			//获取模板;
			Template template = configuration.getTemplate("item.ftl");
			//获取数据模型;
			Map dataModel = new HashMap<>();
			//查询数据库中的信息;(这些数据都是我么需要在页面中进行显示的数据;)
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", goods);
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goodsDesc", goodsDesc);
			//System.out.println(goodsDesc.getItemImages());
			
			//查询商品分类;
			String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			dataModel.put("itemCat1", itemCat1);
			dataModel.put("itemCat2", itemCat2);
			dataModel.put("itemCat3", itemCat3);
			//SKU列表
			TbItemExample example = new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo("1");
			criteria.andGoodsIdEqualTo(goodsId);
			example.setOrderByClause("is_default desc");
			List<TbItem> itemList = itemMapper.selectByExample(example);
			dataModel.put("itemList", itemList);
			
			//创建输出流;
			Writer out = new FileWriter("J:/yemian/"+goodsId+".html");
			//生成文件;
			template.process(dataModel, out);
			//关闭资源;
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return false;
	}

	@Override
	public void deleteItemHtml(Long[] goodsIds) {
		try {
			for (Long id : goodsIds) {
				new File("J:/yemian/"+id+".html").delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
