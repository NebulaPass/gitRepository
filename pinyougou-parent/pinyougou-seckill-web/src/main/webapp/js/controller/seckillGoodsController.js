app.controller('seckillGoodsController',function($scope,$location,$interval,seckillGoodsService){
	
	$scope.findList = function(){
		seckillGoodsService.findList().success(
			function(response){
				$scope.list = response;
			}
		);
	}
	
	$scope.findOne =function(){
		seckillGoodsService.findOne($location.search()['id']).success(
			function(response){
				$scope.entity = response;
				/**
				 * 秒杀倒计时;
				 */
				allsecond = Math.floor((new Date($scope.entity.endTime).getTime()-(new Date().getTime()))/1000);//总秒数;
				time = $interval(function(){
					if(allsecond>0){
						allsecond = allsecond-1;
						$scope.timeString = convertTimeString(allsecond)
					}else{
						$interval.cancel(time);
						alert("秒杀服务已结束!");
					}
				},1000);
			}
		);
	}
	
	/**
	 * 将秒转换为 xx天 xx:xx:xx的格式;
	 */
	convertTimeString = function(allsecond){
		var days = Math.floor(allsecond/(60*60*24));//天数;
		var hours = Math.floor((allsecond-days*60*60*24)/(60*60));//小时数;
		var minutes = Math.floor((allsecond-days*60*60*24-hours*60*60)/60);//分钟数;
		var seconds = allsecond - days*60*60*24-hours*60*60-minutes*60;//秒数;
		var timeString = "";
		if(days > 0){
			timeString = days + "天";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}
	
	/**
	 * 提交订单;
	 */
	$scope.submitOrder = function(){
		location.href="login.html";
		seckillGoodsService.submitOrder($scope.entity.id).success(
			function(response){
				if(response.success){
					alert("下单成功,请快速付款!");
					location.href="pay.html";
				}else{
					alert(response.message);
				}
			}
		);
	}
});