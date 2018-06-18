app.controller('indexController',function($scope,loginService){
	
	$scope.findName=function(){
		loginService.loginName().success(
				function(response){
					$scope.loginName=response.loginName;
				}
		)
	}
});