//商品详情页
app.controller('itemController',function ($scope,$http) {
    //实现购买数量+-
    $scope.addNum = function (num) {
        $scope.num = $scope.num+num;
        //为1时重置为1
        if ($scope.num<1){

            $scope.num = 1;
        }
    }

    $scope.specificationItems = {};//用来记录用户选择的规格
    //选择规格
    $scope.selectSpecification = function (name,value) {
        $scope.specificationItems[name] = value;
        //选择规格后点击
        searchSkuItem();
    }
    //判断规格选项是否被选中
    $scope.isSelected = function (name,value) {
        if ($scope.specificationItems[name]==value){
            return true;
        } else {
            return false;
        }
    }
    //加载默认sku
    $scope.loadSku = function () {
        //得到第一个specList
        $scope.sku = skuList[0];
        //转换specList中的规格
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec))
    }

    //匹配sku表
    matchObj = function (map1,map2) {
        for (var k in map1){
            if (map1[k]!=map2[k]){
                return false;
            }
        }

        for (var k in map2){
            if (map2[k]!=map1[k]){
                return false;
            }
        }

        return true;
    }
    //查询sku
    searchSkuItem = function () {
        for (var i = 0;i<skuList.length;i++){
            //把head.html的sku信息和$scope注入的sku信息
            if (matchObj(skuList[i].spec,$scope.specificationItems)){
                $scope.sku = skuList[i];
                return;
            }
        }
        //no Match words
        $scope.sku={id:0,title:'',price:0};
    }
    //添加商品到购物车
    $scope.addToCat = function () {
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
            + $scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(
            function (response) {
                if (response.success){
                    location.href = 'http://localhost:9107/cart.html';
                } else {
                    alert(response.message);
                }
            }
        )
        alert('skuid'+$scope.sku.id);
    }


});