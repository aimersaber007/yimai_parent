package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateConfirmRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        Map resultMap = new HashMap();
        //创建预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        //转换下单金额按照元
        long total = Long.parseLong(total_fee);
        BigDecimal bigDecimal = BigDecimal.valueOf(total);
        BigDecimal cs = BigDecimal.valueOf(100d);
        BigDecimal money = bigDecimal.divide(cs);
        System.out.println("预下单："+money.doubleValue());
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"," + //商户订单号
                "    \"total_amount\":\"" + money.doubleValue() + "\"," +
                "    \"subject\":\"商品01\"," +
                "    \"store_id\":\"NJ_001\"," +
                "    \"timeout_express\":\"90m\"}"); //订单允许的最晚付款时间
        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
            String code = response.getCode();//状态码
            System.out.println("状态码：" + code);
            System.out.print(response.getBody());
            //根据response中的结果继续业务逻辑处理
            if (code != null && code.equals("10000")) {
                resultMap.put("qrCode", response.getQrCode());
                resultMap.put("outTradeNo", response.getOutTradeNo());   //订单交易编号
                resultMap.put("totalFee", total_fee);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no 订单编号
     * @return
     */
    public Map queryPayStatus(String out_trade_no) {
        Map resultMap = new HashMap();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest(); //创建API对应的request类
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"," +
                "    \"trade_no\":\"\"}");  //设置业务参数
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request); //通过alipayClient调用API，获得对应的response类
            System.out.println(response.getBody());
            String code = response.getCode();
            if (code != null & code.equals("10000")) {
                //根据response中的结果继续业务逻辑处理
                resultMap.put("trade_no", response.getTradeNo());//支付宝的流水号
                resultMap.put("out_trade_no", response.getOutTradeNo());//订单编号
                resultMap.put("trade_status", response.getTradeStatus());//订单状态
            }


        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        return resultMap;
    }
}
