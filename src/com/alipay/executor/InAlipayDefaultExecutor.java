/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.alipay.executor;

import com.alipay.common.MyException;
import com.alipay.util.AlipayMsgBuildUtil;
import net.sf.json.JSONObject;

/**
 * 默认执行器(该执行器仅发送ack响应)
 * 
 * @author baoxing.gbx
 * @version $Id: InAlipayDefaultExecutor.java, v 0.1 Jul 30, 2014 10:22:11 AM baoxing.gbx Exp $
 */
public class InAlipayDefaultExecutor implements ActionExecutor {

    /** 业务参数 */
    private JSONObject bizContent;
    private String APP_ID;

    public InAlipayDefaultExecutor(JSONObject bizContent, String APP_ID) {
        this.bizContent = bizContent;
        this.APP_ID = APP_ID;
    }

    public InAlipayDefaultExecutor() {
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

        return AlipayMsgBuildUtil.buildBaseAckMsg(fromUserId, APP_ID);
    }
}
