app.controller('cartController',function($scope,$controller,cartService){
	
	$controller('addressController',{$scope:$scope});//继承
	
	/**
	 * 查询购物车信息列表;
	 */
	$scope.findCartList = function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList = response;
				$scope.totalValue=sum($scope.cartList);
			}
		);
	}
	
	$scope.addGoodsToCartList = function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
			function(response){
				if(response.success){
					$scope.findCartList();
				}else{
					alert(response.message);
				}
			}
		);
	}
	
	sum=function(cartList){
		var totalValue = {totalNum:0,totalMoney:0.0};
		for(var i =0;i<cartList.length;i++){
			var cart = cartList[i];
			var orderItemList = cart.orderItemList;
			for(var j = 0;j<cart.orderItemList.length;j++){
				var orderItem = orderItemList[j];
				
				totalValue.totalNum += orderItem.num;
				totalValue.totalMoney += orderItem.totalFee;
			}
		}
		return totalValue;
	}
	
	$scope.getUsername = function(){
		cartService.getUsername().success(
			function(response){
				$scope.userName = response;
			}
		);
	}
	
	/**
	 * 查询用户的地址信息;
	 */
	$scope.findListByUserId = function(){
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
	}
	
	/**
	 * 选择地址;
	 */
	$scope.selectAddress = function(address){
		$scope.address = address;
	}
	/**
	 * 判断当前地址是否选中;
	 */
	$scope.isSelectedAddress = function(address){
		//这里边的address是什么???应该是一个对象;但是对象能够直接比较吗???
		if($scope.address == address){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 选择付款方式;
	 */
	$scope.order={paymentType:'1'}
	$scope.selectPayType = function(type){
		$scope.order.paymentType = type;
	}
	
	/**
	 * 保存订单;
	 */
	$scope.submitOrder = function(){
		//将选中的address信息放到order中;
		$scope.order.receiverAreaName = $scope.address.address;
		$scope.order.receiverMobile = $scope.address.mobile;
		$scope.order.receiver = $scope.address.contact;
		
		cartService.submitOrder($scope.order).success(
			function(response){
				if(response.success){
					if($scope.order.paymentType == '1'){
						location.href="pay.html";
					}else{
						location.href="paysuccess.html";
					}
				}else{
					alert(response.message);
				}
			}
		);
		
	}
	
});