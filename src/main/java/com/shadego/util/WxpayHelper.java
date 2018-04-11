package com.shadego.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.shadego.base.utils.network.RetrofitFactory;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class WxpayHelper {
private static final Logger paylog = LoggerFactory.getLogger("paylog");
    
    private String domain;
    private String appId;
    private String mchId;
    private String appKey;
    private String notifuUrl;
    public static final String PACKAGE = "Sign=WXPay"; // 扩展字段 暂填写固定值Sign=WXPay
    public static final String MEDIA_TYPE="Content-Type:application/xml;charset:utf-8";
    
    /**
     * 将签名后的结果传回APP端
     * @param obj
     * @return
     * @throws Exception
     */
    public Map<String, String> createAndroidOrder(Object obj) throws Exception {
        Map<String, String> map=new HashMap<>();
        String orderInfo=createAndroidModel(obj);
        RetrofitFactory.getInstance().getRequestServices()
        .postOriginal("https://"+WXPayConstants.DOMAIN_API+WXPayConstants.UNIFIEDORDER_URL_SUFFIX,RequestBody.create(MediaType.parse(MEDIA_TYPE), orderInfo))
        .subscribe(result->{
            //TODO:验证结果以及记录日志
            Map<String, String> resultXML = WXPayUtil.xmlToMap(result.string());
            map.put("appid",appId);
            map.put("partnerid", mchId);
            map.put("prepayid", resultXML.get("prepay_id").toString());
            map.put("packageValue", PACKAGE);
            map.put("timeStamp", String.valueOf(WXPayUtil.getCurrentTimestamp()));
            map.put("nonceStr", resultXML.get("nonce_str"));
            String sign=WXPayUtil.generateSignature(map,appKey);
            map.put("sign",sign);
        },throwable->{
            paylog.error("创建订单失败",throwable);
        });
        return map;
    }
    
    /**
     * 将二维码地址返回给前台并生成二维码
     * @param obj
     * @return
     * @throws Exception 
     */
    public String createWebOrder(Object obj) throws Exception {
        List<String> list=new ArrayList<>();
        String orderInfo=createWebModel(obj);
        RetrofitFactory.getInstance().getRequestServices()
        .postOriginal("https://"+WXPayConstants.DOMAIN_API+WXPayConstants.UNIFIEDORDER_URL_SUFFIX,RequestBody.create(MediaType.parse(MEDIA_TYPE), orderInfo))
        .subscribe(result->{
          //TODO:验证结果以及记录日志
            Map<String, String> resultXML = WXPayUtil.xmlToMap(result.string());
            list.add(resultXML.get("code_url"));
        },throwable->{
            paylog.error("创建订单失败",throwable);
        });
        return list.get(0);
    }
    
    private String createAndroidModel(Object obj) throws Exception {
        Map<String,String> map=new HashMap<>();
        map.put("appid", appId);// 获取appid参数
        map.put("mch_id", mchId);// 微信支付分配的商户号
        map.put("nonce_str", WXPayUtil.generateUUID());// 获取随机字符串
        //map.put("body", "");// 商品或支付单简要描述
        //map.put("detail", "");// 商品详情
        //主要传送订单id
        //map.put("attach", "");// 附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据
        //map.put("out_trade_no", "");// 户系统内部的订单号,32个字符内、可包含字母
        map.put("fee_type", "CNY");//货币类型  默认人民币：CNY
        //map.put("total_fee", "");//订单总金额 订单金额;
        //map.put("spbill_create_ip","");// APP和网页支付提交用户端ip
        map.put("notify_url", domain+notifuUrl);// 接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
        map.put("trade_type", "APP");//支付类型 取值如下：JSAPI，NATIVE，APP
        return WXPayUtil.generateSignedXml(map,appKey);
    }
    
    private String createWebModel(Object obj) throws Exception {
        Map<String,String> map=new HashMap<>();
        map.put("appid", appId);// 获取appid参数
        map.put("mch_id", mchId);// 微信支付分配的商户号
        map.put("device_info", "WEB");//终端设备号(门店号或收银设备ID)，默认请传"WEB"
        map.put("nonce_str", WXPayUtil.generateUUID());// 获取随机字符串
        //map.put("body", "");// 商品或支付单简要描述
        //map.put("detail", "");// 商品详情
        //主要传送订单id
        //map.put("attach", "");// 附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据
        //map.put("out_trade_no", "");// 户系统内部的订单号,32个字符内、可包含字母
        map.put("fee_type", "CNY");//货币类型  默认人民币：CNY
        //map.put("total_fee", "");//订单总金额 订单金额;
        //map.put("spbill_create_ip","");// APP和网页支付提交用户端ip
        map.put("notify_url", domain+notifuUrl);// 接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
        map.put("trade_type", "NATIVE");//支付类型 取值如下：JSAPI，NATIVE，APP
        return WXPayUtil.generateSignedXml(map,appKey);
    }
}
