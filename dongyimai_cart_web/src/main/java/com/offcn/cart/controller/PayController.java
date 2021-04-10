package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService aliPayService;

    @Autowired
    private IdWorker idWorker;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //从缓存中读取支付日志
        TbPayLog payLog = orderService.findPayLogFromRedis(userId);
        if (payLog != null) {
            return aliPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }
    }


    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        Map map = null;
        int x = 0;
        while (true) {
            try {
                //执行查询状态
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("调用查询服务出错");
            }
            if (map == null) {
                result = new Result(false, "支付异常");
                break;
            }

            if (map.get("trade_status") != null && map.get("trade_status").equals("TRADE_SUCCESS")) {
                result = new Result(true, "支付成功");
                orderService.updateOrderStatus(out_trade_no,(String)map.get("trade_no"));
                break;
            }
            if (map.get("trade_status") != null && map.get("trade_status").equals("TRADE_CLOSED")) {
                result = new Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if (map.get("trade_status") != null && map.get("trade_status").equals("TRADE_FINISHED")) {
                result = new Result(true, "交易结束，不可退款");
                break;
            }

            x++;
            if (x >= 10) {
                result = new Result(true, "二维码超时");
                break;
            }


            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
