app.controller('itemController',function($scope,$http){
	$scope.addNum = function(x){
		$scope.num = parseInt($scope.num);
		$scope.num = $scope.num + x;
		if($scope.num < 1){
			$scope.num = 1;
		}
	}
	
	$scope.specificationItem={};//记录用户的选择规格;
	
	//用户选择规格的方法;
	$scope.selectSpecification=function(name,value){
		$scope.specificationItem[name]=value;
		searchSku();
		//alert(JSON.stringify($scope.specificationItem));
	}
	//判断规格是否被选中;
	$scope.isSelected = function(name,value){
		if($scope.specificationItem[name] == value){
			return true;
		}else{
			return false;
		}
	}
	
	//加载默认sku信息;
	$scope.loadSku=function(){
		$scope.sku = skuList[0];
		//alert(JSON.stringify($scope.sku));
		$scope.specificationItem = JSON.parse(JSON.stringify($scope.sku.spec));
		//alert(JSON.stringify($scope.specificationItem));
	}
	
	//更改规格更新sku;
	matchObject = function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		return true;
	}
	
	//根据规格查询sku
	searchSku = function(){
		
		for(var i =0;i<skuList.length;i++){
			if(matchObject($scope.specificationItem,skuList[i].spec)){
				$scope.sku=skuList[i];
				return;
			}
		}
	}
	
	$scope.addToCart = function(){
		alert("skuId:"+$scope.sku.id);
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
			function(response){
				if(response.success){
					location.href="http://localhost:9107/cart.html";
				}else{
					alert(response.message);
				}
			}
		);
	}
});