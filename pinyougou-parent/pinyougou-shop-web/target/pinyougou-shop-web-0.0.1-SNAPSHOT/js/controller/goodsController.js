 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,itemCatService,typeTemplateService,uploadService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){	
		
		var id = $location.search()["id"];
		if(id == null){
			return ;
		}
	
		goodsService.findOne(id).success(
			function(response){
			
				$scope.entity= response;	
				editor.html($scope.entity.goodsDesc.introduction);
				//数据库中存储的是一个json字符串;需要转换为json对象;
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				//显示属性列表;
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//显示规格选项;
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				
				//SKU列表规格列改变;
				for(var i=0;i<response.itemList.length;i++){
					$scope.entity.itemList[i].spec=JSON.parse( response.itemList[i].spec);
				}
			}
		);				
	}
	
	
	$scope.checkAttributeValue = function(specName,optionName){
		var items = $scope.entity.goodsDesc.specificationItems;
		var Object = $scope.judgeObject(items,"attributeName",specName);
		if(Object == null){
			 return false;
		}else{
			if(Object.attributeValue.indexOf(optionName) >= 0){
				return true;
			}else{
				return false;
			}
		}
	}
	
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]}
	//向图片数组中添加图片对象
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	//从数组中移除图片对象
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1)
	}
	
	//保存 
	$scope.save=function(){		
		$scope.entity.goodsDesc.introduction = editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//location.href="goods.html"
						$scope.entity={};
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	//定义状态信息;
	$scope.status = ["未审核","审核通过","审核未通过","关闭"]
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//获取商品分类列表中的信息;
	$scope.itemCatList={};
	$scope.findItemCatList = function(){
		itemCatService.findAll().success(
			function(response){
				
				for (var i = 0; i < response.length; i++) {
					$scope.itemCatList[response[i].id] = response[i].name;
				}
				//alert(JSON.stringify($scope.itemCatList));
			}
		);
	}
	
	//
    
	$scope.findByParentId = function(){
		itemCatService.findByParentId(0).success(
			function(response){
				$scope.itemCat1List = response;
			}
		)
	}
	
	$scope.$watch("entity.goods.category1Id",function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(
			function(response){
				$scope.itemCat2List = response;
				$scope.itemCat3List = [];
			}
		)
	});
	
	$scope.$watch("entity.goods.category2Id",function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(
			function(response){
				$scope.itemCat3List = response;
			}
		)
	});
	
	$scope.$watch("entity.goods.category3Id",function(newValue,oldValue){
		itemCatService.findOne(newValue).success(
			function(response){
				$scope.entity.goods.typeTemplateId = response.typeId;
				
			}
		)
	});
	
	$scope.$watch("entity.goods.typeTemplateId",function(newValue,oldValue){
		typeTemplateService.findOne(newValue).success(
			function(response){
				$scope.typeTemplate = response;//获取模板对象;
				//接收品牌数据;需要用JSON来进行转换;
				$scope.brandList = JSON.parse(response.brandIds);
				//接收扩展属性;这个属性值要存储到goodsDesc中;所以我们用数据表中定义的字段来进行接收;
				//也需要转换为json对象;
				if($location.search()["id"] == null){
					$scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
				}
				$scope.createItemList();
			}
		)
	});
	
	$scope.$watch("entity.goods.typeTemplateId",function(newValue,oldValue){
		typeTemplateService.findSpecList(newValue).success(
			function(response){
				$scope.specList = response;
			}
		)
	});
	//判断选中的规格是否在数组中存在;key:"attributeName",value:规格名字;
	$scope.judgeObject = function(list,key,value){
		for (var i = 0; i < list.length; i++) {
			if(list[i][key] == value){
				return list[i];
			}
		}
		return null;
	}
	
	//向数组中存储相关信息;
	$scope.updateSpecAttribute = function($event,key,value){
		var object = $scope.judgeObject($scope.entity.goodsDesc.specificationItems,"attributeName",key);
		if(object != null){
			if($event.target.checked){
				object.attributeValue.push(value);
			}else{
				//如果未被选中的话,就将点击项删除;
				var index = object.attributeValue.indexOf(value);
				object.attributeValue.splice(index,1);
				if(object.attributeValue.length == 0){
					//如果对应属性中没有值的话就将这个属性删除;
					var index2 = $scope.entity.goodsDesc.specificationItems.indexOf(key);
					$scope.entity.goodsDesc.specificationItems.splice(index2,1);
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":key,"attributeValue":[value]})
		}
	}
	
	
	//创建SKU列表;
	$scope.createItemList = function(){
		$scope.entity.itemList = [{spec:{},price:0,num:9999,status:1,isDefault:0}];
		var specItems = $scope.entity.goodsDesc.specificationItems;
		//遍历specificationItems,规格属性名;
		for (var i = 0; i < specItems.length; i++) {
			$scope.entity.itemList = addColumn($scope.entity.itemList,specItems[i].attributeName,specItems[i].attributeValue);
		}
	}
	
	addColumn = function(list,name,values){
		var newList = [];
		//遍历oldList;
		for (var i = 0; i < list.length; i++) {
			var oldRow = list[i];
			//遍历规格属性选项;
			for (var j = 0; j < values.length; j++) {
				//进行深克隆;
				//每有一个规格属性选项,就新建一个newRow;
				//newList中的数据:[spec{"网络":"3G",price:0,num:9999,status:1,isDefault:0}},spec{"网络":"4G",price:0,num:9999,status:1,isDefault:0}}];
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[name] = values[j];//spec是一个map集合;
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	$scope.image_entity={};//图片对象
	//uploadFile
	$scope.uploadFile=function(){	  
		uploadService.uploadFile().success(function(response) {        	
        	if(response.success){//如果上传成功，取出url
        		$scope.image_entity.url=response.message;//设置文件地址
        	}else{
        		alert(response.message);
        	}
        }).error(function() {           
        	     alert("上传发生错误");
        });        
    };    
	
	
});	
