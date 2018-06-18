package com.pinyougou.pay.service;

import java.util.Map;

/**
 * 微信支付接口;
 * @author Nebula
 *
 */
public interface WeixinPayService {

	/**
	 * 生成微信支付二维码;
	 * 需要订单id和支付金额;
	 * 根据paylog表中的id查询paylog表中的数据,totalfee也应该是paylog表中的数据;
	 */
	public Map createNative(String out_trade_no,String total_fee);
	
	
	/**
	 * 查询订单的支付状态;
	 */
	public Map queryPayStatus(String out_trade_no);
	
	/**
	 * 关闭支付;
	 */
	public Map closePay(String out_trade_no);
}
