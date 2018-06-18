package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.request.CollectionAdminRequest.Create;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;
import com.pinyougou.seckill.service.SeckillGoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillGoods> page=   (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillGoods!=null){			
						if(seckillGoods.getTitle()!=null && seckillGoods.getTitle().length()>0){
				criteria.andTitleLike("%"+seckillGoods.getTitle()+"%");
			}
			if(seckillGoods.getSmallPic()!=null && seckillGoods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+seckillGoods.getSmallPic()+"%");
			}
			if(seckillGoods.getSellerId()!=null && seckillGoods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillGoods.getSellerId()+"%");
			}
			if(seckillGoods.getStatus()!=null && seckillGoods.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillGoods.getStatus()+"%");
			}
			if(seckillGoods.getIntroduction()!=null && seckillGoods.getIntroduction().length()>0){
				criteria.andIntroductionLike("%"+seckillGoods.getIntroduction()+"%");
			}
	
		}
		
		Page<TbSeckillGoods> page= (Page<TbSeckillGoods>)seckillGoodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
		
		/**
		 * 查询数据库中的秒杀商品的信息;
		 */
		@Override
		public List<TbSeckillGoods> findList() {
			//先从redis中进行查询;
			List<TbSeckillGoods> seckillGoodsList  = redisTemplate.boundHashOps("seckillGoods").values();
			if (seckillGoodsList==null || seckillGoodsList.size()==0) {
				//根据条件进行查询;审核状态,秒杀时间,库存要大于0;
				TbSeckillGoodsExample example = new TbSeckillGoodsExample();
				Criteria criteria = example.createCriteria();
				criteria.andStatusEqualTo("1");
				criteria.andStockCountGreaterThan(0);
				criteria.andStartTimeLessThanOrEqualTo(new Date());
				criteria.andEndTimeGreaterThanOrEqualTo(new Date());
				seckillGoodsList = seckillGoodsMapper.selectByExample(example);
				//将从数据库中查询出来的数据存储到redis中;
				for (TbSeckillGoods seckillGoods : seckillGoodsList) {
					redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
				}
			}
			return seckillGoodsList;
		}

		/**
		 * 从redis中获取商品的详细信息;
		 * @param id
		 * @return
		 */
		@Override
		public TbSeckillGoods findOneFromRedis(Long id) {
			return (TbSeckillGoods)redisTemplate.boundHashOps("seckillGoods").get(id);
		}
	
}
