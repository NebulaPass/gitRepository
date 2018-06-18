//控制层
app.controller('searchController',function($scope,searchService,$location){
	
	$scope.searchMap={keywords:''};
	$scope.resultMap={itemList:[]};
	$scope.search=function(){
		$scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap = response;
				$scope.resultMap.itemList = response.rows;
				$scope.resultMap.categoryList = response.categoryList;
				
				$scope.resultMap.brandList = response.brandList;
				$scope.resultMap.specList = response.specList;
				
				//调用分页方法;
				$scope.buildPageLabel();
			}
		);
	}
	//搜索的对象;
	$scope.searchMap = {'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':30,'sort':'','sortField':''};
	
	//判断页面中的信息是什么;如果是category和brand的话就直接在搜索对象中添加;
	//如果是规格信息的话就将规格名称和规格数据添加到搜索对象中;
	$scope.addSearchItem=function(key,value){
		if(key == "category" || key =="brand" || key=="price"){
			$scope.searchMap[key] = value;
		}else{
			$scope.searchMap.spec[key] = value;
		}
		$scope.search();//执行搜索 
	}
	
	//移除复合搜索条件;
	$scope.removeSearchItem=function(key){
		if(key=="category" ||  key=="brand" || key=="price"){//如果是分类或品牌
			$scope.searchMap[key]="";		
		}else{//否则是规格
			delete $scope.searchMap.spec[key];//移除此属性
		}	
		$scope.search();//执行搜索 
	}
	
	$scope.buildPageLabel = function(){
		$scope.pageLabel=[];//用来封装显示页码数;
		var maxPageNo = $scope.resultMap.totalPage;//最大页码;
		var firstPage = 1;
		var lastPage = maxPageNo;
		
		$scope.firstDot=true;
		$scope.lastDot=true;
		
		if(maxPageNo >= 5){
			if($scope.searchMap.pageNo <= 3){
				lastPage = 5;
				$scope.firstDot=false;
			}else if($scope.searchMap.pageNo >= maxPageNo-2){
				firstPage = maxPageNo-4;
				$scope.lastDot=false;
			}else{
				firstPage = $scope.searchMap.pageNo-2;
				lastPage = $scope.searchMpa.pageNo+2;
			}
		}
		$scope.firstDot=false;
		$scope.lastDot=false;
		//循环产生需要显示的页码;
		for(var i = firstPage; i<=lastPage; i++){
			$scope.pageLabel.push(i);
		}
	}
	
	//根据页码进行查询;
	$scope.queryByPage = function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPage){
			return;
		}
		//对pageNo进行赋值;
		$scope.searchMap.pageNo = pageNo;
		$scope.search();
	}
	
	//设置排序规则;
	$scope.sortSearch = function(sortField,sort){
		$scope.sortField = sortField;
		$scope.sort = sort;
		$scope.search();
	}
	
	//判断关键字是否为品牌;
	$scope.keywordsIsBrand = function(){
		for(var i = 0; i<$scope.resultMap.brandList.length;i++){
			if($scope.resultMap.keywords.indexOf($scope.resultMap.brnadList[i].text)>0){
				return true;
			}
		}
		return false;
	}
	
	$scope.loadKeyWords = function(){
		$scope.resultMap.keywords = $location.search()["keywords"];
		$scope.search();
	}
	
});
