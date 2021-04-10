 //商品类目控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			//给上一级id赋值
			$scope.entity.parentId = $scope.parentId;

			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载

					//重新查询
					$scope.findByParentId($scope.parentId);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.findByParentId($scope.parentId);
					//$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.findByParentId = function (parentId) {
		itemCatService.findByParentId(parentId).success(
			function (response) {
				$scope.list = response;
            }
		)
    }
    //设置默认级别为1
    $scope.grade = 1;
	//设置级别
	$scope.setGrade = function (value) {
		$scope.grade = value;
    }
    //设置导航，读取列表
	$scope.selectList = function (p_entity) {
		if($scope.grade==1){
			$scope.entity_1 = null;
            $scope.entity_2 = null;
		}
		if($scope.grade==2){
			$scope.entity_1 = p_entity;
			$scope.entity_2 = null;
		}
        if($scope.grade==3){
            $scope.entity_2 = p_entity;
		}
		//查询此级的下级列表
		$scope.findByParentId(p_entity.id);
    }

    //上级id
    $scope.parentId = 0;

	$scope.findByParentId = function (parentId) {
		//记录上级的iD
		$scope.parentId = parentId;
		itemCatService.findByParentId(parentId).success(
			function (response) {
				$scope.list = response;
            }
		)
    }


    //模块列表
    $scope.typeTemplateList = {data:[]};

    //实现模块下拉列表框
    $scope.findtypeTemplateList = function () {
        typeTemplateService.selectOptionList().success(
            function (response) {
                $scope.typeTemplateList={data:response};
            }
        )
    }
    
});	