package com.pinyougou.seckill.service;
import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum,int pageSize);
	
	/**
	 * 提交订单的方法,跟据seckillId查询商品信息,生成订单,将订单信息存储到redis中;
	 */
	public void submitOrder(Long seckillId,String userId);
	
	/**
	 * 根据用户名查询redis中的订单信息;
	 */
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId);
	
	/**
	 * 支付成功保存订单;
	 */
	public void saveOrderFromRedisToDB(String userId,Long orderId,String transactionId);
	
	/**
	 * 从缓存中删除订单信息;
	 */
	public void deleteOrderFromRedis(String userId,Long orderId);
	
	
}
