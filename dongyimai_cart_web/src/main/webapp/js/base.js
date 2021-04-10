var app = angular.module('dongyimai', []);

//设置过滤器
app.filter('trustHtml',['$sce',function ($sce){
        return function(data){
            //对数据做过滤处理
            return $sce.trustAsHtml(data);
        }
}])