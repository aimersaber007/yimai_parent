app.controller('baseController',function ($scope){
    //配置分页组件
    $scope.paginationConf = {
        "currentPage": 1,		//当前页
        "totalItems": 10,       //总记录数
        "itemsPerPage": 10,     //每页显示记录数
        "perPageOptions": [10, 20, 30, 40, 50],   //每页记录数选择器
        onChange: function () {
            //执行分页查询
            $scope.reloadList();
        }

    }

    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //初始化品牌ID的数组
    $scope.selectIds = [];

    //选中/反选
    $scope.updateSelection = function ($event, id) {
        //判断复选框是否选中
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1);   //参数一：元素的下标  参数二：移除的个数
        }
    }

    //将JSON字符串转换成格式化的字符串
    $scope.jsonToString = function(jsonString,key){
        //1.将字符串转换成JSON对象
        //2.初始化格式化的字符串
        var value="";
        var json = JSON.parse(jsonString);
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=",";
            }

            value += json[i][key];
        }
        return value;
    }


    //通过key判断元素在集合中是否存在
    $scope.searchObjectByKey=function(list,key,keyValue){
        for(var i=0;i<list.length;i++){
            //通过键值判断元素是否存在，存在则直接将元素返回
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }
})