/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayOpenPublicMessageCustomSendRequest;
import com.alipay.api.response.AlipayOpenPublicMessageCustomSendResponse;
import com.alipay.common.MyException;
import com.alipay.factory.AlipayAPIClientFactory;
import com.alipay.util.AlipayMsgBuildUtil;

import net.sf.json.JSONObject;

/**
 * 聊天执行器(纯文本消息)
 * 
 * @author baoxing.gbx
 * @version $Id: InAlipayChatExecutor.java, v 0.1 Jul 28, 2014 5:17:04 PM baoxing.gbx Exp $
 */
public class InAlipayChatTextExecutor implements ActionExecutor {

    /** 线程池 */
    private static ExecutorService executors = Executors.newSingleThreadExecutor();
    private AlipayClient alipayClient = null;
    private String APP_ID;

    /** 业务参数 */
    private JSONObject             bizContent;

    public InAlipayChatTextExecutor(JSONObject bizContent, AlipayClient alipayClient, String APP_ID) {
        this.bizContent = bizContent;
        this.alipayClient = alipayClient;
        this.APP_ID = APP_ID;
    }

    public InAlipayChatTextExecutor() {
        super();
    }

    /**
     * 
     * @see com.alipay.executor.ActionExecutor#execute()
     */
    @Override
    public String execute() throws MyException {

        //取得发起请求的支付宝账号id
        final String fromUserId = bizContent.getString("FromUserId");

        //1. 首先同步构建ACK响应
        String syncResponseMsg = AlipayMsgBuildUtil.buildBaseAckMsg(fromUserId, APP_ID);

        //2. 异步发送消息
        executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 2.1 构建一个业务响应消息，商户根据自行业务构建，这里只是一个简单的样例
                    String requestMsg = AlipayMsgBuildUtil.buildSingleImgTextMsg(fromUserId);

                    AlipayOpenPublicMessageCustomSendRequest request = new AlipayOpenPublicMessageCustomSendRequest();
                    request.setBizContent(requestMsg);

                    // 2.2 使用SDK接口类发送响应
                    AlipayOpenPublicMessageCustomSendResponse response = alipayClient
                        .execute(request);

                    // 2.3 商户根据响应结果处理结果
                    //这里只是简单的打印，请商户根据实际情况自行进行处理
                    if (null != response && response.isSuccess()) {

                    } else {
                        throw new Exception("异步发送失败 code=" + response.getCode() + "msg：" + response.getMsg());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 3.返回同步的ACK响应
        return syncResponseMsg;
    }

}
