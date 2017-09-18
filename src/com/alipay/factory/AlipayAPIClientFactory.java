/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.factory;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.constants.AlipayServiceEnvConstants;


/**
 * API调用客户端工厂
 * 
 * @author taixu.zqq
 * @version $Id: AlipayAPIClientFactory.java, v 0.1 2014年7月23日 下午5:07:45 taixu.zqq Exp $
 */
public class AlipayAPIClientFactory {

    /** API调用客户端 */
    private static AlipayClient alipayClient;

    /**
     * 获得API调用客户端
     * @param alipayGateway 阿里网关
     * @param appId 应用appid
     * @param privateKey 应用私钥
     * @param format 请求字符格式
     * @param charset 字符编码
     * @param alipayPublicKey 阿里应用公钥
     * @param signType 前面类型
     * @return AlipayClient 对象
     */
    public static AlipayClient getAlipayClient(String alipayGateway, String appId, String privateKey, String format, String charset, String alipayPublicKey, String signType){
        if (format == null || format.trim().length() == 0)
            format = "json";
        if(null == alipayClient){
            alipayClient = new DefaultAlipayClient(alipayGateway, appId, privateKey, format, charset, alipayPublicKey, signType);
        }
        return alipayClient;
    }
}
