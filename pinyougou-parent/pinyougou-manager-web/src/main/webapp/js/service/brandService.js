
//服务
app.service('brandService',function($http){
			//查询方法findAll;
			this.findAll = function(){
				return $http.get('../brand/findAll.do');
			}
			
			//分页查询findPage
			this.findPage=function(currentPage,pageSize){
				return $http.get("../brand/findPage.do?currentPage="+currentPage+"&pageSize="+pageSize);
			}
			
			//add
			this.add=function(entity){
				return $http.post("../brand/add.do",entity);
			}
			//findOne
			this.findOne=function(id){
				return $http.get("../brand/findOne.do?id="+id);
			}
			//update
			this.update = function(entity){
				return $http.post("../brand/update.do",entity);
			}
			//dele
			this.dele = function(ids){
				return $http.post("../brand/delete.do?ids="+ids);
			}
			//search
			this.search = function(currentPage,pageSize,entity){
				return $http.post("../brand/search.do?currentPage="+currentPage+"&pageSize="+pageSize,entity);
			}
			
			//selectOptionList
			this.selectOptionList = function(){
				return $http.get("../brand/SelectOptionList.do");
			}
		}); 