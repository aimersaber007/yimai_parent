package com.offcn.sms.service.impl;

import com.offcn.util.SmsUtil;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component("smsListener")
public class SmsListener implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Override
    public void onMessage(Message message) {
        if (message instanceof MapMessage){
            MapMessage map = (MapMessage) message;
            try {
                System.out.println("收到短信发送请求mobile："+map.getString("mobile")+"param"+map.getString("param"));

                HttpResponse response = smsUtil.sendSms(map.getString("mobile"),map.getString("param"));
                System.out.println("data"+response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
