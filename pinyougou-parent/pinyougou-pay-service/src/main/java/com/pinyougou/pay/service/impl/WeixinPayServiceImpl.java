package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import utils.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService{
	
	@Value("${appid}")
	private String appid;
	
	@Value("${partner}")
	private String partner;
	
	@Value("${partnerkey}")
	private String partnerkey;

	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		//1.创建参数;
		Map <String,String> param = new HashMap<>();//创建参数;
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("nonce_str", WXPayUtil.generateNonceStr());//生成随机字符串;
		param.put("body", "pinyougou");
		param.put("out_trade_no", out_trade_no);
		param.put("total_fee",total_fee);
		param.put("spbill_create_ip", "127.0.0.1");
		param.put("notify_url", "http://test.itcast.cm");
		param.put("trade_type", "NATIVE");
		
		try {
			//2.生成要发送的xml;
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);//安全请求;
			client.setXmlParam(xmlParam);
			client.post();
			//3.获取结果;
			String result = client.getContent();
			System.out.println(result);
			Map<String , String > resultMap = WXPayUtil.xmlToMap(result);
			Map<String , String> map = new HashMap<>();
			map.put("code_url", resultMap.get("code_url"));
			map.put("total_fee", total_fee);
			map.put("out_trade_no", out_trade_no);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	@Override
	public Map queryPayStatus(String out_trade_no) {
		
		//设置请求参数;
		Map param = new HashMap<>();
		param.put("appid", appid);//公众账号id;
		param.put("mch_id", partner);//商户号;
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());//获取随机字符串;
		String url = "https://api.mch.weixin.qq.com/pay/orderquery";
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client = new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Map closePay(String out_trade_no) {
		Map param = new HashMap<>();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());//生成随机字符串;
		String url = "https://api.mch.weixin.qq.com/pay/closeorder";
		
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client=new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
