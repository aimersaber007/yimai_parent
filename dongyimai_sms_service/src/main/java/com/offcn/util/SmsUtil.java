package com.offcn.util;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: lhq
 * @Date: 2020/8/28 10:29
 * @Description: 发送短信工具类
 */
@Component
public class SmsUtil {


    @Value("${appcode}")
    private String appcode;

    @Value("${tpl}")
    private String tpl;

    /**
     * 发送短信
     *
     * @param mobile 手机号
     * @param param  验证码
     * @return
     */
    public HttpResponse sendSms(String mobile, String param) throws Exception {
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", mobile);
        querys.put("param", "code:" + param);
        querys.put("tpl_id", tpl);
        Map<String, String> bodys = new HashMap<String, String>();
        HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
        return response;

    }
}
