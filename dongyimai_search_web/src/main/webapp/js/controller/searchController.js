app.controller('searchController',function($scope, $location,searchService){
    //搜索
    $scope.search=function(){
        //执行页码查询搜索转int
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);

        searchService.search( $scope.searchMap).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
                //调用分页方法，本类中不加$scope
                buildPageLabel();
            }
        );
    }

    //搜索对象
    $scope.searchMap = {'keywords':'',
        'category':'',
        'brand':'',
        'spec':{},
        'price':'',
        'pageNo':1,
        'pageSize':20,
        'sortField':'',
        'sort':''
    };
    //判断点击的是分类还是品牌
    $scope.addSearchItem = function (key,value) {


        if (key=='category'||key=='brand'||key=='price'){
            $scope.searchMap[key] = value;
        }else{
            $scope.searchMap.spec[key] = value;
        }
        //重置当前页为首页
        $scope.searchMap.pageNo=1;

        $scope.search();
    }

    $scope.removeSearchItem = function (key) {
        //如果是分类或者品牌
        if (key=="category"|| key=='brand'||key=='price'){
            $scope.searchMap[key]="";
        } else{
            //如果是规格
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

    buildPageLabel = function () {
        //构建分页栏属性
        $scope.pageLabel=[];
        var maxPageNo = $scope.resultMap.totalPages;//得到总页码
        var firstPage = 1;
        var lastPage = maxPageNo;

        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后面有点
        if (maxPageNo>5){
            if ($scope.searchMap.pageNo<=3){
                lastPage=5;
                $scope.firstDot=false;
            } else if ($scope.searchMap.pageNo>=lastPage-2){
                firstPage = maxPageNo-4;
                $scope.lastDot=false;//后面没点
            } else {
                firstPage = $scope.searchMap.pageNo-2;
                lastPage = $scope.searchMap.pageSize+2;
            }
        }else {
            $scope.firstDot = false;//前面有点
            $scope.lastDot = false;//后面有点
        }
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    //根据页码进行查询
    $scope.queryByPage = function (pageNo) {
        //页码验证，页码为负不通过，页码大于总页码不通过
        if (pageNo<1 || pageNo > $scope.resultMap.totalPages){
            return;
        }
        //把当前页传给前端调用
        $scope.searchMap.pageNo = pageNo;
        //调用查询方法
        $scope.search();
    }

    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo==1){
            return true;
        } else {
            return false;
        }
    }

    $scope.resultMap = {"totalPages":1};

    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        } else {
            return false;
        }
    }

    $scope.isPage = function (p) {
        if (parseInt(p)==parseInt($scope.searchMap.pageNo)){
            return true;
        } else {
            return false;
        }
    }
    //设置排序规格
    $scope.sortSearch = function (sortField,sort) {

        $scope.searchMap.sortField = sortField;

        $scope.searchMap.sort = sort;

        $scope.search();
    }

    //判断关键字是不是品牌
    $scope.keywordsIsBrand = function () {
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                //如果包含
                return true;
            }
        }
        return false;
    }

    $scope.loadKeywords = function () {
        //地址路由传值
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }

});
