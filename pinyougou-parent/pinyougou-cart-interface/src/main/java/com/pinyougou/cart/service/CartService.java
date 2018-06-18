package com.pinyougou.cart.service;

import java.util.List;

import entity.Cart;

public interface CartService {
	/**
	 * 向购物车中添加商品;
	 */
	public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num );
	
	/**
	 * 从redis中查询购物车信息;
	 */
	public List<Cart> findCartFromRedis(String username);
	
	/**
	 * 向redis中存储购物车信息;
	 */
	public void saveCartListToRedis(String username,List<Cart> cartList);
	
	/**
	 * 合并购物车;
	 */
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart>cartList2);
} 
