app.controller('contentController',function ($scope,contentService) {
    //设置广告集合
    $scope.contentList = [];

    $scope.findContentList =function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId] = response;
            }
        )
    }

    //搜索跳转
    $scope.search = function () {

        location.href = "http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});