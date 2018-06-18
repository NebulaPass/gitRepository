package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import utils.HttpClient;
import utils.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public void submitOrder(Long seckillId, String userId) {
			//根据seckillId从redis中获取商品信息;
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			//判断redis中是否存在该商品;
			if (seckillId == null) {
				throw new RuntimeException("商品不存在!");
			}
			if (seckillGoods.getStockCount() <= 0) {
				throw new RuntimeException("商品已被强光!!");
			}
			//将redis中的商品库存数量进行减一;
			seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
			//将修改以后的数据存储到数据库中;
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
			
			/**
			 * 判断减完库存以后的商品库存数,是否为0;
			 */
			if (seckillGoods.getStockCount() == 0) {
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//同步到数据库	
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);	
			}
			//保存订单;
			long orderId = idWorker.nextId();
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			seckillOrder.setId(orderId);
			seckillOrder.setCreateTime(new Date());
			seckillOrder.setMoney(seckillGoods.getCostPrice());
			seckillOrder.setSellerId(seckillGoods.getSellerId());
			seckillOrder.setUserId(userId);
			seckillOrder.setStatus("0");
			redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrder);
		}

		@Override
		public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
			
			return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			
		}

		@Override
		public void saveOrderFromRedisToDB(String userId, Long orderId, String transactionId) {
			//根据用户id查询redis中存储的订单信息;
			TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			//根据查询出来的信息;,判断订单是否存在;
			if (seckillOrder == null) {
				throw new RuntimeException("订单不存在!");
			}
			//如果与传过来的订单id不符合;
			if (seckillOrder.getId().longValue() != orderId.longValue() ) {
				throw new RuntimeException("订单不相符!");
			}
			//补全数据;
			seckillOrder.setTransactionId(transactionId);
			seckillOrder.setPayTime(new Date());
			seckillOrder.setStatus("1");
			//将订单数据存储到数据库中;
			seckillOrderMapper.insert(seckillOrder);
			//将redis中的数据进行清空;
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
		}

		@Override
		public void deleteOrderFromRedis(String userId, Long orderId) {
			//根据用户id查询redis中的订单详情;
			TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
				redisTemplate.boundHashOps("seckillOrder").delete(userId);//删除缓存中的订单;
				
				/**
				 * 回复库存;
				 */
				//1.从缓存中提取秒杀商品;
				TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
				if (seckillGoods != null) {
					seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
				}
			}
		}
}
