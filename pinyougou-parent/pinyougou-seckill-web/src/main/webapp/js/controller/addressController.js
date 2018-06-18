 //控制层 
app.controller('addressController' ,function($scope,$controller   ,addressService,cartService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		addressService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		addressService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		addressService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=addressService.update( $scope.entity ); //修改  
		}else{
			serviceObject=addressService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
					$scope.findListByUserId();
		        	alert(response.message);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(addressId){			
		//获取选中的复选框			
		addressService.dele( addressId ).success(
			function(response){
				if(response.success){
					alert("删除成功!")
					$scope.findListByUserId();
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		addressService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	/*//刷新
	refresh = function(){
		cartService.findListByUserId().success(
				function(response){
					$scope.addressList = response;
					//在查询的时候,设置默认选择的地址;
					for(var i =0;i<$scope.addressList.length;i++){
						if($scope.addressList[i].isDefault == '1'){
							$scope.address = $scope.addressList[i];
						}
					}
				}
		);
	}*/
});	
