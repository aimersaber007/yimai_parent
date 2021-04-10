app.controller('baseController', function ($scope) {

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

    $scope.jsonToString = function (jsonString, key) {

        var json = JSON.parse(jsonString)

        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ","
            }
            value += json[i][key];

        }
        return value;
    }
})