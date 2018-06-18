package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.conn.util.PublicSuffixList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbOrderExample.Criteria;

import entity.Cart;
import entity.PageResult;
import utils.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//传进来的TbOrder只是用来进行传递信息的;相当于一个QueryVo;
		
		/**
		 * 在生成商家订单的时候同时生成日志订单;
		 */
		//订单id列表;
		List<String> orderIdList = new ArrayList<String>();
		double total_money = 0;//总金额;
		
		//根据用户id从redis中查询购物车信息;
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		for (Cart cart : cartList) {
			/**
			 * 大购物车中包含商家购物车的信息;
			 * 遍历大购物车信息,得到商家的信息;将商家id存储到订单中;
			 * 商家购物订单中的信息应该包括,商家id和用户的地址信息,商品信息;
			 */
			long orderId = idWorker.nextId();
			System.out.println("sellerId:"+cart.getSellerId());
			TbOrder tbOrder = new TbOrder();//创建新的商家订单;
			tbOrder.setOrderId(orderId);
			tbOrder.setUserId(order.getUserId());
			tbOrder.setPaymentType(order.getPaymentType());
			tbOrder.setStatus("1");//设置状态;未付款;
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(new Date());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiver(order.getReceiver());
			tbOrder.setSourceType(order.getSourceType());
			tbOrder.setSellerId(cart.getSellerId());
			
			//循环购物车明细;
			double money = 0;
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);
				orderItem.setSellerId(cart.getSellerId());
				money += orderItem.getTotalFee().doubleValue();
				orderItemMapper.insert(orderItem);
			}
			tbOrder.setPayment(new BigDecimal(money));
			orderMapper.insert(tbOrder);
			
			//将商家订单id添加到paylog日志中的订单列表中;
			orderIdList.add(orderId+"");
			//获取所用商家订单的金额总和;
			total_money += money;
			System.out.println(total_money);
		}
		if ("1".equals(order.getPaymentType())) {//如果是微信支付;
			TbPayLog payLog = new TbPayLog();
			String outTradeNo = idWorker.nextId()+"";//支付订单号;
			payLog.setOutTradeNo(outTradeNo);
			payLog.setCreateTime(new Date());
			//将订单号列表转换为字符串;
			String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
			payLog.setOrderList(ids);
			payLog.setPayType("1");
			payLog.setTotalFee((long)(total_money*100));
			payLog.setTradeState("0");
			payLog.setUserId(order.getUserId());
			//将数据插入到支付日志表;
			payLogMapper.insert(payLog);
			//将数据放到缓存中;
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
		}
		//从redis中删除购物车信息;
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public TbPayLog searchPayLogFromRedis(String userId) {
			
			return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
		}

		@Override
		public void updateOrderStatus(String out_trade_no, String transaction_id) {
			//修改支付日志状态;
			TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
			payLog.setPayTime(new Date());
			payLog.setTradeState("1");
			payLog.setTransactionId(transaction_id);
			payLogMapper.updateByPrimaryKey(payLog);
			//修改订单状态;
			String orderList = payLog.getOrderList();
			String [] orderIds = orderList.split(",");
			for (String orderId : orderIds) {
				TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
				if (order != null) {
					order.setStatus("2");
					orderMapper.updateByPrimaryKey(order);
				}
			}
			//清除缓存;
			redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
		}
	
}
