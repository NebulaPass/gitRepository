app.service('cartService',function($http){
	this.findCartList = function(){
		return $http.get("cart/findCartList.do");
	}
	
	this.addGoodsToCartList = function(itemId,num){
		return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
	}
	this.getUsername = function(){
		return $http.get('cart/getUsername.do');
	}
	
	this.findListByUserId = function(){
		return $http.get('address/findListByUserId.do');
	}
	this.submitOrder = function(order){
		return $http.post('order/add.do',order)
	}
	
});