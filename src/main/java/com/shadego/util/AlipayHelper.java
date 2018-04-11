package com.shadego.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;

public class AlipayHelper {
    private static final Logger paylog = LoggerFactory.getLogger("paylog");
    
    private String domain;
    private String appId;
    private String privateKey;
    private String aliPublicKey;
    private String notifuUrl;
    private String orderUrl;
    
    public static final String CHARSET="utf-8";
    public static final String SIGN_TYPE="RSA2";
    
    /**
     * 字符串记录日志后直接返回给APP端
     * @param obj
     * @return
     */
    public String createAndroidOrder(Object obj) {
        String orderStr = null;
        AlipayClient alipayClient = new DefaultAlipayClient(orderUrl, appId,privateKey, "json", CHARSET, aliPublicKey, SIGN_TYPE);
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setBizModel(createModel(obj));
        request.setNotifyUrl(domain+notifuUrl);
        try {
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            orderStr = response.getBody();
        } catch (AlipayApiException e) {
            paylog.error("SDK请求失败",e);
        }
        return orderStr;
    }
    
    /**
     * Controller层
     * httpResponse.setContentType("text/html;charset=" + CHARSET);
    httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
    httpResponse.getWriter().flush();
    httpResponse.getWriter().close();
     * @param obj
     * @return
     */
    public String createWebOrder(Object obj) {
        String orderStr = null;
        AlipayClient alipayClient = new DefaultAlipayClient(orderUrl, appId,privateKey, "json", CHARSET, aliPublicKey, SIGN_TYPE);
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setBizModel(createModel(obj));
        try {
            AlipayTradeAppPayResponse response = alipayClient.pageExecute(request);
            orderStr = response.getBody();
        } catch (AlipayApiException e) {
            paylog.error("SDK请求失败",e);
        }
        return orderStr;
    }
    
    private AlipayTradeAppPayModel createModel(Object obj) {
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        //model.setBody("orderName");
        //model.setSubject("sub");
        //model.setOutTradeNo("tradeNo");
        //model.setTotalAmount("price");
        model.setProductCode("QUICK_MSECURITY_PAY");// 固定值
        return model;
    }
    
    /**
     * 回调验签
     * @param param
     * @return
     */
    public boolean checkSign(Map<String,String> param) {
        try {
            return AlipaySignature.rsaCheckV1(param, aliPublicKey, CHARSET, SIGN_TYPE);
        } catch (AlipayApiException e) {
            paylog.error("验签异常",e);
            return false;
        }
    }
}
