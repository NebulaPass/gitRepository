package com.pinyougou.manager.controller;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
//	@Reference
//	private ItemSearchService itemSearchService;
	
//	@Reference
//	private ItemPageService itemPageService;
	
	@Autowired
	private Destination queueSolrDestination;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private Destination queueSolrDeleteDestination;
	
	@Autowired
	private Destination topicPageDestination;
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbGoods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//将索引库中的数据同步删除;
			//itemSearchService.deleteList(Arrays.asList(ids));
			
			//发送消息;
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					
					return session.createObjectMessage(ids);
				}
			});
			
			jmsTemplate.send(topicPageDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					
					return session.createObjectMessage(ids);
				}
			});
			
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	
	//进行审核;
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){		
		try {
			goodsService.updateStatus(ids, status);
			
			//按照SPU ID查询 SKU列表(状态为1)
			//判断是否审核成功;
			if ("1".equals(status)) {
				//从数据库查询数据;
				List<TbItem> list = goodsService.findItemListByGoodsIdandStatus(ids, status);
				if (list.size()>0) {
					//将查询出来的数据存储到索引库(solr)中;
					//itemSearchService.importList(list);
					
					//将信息上传到activeMQ中;
					final String jsonString = JSON.toJSONString(list);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						
						@Override
						public Message createMessage(Session session) throws JMSException {
							
							return session.createTextMessage(jsonString);
						}
					});
					
					
				}else{
					System.out.println("没有数据");
				}
				
			}
			
			//生成静态页面;
			for (final Long id : ids) {
				//itemPageService.genItemHtml(id);
				jmsTemplate.send(topicPageDestination, new MessageCreator() {
					
					@Override
					public Message createMessage(Session session) throws JMSException {

						return session.createTextMessage(id+"");
					}
				});
			}
			
			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}
	}
	
	/*@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		itemPageService.genItemHtml(goodsId);
	}*/
	
}
