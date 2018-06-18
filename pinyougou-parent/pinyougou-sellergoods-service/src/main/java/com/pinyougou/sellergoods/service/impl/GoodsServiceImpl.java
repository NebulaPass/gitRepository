package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");
		//存储tbgoods;
		goodsMapper.insert(goods.getGoods());
		//存储goodsDesc;
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());
	}
	
	public void insertItems(Goods goods){
		if("1".equals(goods.getGoods().getIsEnableSpec())){//启用规格
			//3.添加SKU列表
			for(TbItem item : goods.getItemList()){
				//标题
				String title= goods.getGoods().getGoodsName();
				Map<String, Object> specMap=JSON.parseObject(item.getSpec());
				for(String key:specMap.keySet()){
					title+=" "+specMap.get(key);
				}
				item.setTitle(title);
				setItem(item,goods);
				
				itemMapper.insert(item);	
			}
		}else{//不启用规格，SKU只有一条
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//将SPU名称作为SKU名称
			item.setPrice(goods.getGoods().getPrice());//将SPU的价格作为SKU的价格
			item.setNum(99999);
			item.setStatus("1");
			item.setIsDefault("1");
			setItem(item,goods);
			itemMapper.insert(item);
		}
	}
	
	@Override
	public void update(Goods goods) {
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//1删除相关联的SKU列表
		TbItemExample example=new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		//2重新关联页面提交的SKU列表
		insertItems(goods);
	}
	
	
	//设置SKU部分属性
	private void setItem(TbItem item,Goods goods){
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		item.setGoodsId(goods.getGoods().getId());//当前SKU是属于哪个SPU的
		item.setSellerId(goods.getGoods().getSellerId());//当前SKU是属于哪个商家的
		
		//设置分类
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//设置品牌
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//设置商家
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		
		//获取商品图片列表，将第一个图片的URL设置到item中
		List<Map> imageList=JSON.parseArray(goods.getGoodsDesc().getItemImages(),Map.class);
		item.setImage((String)imageList.get(0).get("url"));
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbGoods goods){
		goodsMapper.updateByPrimaryKey(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		//查询goods表;
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> items = itemMapper.selectByExample(example );
		return new Goods(tbGoods,tbGoodsDesc,items);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}			
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
			criteria.andIsDeleteIsNull();//非删除状态
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public void updateStatus(Long[] ids, String status) {
			for(Long id:ids){
				TbGoods goods = goodsMapper.selectByPrimaryKey(id);
				goods.setAuditStatus(status);
				goodsMapper.updateByPrimaryKey(goods);
			}
		}
		
		
		//根据goodsid和status从数据库中查询数据;
		@Override
		public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
			TbItemExample example = new TbItemExample();
			com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdIn(Arrays.asList(goodsIds));
			criteria.andStatusEqualTo(status);
			List<TbItem> list = itemMapper.selectByExample(example );
			
			return list;
		}
}
