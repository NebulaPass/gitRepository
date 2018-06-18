app.controller('brandController', function($scope,$controller, brandService) {
	
	$controller('baseController',{$scope:$scope});
	
	$scope.findAll = function() {
		brandService.findAll().success(function(response) {
			$scope.list = response;
		});
	}

	$scope.findPage = function(currentPage, pageSize) {
		brandService.findPage(currentPage, pageSize).success(
				function(response) {
					$scope.list = response.rows;
					$scope.paginationConf.totalItems = response.total;
				});
	}

	$scope.entity = {};
	$scope.add = function() {
		brandService.add($scope.entity).seccess(function(response) {
			if (response.success) {
				alert(response.message);
				$scope.entity = {};
			} else {
				alert(response.message);
			}
		});
	}

	$scope.findOne = function(id) {
		brandService.findOne(id).success(function(response) {
			$scope.entity = response;
		});
	}

	$scope.update = function() {
		brandService.update($scope.entity).success(function(response) {
			if (response.success) {
				alert(response.message);
				$scope.reloadList();
			} else {
				alert(response.message);
			}
		});
	}

	$scope.save = function() {
		$scope.method = brandService.add($scope.entity);
		if ($scope.entity.id != null) {
			$scope.method = brandService.update($scope.entity);
		}
		$scope.method.success(function(response) {
			if (response.success) {
				alert(response.message);
				$scope.reloadList();
			} else {
				alert(response.message);
			}
		});
	}

	$scope.dele = function() {
		brandService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				alert(response.message);
				$scope.reloadList();
				$scope.selectIds = [];
			} else {
				alert(response.message);
			}
		});
	}

	$scope.searchEntity = {};

	$scope.search = function(currentPage, pageSize) {
		brandService.search(currentPage, pageSize, $scope.searchEntity).success(
				function(response) {
					$scope.list = response.rows;
					$scope.paginationConf.totalItems = response.total;
				});
	}

});