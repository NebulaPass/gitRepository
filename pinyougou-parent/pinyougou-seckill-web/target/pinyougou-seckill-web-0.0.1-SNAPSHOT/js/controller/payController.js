app.controller('payController',function($scope,payService){
	
	//根据地址生成二维码;
	$scope.createNative = function(){
		payService.createNative().success(
			function(response){
				$scope.money = (response.total_fee/100).toFixed(2);//获取金额;
				$scope.out_trade_no = response.out_trade_no;//获取订单id;
				
				//生成二维码;
				var qr = new QRious({
					element:document.getElementById('qrious'),
					size:250,
					level:'H',
					value:response.code_url
				});
				//查询支付状态;
				//queryPayStatus(response.out_trade_no);
			}
		);
	}
	
	/**
	 * 查询支付状态;
	 */
	queryPayStatus = function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(
			function(response){
				if(response.success){
					location.href="paysuccess.html#?money="+$scope.money;
				}else{
					if(response.message == "二维码超时!"){
						$scope.createNative();//重新生成二维码;
					}else{
						if(response.message=='二维码超时'){
							location.href="payTimeOut.html";	
						}else{
							location.href="payfail.html";
						}	
					}
				}
			}
		);
		
		//获取金额;
		$scope.getMoney = function(){
			return $location.search()['money'];
		}
		
	}
});