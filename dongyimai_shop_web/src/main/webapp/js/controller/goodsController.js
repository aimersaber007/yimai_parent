 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location ,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
        var id = $location.search()['id'];
        if (id==null){
            return;
        }
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//向富文本编辑器中添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//显示图片列表
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				//现实商品扩展属性
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格  将非结构化数据转换为json数据
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

				//sku列表规格列表转换
				for (var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);
	}

    //根据规格名称和选项名称返回是否被勾选
    $scope.checkAttribute = function (specName,optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items,'attributeName',specName);

        if (object==null){
            return false;
        } else{
            if (object.attributeValue.indexOf(optionName)>=0){
                return true;
            } else {
                return false;
            }
        }
    }
	
	//保存 
	$scope.save=function(){

		$scope.entity.goodsDesc.introduction = editor.html();

		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	//$scope.reloadList();//重新加载
					location.href = "goods.html";
					alert('保存成功')
					$scope.entity={};
                    editor.html('')
				}else{
					alert(response.message);
				}
			}		
		);				
	}

	/*$scope.add = function(){

		$scope.entity.goodsDesc.introduction = editor.html();

		goodsService.add($scope.entity).success(

			function (response) {
				if (response.success){
					alert("保存成功");
					//清空表单数据
					//$scope.entity = {};

					$scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}}
					//清空富文本编辑器
					editor.html('');
				} else {
					alert(response.message);
				}
            }
		)
	}*/

	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//文件上传
	$scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
            if (response.success){
                $scope.image_entity.url = response.message;
            } else{
                alert(response.message)
            }
        }).error(function () {
            alert("上传c错误");
        })
    }

    //定义页面实体结构
    $scope.entity = {goods:{},goodsDesc:{itemImages:[]}};

	//添加图片
	$scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //删除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }


    //一级分类
    $scope.selectItemCat1List = function () {
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List = response;
            }
		)
    }
    //二级分类
    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
		if(newValue){
			itemCatService.findByParentId(newValue).success(
				function (response) {
					$scope.itemCat2List = response;
                }
			)
		}
    })

	//三级分类
	$scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
		//判断二级目录有无具体分类，获取三级分类
		if(newValue) {
			itemCatService.findByParentId(newValue).success(
				function (response) {
					$scope.itemCat3List = response;
                }
			)
		}
    })

	//读取模板id
	$scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
		if (newValue){
			itemCatService.findOne(newValue).success(
				function (response) {
					$scope.entity.goods.typeTemplateId = response.typeId;
                }
			)
		}
    })
	//更新品牌列表
	$scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
		if(newValue){
			typeTemplateService.findOne(newValue).success(
				function (response) {
					//获取类型模块entity
					$scope.typeTemplate = response;
					//品牌列表
					$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);


					//判断是否有id，没有则加载模板中的扩展数据
					if($location.search()['id']==null){
                        //添加扩展属性
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
					}

                }
			)
			typeTemplateService.findSpecList(newValue).success(
				function (response) {
					$scope.specList = response;
                }
			)
		}
    })

	//初始化
	$scope.entity = {goodsDesc:{itemImages:[],specificationItems:[]}};

	$scope.updateSpecAttribute = function ($event,name,value) {
		//搜索规格选项，查看指定规格是否存在，name规格名称 value 规格选项
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		//如果规格存在
		if(null!=object){
			//判断复选框选中状态
			if($event.target.checked){
				//吧对应的规格选项值插入到数组中
				object.attributeValue.push(value);
			}else{
				//取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value),1)
				//全部取消，将此条记录移除
				if(object.attributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else{
			//首次选中规格，添加数值
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
    }

    //创建sku列表
	$scope.createItemList = function () {
		//spec存储sku对应的规格
		$scope.entity.itemList = [{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];
		//定义变量items指向用户选中规格集合
		var items = $scope.entity.goodsDesc.specificationItems;
		//遍历用户选中的规格集合
		for(var i=0;i<items.length;i++){
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
    }
    //添加列值
    addColumn = function (list,attributeName,attributeValue) {
		var newList = [];//定义一个新的集合
		//遍历sku规格选项
		for(var i=0;i<list.length;i++){
			//赋值
			var oldRow = list[i];
			//遍历规格选项
			for(var j=0;j<attributeValue.length;j++){
                //深克隆当前行sku数据为newRow
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
				//在新行扩展列（列名是规格名）给列赋值（规格选项值）
				newRow.spec[attributeName]=attributeValue[j];
				//保存newskurow in newskuList
				newList.push(newRow);
			}
		}
		return newList;
    }


    $scope.itemCatList = [];

	$scope.findItemCatList = function () {
		itemCatService.findAll().success(
			function (response) {
				for (var i=0;i<response.length;i++){
					$scope.itemCatList[response[i].id] = response[i].name;

				}
            }
		)
    }

});	