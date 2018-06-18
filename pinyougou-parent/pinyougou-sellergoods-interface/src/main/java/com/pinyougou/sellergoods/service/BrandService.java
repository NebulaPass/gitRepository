package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {
	
	public List<TbBrand> findAll();
	
	//public PageResult findPage(int pageNo,int pageSize);
	
	public void add(TbBrand tbBrand);
	
	public TbBrand findOne(Long id);
	
	public void update(TbBrand tbBrand);
	
	public void delete(Long [] ids);
	
	public PageResult findPage(int pageNo,int pageSize,TbBrand tbBrand);
	
	public List<Map> selectOptionList();
}
