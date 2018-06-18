package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper tbBrandMapper;

	@Override
	public List<TbBrand> findAll() {
		return tbBrandMapper.selectByExample(null);
	}

	/*@Override
	public PageResult findPage(int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		Page<TbBrand> brandPage = (Page<TbBrand>) tbBrandMapper.selectByExample(null);
		return new PageResult(brandPage.getTotal(), brandPage.getResult());
	}*/

	@Override
	public void add(TbBrand tbBrand) {
		tbBrandMapper.insert(tbBrand);
	}

	@Override
	public TbBrand findOne(Long id) {
		return tbBrandMapper.selectByPrimaryKey(id);
	}

	@Override
	public void update(TbBrand tbBrand) {
		tbBrandMapper.updateByPrimaryKey(tbBrand);
	}

	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			tbBrandMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(int pageNo, int pageSize, TbBrand tbBrand) {
		PageHelper.startPage(pageNo, pageSize);
		TbBrandExample tbBrandExample = new TbBrandExample();
		Criteria criteria = tbBrandExample.createCriteria();
		if (tbBrand != null) {
			if (tbBrand.getName() != null && !"".equals(tbBrand.getName())) {
				criteria.andNameLike("%" + tbBrand.getName() + "%");
			}
			if (tbBrand.getFirstChar() != null && !"".equals(tbBrand.getFirstChar())) {
				criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
			}
		}

		Page<TbBrand> brandPage = (Page<TbBrand>) tbBrandMapper.selectByExample(tbBrandExample);
		return new PageResult(brandPage.getTotal(), brandPage.getResult());
	}

	@Override
	public List<Map> selectOptionList() {

		return tbBrandMapper.selectOptionList();
		
	}

}
