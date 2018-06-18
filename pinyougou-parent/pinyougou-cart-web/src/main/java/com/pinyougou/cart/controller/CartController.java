package com.pinyougou.cart.controller;

import java.security.Security;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;

import entity.Cart;
import entity.Result;
import utils.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Reference
	private CartService cartService;
	@Autowired 
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	
	
	/**
	 * 从cookie中查询购物车信息;
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		
		//获取用户名;
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		String cartListString = utils.CookieUtil.getCookieValue(request, "cartList", "utf-8");
		if (cartListString==null || cartListString.equals("")) {
			cartListString = "[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString,Cart.class);
		
		//判断username;
		if (username.equals("anonymousUser")) {
			return cartList_cookie;
		}else{
			//从redis中获取购物车信息;
			List<Cart> cartList_redis = cartService.findCartFromRedis(username);
			//判断cookie中是否有购物车信息;
			if (cartList_cookie.size()>0) {
				//System.out.println(cartList_cookie.size());
				cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
				//清除本地的cookie中的数据;
				//TODO
				utils.CookieUtil.deleteCookie(request, response, "cartList");
			}
			return cartList_redis;
		}
	}
	
	/**
	 * 向购物车中添加商品;
	 */
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId, Integer num){
		
		//允许进行跨域请求;
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		
		
		//获取用户名;
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			List<Cart> cartList = findCartList();
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if (username.equals("anonymousUser")) {
				//未登录;将数据存储到cookie中;
				utils.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList),3600*24,"utf-8");
			}else{
				//从redis中获取购物车信息;
				cartService.saveCartListToRedis(username, cartList);
			}
			return new Result(true, "添加成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败!");
		}
	}
	
	@RequestMapping("/getUsername")
	public Result getUsername(){
		
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		if (userName.equals("anonymousUser")) {
			return new Result(false, "anonymousUser");
		}else{
			return new Result(true, userName);
		}
	}
}
