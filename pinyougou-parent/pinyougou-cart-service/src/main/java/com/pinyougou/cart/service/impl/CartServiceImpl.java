package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;

import entity.Cart;

@Service
public class CartServiceImpl implements CartService {
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 向购物车中添加商品信息;
	 */
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//判断商品是否存在;
		
		//System.out.println(itemId);
		TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
		
		if (tbItem==null) {
			throw new RuntimeException("商品不存在!!");
		}
		if (!tbItem.getStatus().equals("1")) {
			throw new RuntimeException("商品状态无效!");
		}
		//根据itemId获取商家Id;
		String sellerId = tbItem.getSellerId();
		String sellerName = tbItem.getSeller();
		//根据商家ID判断购物车列表中是否包含此商品的商家;
		Cart cart = searchCartBySellerId(cartList, sellerId);
		//判断cart是否为空;
		if (cart==null) {
			//购物车列表中不包含此商品的商家;创建此商家的购物车;
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(sellerName);
			//创建订单详情;
			TbOrderItem orderItem = createOrderItem(tbItem, num);
			List<TbOrderItem> orderItemList = new ArrayList<>();
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			//将商家购物车添加到购物车列表;
			cartList.add(cart);
		}else{
			//购物车列表中存才改商家的购物车信息;查询购物车信息;判断是否包含此商品;
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			//判断商家购物车信息中是否包含此商品信息;
			if (orderItem == null) {
				//商家购物车中不包含此商品信息;
				orderItem = createOrderItem(tbItem, num);
				cart.getOrderItemList().add(orderItem);
			}else{
				//商家购物车中包含此商品的信息;更改数量和金额;
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));
				//如果操作后数量小于等于0;则移除此商品信息;
				if (orderItem.getNum()<=0) {
					cart.getOrderItemList().remove(orderItem);
				}
				if (cart.getOrderItemList().size()<=0) {
					cartList.remove(cart);
				}
			}
		}
		return cartList;
	}
	
	/**
	 * 根据商家ID判断购物车列表中是否包含此商品的商家;
	 */
	public Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	
	/**
	 * 创建订单详情;
	 */
	public TbOrderItem createOrderItem(TbItem item,Integer num){
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setItemId(item.getId());
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		return orderItem;
	}
	
	/**
	 * 查询商家购物车中的信息;
	 */
	public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
		for (TbOrderItem orderItem : orderItemList) {
			//封装类型不能直接进行比较;
			if (orderItem.getItemId().longValue() == itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
	}

	@Override
	public List<Cart> findCartFromRedis(String username) {
		
		//从数据库查询购物车信息;
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if (cartList == null) {
			cartList = new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {

		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		
			//遍历cookie中购物车的信息;
			for (Cart cart : cartList2) {
				for (TbOrderItem orderItem : cart.getOrderItemList()) {
					cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
				}
			}
		return cartList1;
	}
}
