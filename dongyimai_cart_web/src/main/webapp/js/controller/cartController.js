app.controller('cartController',function ($scope,cartService){

    $scope.findCartList = function (){
        cartService.findCartList().success(
            function(response){
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);
        })
    }

    $scope.addGoodsToCartList = function (itemId,num){
        cartService.addGoodsToCartList(itemId,num).success(
            function(response){
            if(response.success){
                //刷新购物车列表
                $scope.findCartList();
            }else{
                alert(resonse.message);
            }
        })
    }

    $scope.findListByUserId = function (){
        cartService.findListByUserId().success(function(response){
            $scope.addressList = response;
            //默认地址选中操作
            for(var i=0;i<$scope.addressList.length;i++){
                if($scope.addressList[i].isDefault=='1'){
                    $scope.address = $scope.addressList[i];
                    break;
                }
            }


        })
    }

    $scope.selectAddress = function (address){
        $scope.address = address;
    }
    $scope.isSelectAddress = function (address){
        if($scope.address == address){
            return true;
        }else{
            return false;
        }
    }


    $scope.order = {paymentType:'1'};
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    $scope.submitOrder=function(){
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder( $scope.order ).success(
            function(response){
                if(response.success){
                    //页面跳转
                    if($scope.order.paymentType=='1'){//如果是扫码支付，跳转到支付页面
                        location.href="pay.html";
                    }else{//如果货到付款，跳转到提示页面
                        location.href="paysuccess.html";
                    }
                }else{
                    alert(response.message);	//也可以跳转到提示页面
                }
            }
        );
    }



})