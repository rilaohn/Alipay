package com.alipay.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.FileItem;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.StringUtils;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.alipay.dispatcher.Dispatcher;
import com.alipay.domain.AlipayColorValue;
import com.alipay.domain.AlipayImageTextMessage;
import com.alipay.executor.ActionExecutor;
import com.alipay.factory.AlipayAPIClientFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliTool {
	/**
	 * 支付宝公钥-从支付宝生活号详情页面获取
	 */
	private String ALIPAY_PUBLIC_KEY;

	/**
	 * 签名编码-视支付宝服务窗要求
	 */
	private String SIGN_CHARSET = "utf-8";

	/**
	 * 字符编码-传递给支付宝的数据编码
	 */
	private String CHARSET = "utf-8";

	/**
	 * 签名类型-视支付宝服务窗要求
	 */
	private String SIGN_TYPE = "RSA2";

	/**
	 * 开发者账号PID
	 */
	private String PARTNER = "";

	/**
	 * 服务窗appId
	 */
	// 注：该appId必须设为开发者自己的生活号id
	private String APP_ID;

	// 注：该私钥为测试账号私钥  开发者必须设置自己的私钥 , 否则会存在安全隐患
	private String PRIVATE_KEY;

	// 注：该公钥为测试账号公钥  开发者必须设置自己的公钥 ,否则会存在安全隐患
	private String PUBLIC_KEY;

	/**
	 * 支付宝网关
	 */
	private String ALIPAY_GATEWAY = "https://openapi.alipay.com/gateway.do";

	/**
	 * 授权访问令牌的授权类型
	 */
	private String GRANT_TYPE = "authorization_code";

	private Map<String, AlipayClient> clientMap;

	private Gson googleGson;

	/**
	 * 构造函数
	 *
	 * @param appId            应用的appid
	 * @param privateKey       应用的私钥
	 * @param publicKey        应用的公钥
	 * @param alipayPublickkey 应用的阿里公钥
	 */
	public AliTool(String appId, String privateKey, String publicKey, String alipayPublickkey) {
		this.APP_ID = appId;
		this.PRIVATE_KEY = privateKey;
		this.PUBLIC_KEY = publicKey;
		this.ALIPAY_PUBLIC_KEY = alipayPublickkey;
		this.clientMap = new HashMap<>();
		this.googleGson = null;
	}

	/**
	 * 构造函数
	 *
	 * @param appId            应用的appId
	 * @param privateKey       应用的私钥
	 * @param publicKey        应用的公钥
	 * @param alipayPublickkey 应用的阿里公钥
	 * @param partner          开发者的PID
	 */
	public AliTool(String appId, String privateKey, String publicKey, String alipayPublickkey, String partner) {
		this(appId, privateKey, publicKey, alipayPublickkey);
		if (partner != null && partner.trim().length() > 0)
			this.PARTNER = partner.trim();
	}

	/**
	 * 构造函数
	 *
	 * @param appId            应用的appId
	 * @param privateKey       应用的私钥
	 * @param publicKey        应用的公钥
	 * @param alipayPublickkey 应用的阿里公钥
	 * @param partner          开发者的PID
	 * @param signCharset      签名编码 默认：utf-8
	 * @param charset          字符编码 默认：utf-8
	 * @param signType         签名类型 默认：RSA2
	 * @param alipayGateway    支付宝网关 默认：https://openapi.alipay.com/gateway.do
	 * @param grantType        授权访问令牌的授权类型 默认：authorization_code
	 */
	public AliTool(String appId, String privateKey, String publicKey, String alipayPublickkey, String partner, String signCharset, String charset, String
			signType, String alipayGateway, String grantType) {
		this(appId, privateKey, publicKey, alipayPublickkey, partner);
		if (signCharset != null && signCharset.trim().length() > 0)
			this.SIGN_CHARSET = signCharset.trim();
		if (charset != null && charset.trim().length() > 0)
			this.CHARSET = charset.trim();
		if (signType != null && signType.trim().length() > 0)
			this.SIGN_TYPE = signType.trim();
		if (alipayGateway != null && alipayGateway.trim().length() > 0)
			this.ALIPAY_GATEWAY = alipayGateway.trim();
		if (grantType != null && grantType.trim().length() > 0)
			this.GRANT_TYPE = grantType.trim();
	}

	public String getPUBLIC_KEY() {
		return PUBLIC_KEY;
	}

	public String getPARTNER() {
		return PARTNER;
	}

	// FIXME   绑定商户会员号接口

	/**
	 * 添加绑定商户会员号
	 *
	 * @param bindAccountNo 绑定帐号，建议在开发者的系统中保持唯一性
	 * @param displayName   开发者期望在服务窗首页看到的关于该用户的显示信息，最长10个字符
	 * @param fromUserId    要绑定的商户会员对应的支付宝userid，2088开头长度为16位的字符串
	 * @param agreementId   账户添加成功，在支付宝与其对应的协议号。如果账户重复添加，接口保证幂等依然视为添加成功，返回此前该账户在支付宝对应的协议号。其他异常该字段不存在。
	 * @param realName      要绑定的商户会员的真实姓名，最长10个汉字
	 * @param remark        备注信息，开发者可以通过该字段纪录其他的额外信息
	 * @return AlipayOpenPublicAccountCreateResponse 对象
	 */
	public AlipayOpenPublicAccountCreateResponse accountCreate(String bindAccountNo, String displayName, String fromUserId, String agreementId, String
			realName, String remark) {
		return accountCreate(bindAccountNo, displayName, fromUserId, agreementId, realName, remark, "JSON");
	}

	/**
	 * 添加绑定商户会员号
	 *
	 * @param bindAccountNo 绑定帐号，建议在开发者的系统中保持唯一性
	 * @param displayName   开发者期望在服务窗首页看到的关于该用户的显示信息，最长10个字符
	 * @param fromUserId    要绑定的商户会员对应的支付宝userid，2088开头长度为16位的字符串
	 * @param agreementId   账户添加成功，在支付宝与其对应的协议号。如果账户重复添加，接口保证幂等依然视为添加成功，返回此前该账户在支付宝对应的协议号。其他异常该字段不存在。
	 * @param realName      要绑定的商户会员的真实姓名，最长10个汉字
	 * @param remark        备注信息，开发者可以通过该字段纪录其他的额外信息
	 * @param format        请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicAccountCreateResponse 对象
	 */
	public AlipayOpenPublicAccountCreateResponse accountCreate(String bindAccountNo, String displayName, String fromUserId, String agreementId, String
			realName, String remark, String format) {
		AlipayOpenPublicAccountCreateModel model = getAlipayOpenPublicAccountCreateModel(bindAccountNo, displayName, fromUserId, agreementId, realName,
				remark);
		return accountCreate(model, format);
	}

	/**
	 * 添加绑定商户会员号
	 *
	 * @param model AlipayOpenPublicAccountCreateModel对象
	 * @return AlipayOpenPublicAccountCreateResponse 对象
	 */
	public AlipayOpenPublicAccountCreateResponse accountCreate(AlipayOpenPublicAccountCreateModel model) {
		return accountCreate(model, "JSON");
	}

	/**
	 * 添加绑定商户会员号
	 *
	 * @param model  AlipayOpenPublicAccountCreateModel 对象
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicAccountCreateResponse 对象
	 */
	public AlipayOpenPublicAccountCreateResponse accountCreate(AlipayOpenPublicAccountCreateModel model, String format) {
		if (null == format || format.trim().length() == 0)
			format = "JSON";
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicAccountCreateRequest request = new AlipayOpenPublicAccountCreateRequest();
		request.setBizModel(model);
		AlipayOpenPublicAccountCreateResponse response = null;
		try {

			// 使用SDK，调用交易下单接口
			response = alipayClient.execute(request);

		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 查询绑定商户会员号
	 *
	 * @param userId 支付宝账号userid，2088开头长度为16位的字符串
	 * @return AlipayOpenPublicAccountQueryResponse 对象
	 */
	public AlipayOpenPublicAccountQueryResponse accountQuery(String userId) {
		return accountQuery(userId, "JSON");
	}

	/**
	 * 查询绑定商户会员号
	 *
	 * @param userId 支付宝账号userid，2088开头长度为16位的字符串
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicAccountQueryResponse 对象
	 */
	public AlipayOpenPublicAccountQueryResponse accountQuery(String userId, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicAccountQueryRequest request = new AlipayOpenPublicAccountQueryRequest();
		AlipayOpenPublicAccountQueryModel model = new AlipayOpenPublicAccountQueryModel();
		AlipayOpenPublicAccountQueryResponse response = null;
		try {
			if (null != userId && userId.trim().length() > 0)
				model.setUserId(userId);
			else throw new AlipayApiException("查询绑定商户会员号时userId必须要有");
			request.setBizModel(model);
			// 使用SDK，调用交易下单接口
			response = alipayClient.execute(request);

		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 重置绑定的商户会员号
	 *
	 * @param bindAccountNo 绑定帐号，建议在开发者的系统中保持唯一性
	 * @param displayName   开发者期望在服务窗首页看到的关于该用户的显示信息，最长10个字符
	 * @param fromUserId    要绑定的商户会员对应的支付宝userid，2088开头长度为16位的字符串
	 * @param agreementId   账户添加成功，在支付宝与其对应的协议号。如果账户重复添加，接口保证幂等依然视为添加成功，返回此前该账户在支付宝对应的协议号。其他异常该字段不存在。
	 * @param realName      要绑定的商户会员的真实姓名，最长10个汉字
	 * @param remark        备注信息，开发者可以通过该字段纪录其他的额外信息
	 * @return AlipayOpenPublicAccountResetResponse 对象
	 */
	public AlipayOpenPublicAccountResetResponse accountReset(String bindAccountNo, String displayName, String fromUserId, String agreementId, String realName,
															 String remark) {
		return accountReset(bindAccountNo, displayName, fromUserId, agreementId, realName, remark, "JSON");
	}

	/**
	 * 重置绑定的商户会员号
	 *
	 * @param bindAccountNo 绑定帐号，建议在开发者的系统中保持唯一性
	 * @param displayName   开发者期望在服务窗首页看到的关于该用户的显示信息，最长10个字符
	 * @param fromUserId    要绑定的商户会员对应的支付宝userid，2088开头长度为16位的字符串
	 * @param agreementId   账户添加成功，在支付宝与其对应的协议号。如果账户重复添加，接口保证幂等依然视为添加成功，返回此前该账户在支付宝对应的协议号。其他异常该字段不存在。
	 * @param realName      要绑定的商户会员的真实姓名，最长10个汉字
	 * @param remark        备注信息，开发者可以通过该字段纪录其他的额外信息
	 * @param format        请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicAccountResetResponse 对象
	 */
	public AlipayOpenPublicAccountResetResponse accountReset(String bindAccountNo, String displayName, String fromUserId, String agreementId, String realName,
															 String remark, String format) {
		return accountReset(getAlipayOpenPublicAccountResetModel(bindAccountNo, displayName, fromUserId, agreementId, realName, remark), format);
	}

	/**
	 * 重置绑定的商户会员号
	 *
	 * @param model AlipayOpenPublicAccountResetModel 对象
	 * @return AlipayOpenPublicAccountResetResponse 对象
	 */
	public AlipayOpenPublicAccountResetResponse accountReset(AlipayOpenPublicAccountResetModel model) {
		return accountReset(model, "JSON");
	}

	/**
	 * 重置绑定的商户会员号
	 *
	 * @param model  AlipayOpenPublicAccountResetModel 对象
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicAccountResetResponse 对象
	 */
	public AlipayOpenPublicAccountResetResponse accountReset(AlipayOpenPublicAccountResetModel model, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicAccountResetRequest request = new AlipayOpenPublicAccountResetRequest();
		request.setBizModel(model);
		AlipayOpenPublicAccountResetResponse response = null;

		try {

			// 使用SDK，调用交易下单接口
			response = alipayClient.execute(request);

		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 解除绑定的商户会员号
	 *
	 * @param agreementId   账户添加成功，在支付宝与其对应的协议号。如果账户重复添加，接口保证幂等依然视为添加成功，返回此前该账户在支付宝对应的协议号。其他异常该字段不存在。
	 * @param bindAccountNo 绑定帐号，建议在开发者的系统中保持唯一性
	 * @return AlipayOpenPublicAccountDeleteResponse 对象
	 */
	public AlipayOpenPublicAccountDeleteResponse accountDelete(String agreementId, String bindAccountNo) {
		return accountDelete(agreementId, bindAccountNo, "JSON");
	}

	/**
	 * 解除绑定的商户会员号
	 *
	 * @param agreementId   账户添加成功，在支付宝与其对应的协议号。如果账户重复添加，接口保证幂等依然视为添加成功，返回此前该账户在支付宝对应的协议号。其他异常该字段不存在。
	 * @param bindAccountNo 绑定帐号，建议在开发者的系统中保持唯一性
	 * @param format        请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicAccountDeleteResponse 对象
	 */
	public AlipayOpenPublicAccountDeleteResponse accountDelete(String agreementId, String bindAccountNo, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicAccountDeleteRequest request = new AlipayOpenPublicAccountDeleteRequest();
		AlipayOpenPublicAccountDeleteModel model = new AlipayOpenPublicAccountDeleteModel();
		AlipayOpenPublicAccountDeleteResponse response = null;

		try {
			if ((agreementId == null || agreementId.trim().length() == 0) && (bindAccountNo == null || bindAccountNo.trim().length() == 0))
				throw new AlipayApiException("解除绑定的商户会员号时agreementId与bindAccountNo不能同时为空！");
			if (agreementId != null && agreementId.trim().length() > 0)
				model.setAgreementId(agreementId);
			if (bindAccountNo != null && bindAccountNo.trim().length() > 0)
				model.setBindAccountNo(bindAccountNo);
			request.setBizModel(model);

			// 使用SDK，调用交易下单接口
			response = alipayClient.execute(request);


		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}


	// FIXME   基础信息设置接口

	/**
	 * 服务窗基础信息查询接口
	 *
	 * @return AlipayOpenPublicInfoQueryResponse 对象
	 */
	public AlipayOpenPublicInfoQueryResponse appInfoQuery() {
		return appInfoQuery("JSON");
	}

	/**
	 * 服务窗基础信息查询接口
	 *
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicInfoQueryResponse 对象
	 */
	public AlipayOpenPublicInfoQueryResponse appInfoQuery(String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicInfoQueryRequest request = new AlipayOpenPublicInfoQueryRequest();
		AlipayOpenPublicInfoQueryResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 服务窗基础信息修改接口
	 *
	 * @param appName        服务窗名称，2-20个字之间；不得含有违反法律法规和公序良俗的相关信息；不得侵害他人名誉权、知识产权、商业秘密等合法权利；不得以太过广泛的、或产品、行业词组来命名，如：女装、皮革批发；不得以实名认证的媒体资质账号创建服务窗，或媒体相关名称命名服务窗，如：XX
	 *                          电视台、XX杂志等
	 * @param authPic        授权运营书，企业商户若为被经营方授权，需上传加盖公章的扫描件，请使用照片上传接口上传图片获得image_url
	 * @param licenseUrl     营业执照地址，建议尺寸 320 x 320px，支持.jpg .jpeg .png 格式，小于3M
	 * @param logoUrl        服务窗头像地址，建议尺寸 320 x 320px，支持.jpg .jpeg .png 格式，小于3M
	 * @param publicGreeting 服务窗欢迎语，200字以内，首次使用服务窗必须
	 * @param shopPics       门店照片Url
	 * @return AlipayOpenPublicInfoModifyResponse 对象
	 */
	public AlipayOpenPublicInfoModifyResponse appInfoModify(String appName, String authPic, String licenseUrl, String logoUrl, String publicGreeting,
															List<String> shopPics) {
		return appInfoModify(appName, authPic, licenseUrl, logoUrl, publicGreeting, shopPics, "JSON");
	}

	/**
	 * 服务窗基础信息修改接口
	 *
	 * @param appName        服务窗名称，2-20个字之间；不得含有违反法律法规和公序良俗的相关信息；不得侵害他人名誉权、知识产权、商业秘密等合法权利；不得以太过广泛的、或产品、行业词组来命名，如：女装、皮革批发；不得以实名认证的媒体资质账号创建服务窗，或媒体相关名称命名服务窗，如：XX
	 *                          电视台、XX杂志等
	 * @param authPic        授权运营书，企业商户若为被经营方授权，需上传加盖公章的扫描件，请使用照片上传接口上传图片获得image_url
	 * @param licenseUrl     营业执照地址，建议尺寸 320 x 320px，支持.jpg .jpeg .png 格式，小于3M
	 * @param logoUrl        服务窗头像地址，建议尺寸 320 x 320px，支持.jpg .jpeg .png 格式，小于3M
	 * @param publicGreeting 服务窗欢迎语，200字以内，首次使用服务窗必须
	 * @param shopPics       门店照片Url
	 * @param format         请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicInfoModifyResponse 对象
	 */
	public AlipayOpenPublicInfoModifyResponse appInfoModify(String appName, String authPic, String licenseUrl, String logoUrl, String publicGreeting,
															List<String> shopPics, String format) {
		return appInfoModify(getAlipayOpenPublicInfoModifyModel(appName, authPic, licenseUrl, logoUrl, publicGreeting, shopPics), format);
	}

	/**
	 * 服务窗基础信息修改接口
	 *
	 * @param model AlipayOpenPublicInfoModifyModel对象
	 * @return AlipayOpenPublicInfoModifyResponse 对象
	 */
	public AlipayOpenPublicInfoModifyResponse appInfoModify(AlipayOpenPublicInfoModifyModel model) {
		return appInfoModify(model, "JSON");
	}

	/**
	 * 服务窗基础信息修改接口
	 *
	 * @param model  AlipayOpenPublicInfoModifyModel对象
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicInfoModifyResponse 对象
	 */
	public AlipayOpenPublicInfoModifyResponse appInfoModify(AlipayOpenPublicInfoModifyModel model, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicInfoModifyRequest request = new AlipayOpenPublicInfoModifyRequest();
		request.setBizModel(model);
		AlipayOpenPublicInfoModifyResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}


	// FIXME 推广支持接口

	/**
	 * 带参推广二维码接口
	 *
	 * @param model  AlipayOpenPublicQrcodeCreateModel 对象
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicQrcodeCreateResponse 对象
	 */
	public AlipayOpenPublicQrcodeCreateResponse qrcodeCreate(AlipayOpenPublicQrcodeCreateModel model, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicQrcodeCreateRequest request = new AlipayOpenPublicQrcodeCreateRequest();
		request.setBizModel(model);
		AlipayOpenPublicQrcodeCreateResponse response = new AlipayOpenPublicQrcodeCreateResponse();
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 带参推广二维码接口
	 *
	 * @param model AlipayOpenPublicQrcodeCreateModel 对象
	 * @return AlipayOpenPublicQrcodeCreateResponse 对象
	 */
	public AlipayOpenPublicQrcodeCreateResponse qrcodeCreate(AlipayOpenPublicQrcodeCreateModel model) {
		return qrcodeCreate(model, "JSON");
	}

	/**
	 * 带参推广二维码接口
	 *
	 * @param codeInfo     CodeInfo 对象  服务窗创建带参二维码接口，开发者自定义信息
	 * @param codeType     二维码类型，目前只支持两种类型：  TEMP：临时的（默认）；  PERM：永久的
	 * @param expireSecond 临时码过期时间，以秒为单位，最大不超过1800秒；  永久码置空
	 * @param showLogo     二维码中间是否显示服务窗logo，Y：显示；N：不显示（默认）
	 * @param format       请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicQrcodeCreateResponse 对象
	 */
	public AlipayOpenPublicQrcodeCreateResponse qrcodeCreate(CodeInfo codeInfo, String codeType, String expireSecond, String showLogo, String format) {
		return qrcodeCreate(getAlipayOpenPublicQrcodeCreateModel(codeInfo, codeType, expireSecond, showLogo), format);
	}

	/**
	 * 带参推广二维码接口
	 *
	 * @param codeInfo     CodeInfo 对象  服务窗创建带参二维码接口，开发者自定义信息
	 * @param codeType     二维码类型，目前只支持两种类型：  TEMP：临时的（默认）；  PERM：永久的
	 * @param expireSecond 临时码过期时间，以秒为单位，最大不超过1800秒；  永久码置空
	 * @param showLogo     二维码中间是否显示服务窗logo，Y：显示；N：不显示（默认）
	 * @return AlipayOpenPublicQrcodeCreateResponse 对象
	 */
	public AlipayOpenPublicQrcodeCreateResponse qrcodeCreate(CodeInfo codeInfo, String codeType, String expireSecond, String showLogo) {
		return qrcodeCreate(codeInfo, codeType, expireSecond, showLogo, "JSON");
	}

	/**
	 * 带参推广二维码接口
	 *
	 * @param gotoUrl      跳转URL，扫码关注服务窗后会直接跳转到此URL
	 * @param sceneId      场景Id，最长32位，英文字母、数字以及下划线，开发者自定义
	 * @param codeType     二维码类型，目前只支持两种类型：  TEMP：临时的（默认）；  PERM：永久的
	 * @param expireSecond 临时码过期时间，以秒为单位，最大不超过1800秒；  永久码置空
	 * @param showLogo     二维码中间是否显示服务窗logo，Y：显示；N：不显示（默认）
	 * @param format       请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicQrcodeCreateResponse 对象
	 */
	public AlipayOpenPublicQrcodeCreateResponse qrcodeCreate(String gotoUrl, String sceneId, String codeType, String expireSecond, String showLogo, String
			format) {
		return qrcodeCreate(getAlipayOpenPublicQrcodeCreateModel(gotoUrl, sceneId, codeType, expireSecond, showLogo), format);
	}

	/**
	 * 带参推广二维码接口
	 *
	 * @param gotoUrl      跳转URL，扫码关注服务窗后会直接跳转到此URL
	 * @param sceneId      场景Id，最长32位，英文字母、数字以及下划线，开发者自定义
	 * @param codeType     二维码类型，目前只支持两种类型：  TEMP：临时的（默认）；  PERM：永久的
	 * @param expireSecond 临时码过期时间，以秒为单位，最大不超过1800秒；  永久码置空
	 * @param showLogo     二维码中间是否显示服务窗logo，Y：显示；N：不显示（默认）
	 * @return AlipayOpenPublicQrcodeCreateResponse 对象
	 */
	public AlipayOpenPublicQrcodeCreateResponse qrcodeCreate(String gotoUrl, String sceneId, String codeType, String expireSecond, String showLogo) {
		return qrcodeCreate(gotoUrl, sceneId, codeType, expireSecond, showLogo, "JSON");
	}

	/**
	 * 带参推广短链接接口
	 *
	 * @param sceneId 短链接对应的场景ID，该ID由商户自己定义
	 * @param remark  对于场景ID的描述，商户自己定义
	 * @param format  请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicShortlinkCreateResponse 对象
	 */
	public AlipayOpenPublicShortlinkCreateResponse shortlinkCreate(String sceneId, String remark, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicShortlinkCreateRequest request = new AlipayOpenPublicShortlinkCreateRequest();
		AlipayOpenPublicShortlinkCreateModel model = new AlipayOpenPublicShortlinkCreateModel();
		AlipayOpenPublicShortlinkCreateResponse response = new AlipayOpenPublicShortlinkCreateResponse();
		if (null != remark && remark.trim().length() > 0)
			model.setRemark(remark);
		try {
			if (null != sceneId && sceneId.trim().length() > 0)
				model.setSceneId(sceneId);
			else
				throw new Exception("带参推广短链接必须要有sceneId");
			request.setBizModel(model);
			response = alipayClient.execute(request);
			System.out.println(response.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 带参推广短链接接口
	 *
	 * @param sceneId 短链接对应的场景ID，该ID由商户自己定义
	 * @param remark  对于场景ID的描述，商户自己定义
	 * @return AlipayOpenPublicShortlinkCreateResponse 对象
	 */
	public AlipayOpenPublicShortlinkCreateResponse shortlinkCreate(String sceneId, String remark) {
		return shortlinkCreate(sceneId, remark, "JSON");
	}


	// FIXME 扩展区接口

	/**
	 * 使用alipay.offline.material.image.upload接口预先上传图片
	 *
	 * @param request AlipayOfflineMaterialImageUploadRequest 对象
	 * @param format  请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOfflineMaterialImageUploadResponse 对象
	 */
	public AlipayOfflineMaterialImageUploadResponse imageUpload(AlipayOfflineMaterialImageUploadRequest request, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOfflineMaterialImageUploadResponse response = null;
		try {
			response = alipayClient.execute(request);
			System.out.println(response.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 使用alipay.offline.material.image.upload接口预先上传图片
	 *
	 * @param imageName    图片/视频名称（如：jpg或mp4）
	 * @param imageType    图片/视频格式
	 * @param imageContent FileItem 对象，图片/视频二进制内容，图片/视频大小不能超过5M
	 * @param imagePid     用于显示指定图片/视频所属的partnerId（支付宝内部使用，外部商户无需填写此字段）
	 * @param format       请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOfflineMaterialImageUploadResponse 对象
	 */
	public AlipayOfflineMaterialImageUploadResponse imageUpload(String imageName, String imageType, FileItem imageContent, String imagePid, String format) {
		return imageUpload(getAlipayOfflineMaterialImageUploadRequest(imageName, imageType, imageContent, imagePid), format);
	}

	/**
	 * 使用alipay.offline.material.image.upload接口预先上传图片
	 *
	 * @param imageName 图片/视频名称（如：jpg或mp4）
	 * @param imageType 图片/视频格式
	 * @param imagePath 图片/视频的路径
	 * @param imagePid  用于显示指定图片/视频所属的partnerId（支付宝内部使用，外部商户无需填写此字段）
	 * @param format    请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOfflineMaterialImageUploadResponse 对象
	 */
	public AlipayOfflineMaterialImageUploadResponse imageUpload(String imageName, String imageType, String imagePath, String imagePid, String format) {
		return imageUpload(getAlipayOfflineMaterialImageUploadRequest(imageName, imageType, imagePath, imagePid), format);
	}

	/**
	 * 使用alipay.offline.material.image.upload接口预先上传图片
	 *
	 * @param imageName    图片/视频名称（如：jpg或mp4）
	 * @param imageType    图片/视频格式
	 * @param imageContent FileItem 对象，图片/视频二进制内容，图片/视频大小不能超过5M
	 * @param imagePid     用于显示指定图片/视频所属的partnerId（支付宝内部使用，外部商户无需填写此字段）
	 * @return AlipayOfflineMaterialImageUploadResponse 对象
	 */
	public AlipayOfflineMaterialImageUploadResponse imageUpload(String imageName, String imageType, FileItem imageContent, String imagePid) {
		return imageUpload(imageName, imageType, imageContent, imagePid, "JSON");
	}

	/**
	 * 使用alipay.offline.material.image.upload接口预先上传图片
	 *
	 * @param imageName 图片/视频名称（如：jpg或mp4）
	 * @param imageType 图片/视频格式
	 * @param imagePath 图片/视频的路径
	 * @param imagePid  用于显示指定图片/视频所属的partnerId（支付宝内部使用，外部商户无需填写此字段）
	 * @return AlipayOfflineMaterialImageUploadResponse 对象
	 */
	public AlipayOfflineMaterialImageUploadResponse imageUpload(String imageName, String imageType, String imagePath, String imagePid) {
		return imageUpload(imageName, imageType, imagePath, imagePid, "JSON");
	}

	/**
	 * 默认扩展区创建
	 *
	 * @param areas  List&lt;ExtensionArea&gt;对象   默认扩展区列表，最多包含3个扩展区
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicDefaultExtensionCreateResponse 对象
	 */
	public AlipayOpenPublicDefaultExtensionCreateResponse defaultExtensionCreate(List<ExtensionArea> areas, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicDefaultExtensionCreateRequest request = new AlipayOpenPublicDefaultExtensionCreateRequest();
		AlipayOpenPublicDefaultExtensionCreateModel model = new AlipayOpenPublicDefaultExtensionCreateModel();
		AlipayOpenPublicDefaultExtensionCreateResponse response = new AlipayOpenPublicDefaultExtensionCreateResponse();
		try {
			if (areas != null)
				model.setAreas(areas);
			else throw new Exception("创建扩展区时areas不能为空！");
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 默认扩展区创建
	 *
	 * @param areas List&lt;ExtensionArea&gt;对象  默认扩展区列表，最多包含3个扩展区
	 * @return AlipayOpenPublicDefaultExtensionCreateResponse 对象
	 */
	public AlipayOpenPublicDefaultExtensionCreateResponse defaultExtensionCreate(List<ExtensionArea> areas) {
		return defaultExtensionCreate(areas, "JSON");
	}

	/**
	 * 个性化扩展区创建
	 *
	 * @param areas      List&lt;ExtensionArea&gt;对象  默认扩展区列表，最多包含3个扩展区
	 * @param labelRules List&lt;LabelRule&gt;对象  标签规则，目前限定只能传入1条，在扩展区上线后，满足该标签规则的用户进入生活号首页，将看到该套扩展区。
	 * @param format     请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicPersonalizedExtensionCreateResponse 对象
	 */
	public AlipayOpenPublicPersonalizedExtensionCreateResponse personalizedExtensionCreate(List<ExtensionArea> areas, List<LabelRule> labelRules, String
			format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicPersonalizedExtensionCreateRequest request = new AlipayOpenPublicPersonalizedExtensionCreateRequest();
		AlipayOpenPublicPersonalizedExtensionCreateModel model = new AlipayOpenPublicPersonalizedExtensionCreateModel();
		AlipayOpenPublicPersonalizedExtensionCreateResponse response = new AlipayOpenPublicPersonalizedExtensionCreateResponse();
		try {
			model.setAreas(areas);
			model.setLabelRule(labelRules);
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 个性化扩展区创建
	 *
	 * @param areas      List&lt;ExtensionArea&gt;对象  默认扩展区列表，最多包含3个扩展区
	 * @param labelRules List&lt;LabelRule&gt;对象  标签规则，目前限定只能传入1条，在扩展区上线后，满足该标签规则的用户进入生活号首页，将看到该套扩展区。
	 * @return AlipayOpenPublicPersonalizedExtensionCreateResponse 对象
	 */
	public AlipayOpenPublicPersonalizedExtensionCreateResponse personalizedExtensionCreate(List<ExtensionArea> areas, List<LabelRule> labelRules) {
		return personalizedExtensionCreate(areas, labelRules, "JSON");
	}

	/**
	 * 化扩展区删除
	 *
	 * @param extensionKey 一套扩展区的key，删除默认扩展区时传入default ，查询扩展区列表可以获得每套扩展区的key
	 * @param format       请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicPersonalizedExtensionDeleteResponse 对象
	 */
	public AlipayOpenPublicPersonalizedExtensionDeleteResponse extensionDelete(String extensionKey, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicPersonalizedExtensionDeleteRequest request = new AlipayOpenPublicPersonalizedExtensionDeleteRequest();
		AlipayOpenPublicPersonalizedExtensionDeleteModel model = new AlipayOpenPublicPersonalizedExtensionDeleteModel();
		AlipayOpenPublicPersonalizedExtensionDeleteResponse response = new AlipayOpenPublicPersonalizedExtensionDeleteResponse();
		try {
			//删除默认扩展区，extension_key为default
			//        model.setExtensionKey("default");
			if (extensionKey != null && extensionKey.trim().length() > 0)
				model.setExtensionKey(extensionKey);
			else throw new Exception("化扩展区删除时extensionKey不能为空！");
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 化扩展区删除
	 *
	 * @param extensionKey 一套扩展区的key，删除默认扩展区时传入default ，查询扩展区列表可以获得每套扩展区的key
	 * @return AlipayOpenPublicPersonalizedExtensionDeleteResponse 对象
	 */
	public AlipayOpenPublicPersonalizedExtensionDeleteResponse extensionDelete(String extensionKey) {
		return extensionDelete(extensionKey, "JSON");
	}

	/**
	 * 个性化扩展区批量查询
	 *
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicPersonalizedExtensionBatchqueryResponse 对象
	 */
	public AlipayOpenPublicPersonalizedExtensionBatchqueryResponse batchQueryExtensions(String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicPersonalizedExtensionBatchqueryRequest request = new AlipayOpenPublicPersonalizedExtensionBatchqueryRequest();
		AlipayOpenPublicPersonalizedExtensionBatchqueryResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 个性化扩展区批量查询
	 *
	 * @return AlipayOpenPublicPersonalizedExtensionBatchqueryResponse 对象
	 */
	public AlipayOpenPublicPersonalizedExtensionBatchqueryResponse batchQueryExtensions() {
		return batchQueryExtensions("JSON");
	}


	// FIXME 服务窗自定义标签接口

	/**
	 * 创建自定义标签
	 *
	 * @param labelName 自定义标签名
	 * @param dataType  标签值类型，目前只支持string（字符串类型），不传默认为"string"
	 * @param format    请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicLifeLabelCreateResponse 对象
	 */
	public AlipayOpenPublicLifeLabelCreateResponse createLifeLabel(String labelName, String dataType, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicLifeLabelCreateRequest request = new AlipayOpenPublicLifeLabelCreateRequest();
		AlipayOpenPublicLifeLabelCreateModel model = new AlipayOpenPublicLifeLabelCreateModel();
		if (null == dataType || dataType.trim().length() == 0)
			dataType = "String";
		AlipayOpenPublicLifeLabelCreateResponse response = null;
		try {
			if (null != labelName && labelName.trim().length() > 0)
				model.setLabelName(labelName);
			else throw new Exception("创建自定义标签时labelName不能为空！");
			model.setDataType(dataType);
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 创建自定义标签
	 *
	 * @param labelName 自定义标签名
	 * @param dataType  标签值类型，目前只支持string（字符串类型），不传默认为"string"
	 * @return AlipayOpenPublicLifeLabelCreateResponse 对象
	 */
	public AlipayOpenPublicLifeLabelCreateResponse createLifeLabel(String labelName, String dataType) {
		return createLifeLabel(labelName, dataType, "JSON");
	}

	/**
	 * 修改自定义标签
	 *
	 * @param labelName 标签名
	 * @param labelId   标签id，调用创建标签接口后由支付宝返回 ，只支持生活号自定义标签
	 * @param format    请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicLifeLabelModifyResponse 对象
	 */
	public AlipayOpenPublicLifeLabelModifyResponse modifyLifeLabel(String labelName, String labelId, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicLifeLabelModifyRequest request = new AlipayOpenPublicLifeLabelModifyRequest();
		AlipayOpenPublicLifeLabelModifyModel model = new AlipayOpenPublicLifeLabelModifyModel();
		AlipayOpenPublicLifeLabelModifyResponse response = null;
		try {
			if (null != labelName && labelName.trim().length() > 0)
				model.setLabelId("6");
			else throw new Exception("修改自定义标签时labelName不能为空！");
			if (null != labelId && labelId.trim().length() > 0)
				model.setLabelName("测试标签003");
			else throw new Exception("修改自定义标签时labelId不能为空！");
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 修改自定义标签
	 *
	 * @param labelName 标签名
	 * @param labelId   标签id，调用创建标签接口后由支付宝返回 ，只支持生活号自定义标签
	 * @return AlipayOpenPublicLifeLabelModifyResponse 对象
	 */
	public AlipayOpenPublicLifeLabelModifyResponse modifyLifeLabel(String labelName, String labelId) {
		return modifyLifeLabel(labelName, labelId, "JSON");
	}

	/**
	 * 批量查询自定义标签
	 *
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicLifeLabelBatchqueryResponse 对象
	 */
	public AlipayOpenPublicLifeLabelBatchqueryResponse lifeLabelBatchQuery(String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicLifeLabelBatchqueryRequest request = new AlipayOpenPublicLifeLabelBatchqueryRequest();
		AlipayOpenPublicLifeLabelBatchqueryResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 批量查询自定义标签
	 *
	 * @return AlipayOpenPublicLifeLabelBatchqueryResponse 对象
	 */
	public AlipayOpenPublicLifeLabelBatchqueryResponse lifeLabelBatchQuery() {
		return lifeLabelBatchQuery("JSON");
	}

	//删除自定义标签
	public AlipayOpenPublicLifeLabelDeleteResponse deleteLifeLabel(String labelId, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicLifeLabelDeleteRequest request = new AlipayOpenPublicLifeLabelDeleteRequest();
		AlipayOpenPublicLifeLabelDeleteModel model = new AlipayOpenPublicLifeLabelDeleteModel();
		AlipayOpenPublicLifeLabelDeleteResponse response = new AlipayOpenPublicLifeLabelDeleteResponse();
		try {
			if (null != labelId && labelId.trim().length() > 0)
				model.setLabelId(labelId);
			else throw new Exception("删除自定义标签时labelId不能为空！");
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public AlipayOpenPublicLifeLabelDeleteResponse deleteLifeLabel(String labelId) {
		return deleteLifeLabel(labelId, "JSON");
	}

	/**
	 * 用户打标匹配
	 *
	 * @param labelId    标签id，调用创建标签接口会返回label_id
	 * @param labelValue 标签值，由开发者自主指定，标签值类型要满足创建标签接口中data_type参数的限定。
	 * @param matchers   List&lt;Matcher&gt;对象， 支付宝用户匹配器列表，最多传入10条
	 * @param format     请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMatchuserLabelCreateResponse 对象
	 */
	public AlipayOpenPublicMatchuserLabelCreateResponse matchuserLabelAdd(String labelId, String labelValue, List<Matcher> matchers, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMatchuserLabelCreateRequest request = new AlipayOpenPublicMatchuserLabelCreateRequest();
		AlipayOpenPublicMatchuserLabelCreateModel model = new AlipayOpenPublicMatchuserLabelCreateModel();
		AlipayOpenPublicMatchuserLabelCreateResponse response = null;
		try {
			if (labelId != null && labelId.trim().length() > 0)
				model.setLabelId(labelId);
			else throw new Exception("用户打标匹配时labelId不能为空！");
			if (null != labelValue && labelValue.trim().length() > 0)
				model.setLabelValue(labelValue);
			else throw new Exception("用户打标匹配时labelValue不能为空！");
			if (matchers == null)
				throw new Exception("用户打标匹配时matchers不能为空！");
			model.setMatchers(matchers);
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 用户打标匹配
	 *
	 * @param labelId    标签id，调用创建标签接口会返回label_id
	 * @param labelValue 标签值，由开发者自主指定，标签值类型要满足创建标签接口中data_type参数的限定。
	 * @param matchers   List&lt;Matcher&gt;对象， 支付宝用户匹配器列表，最多传入10条
	 * @return AlipayOpenPublicMatchuserLabelCreateResponse 对象
	 */
	public AlipayOpenPublicMatchuserLabelCreateResponse matchuserLabelAdd(String labelId, String labelValue, List<Matcher> matchers) {
		return matchuserLabelAdd(labelId, labelValue, matchers, "JSON");
	}

	/**
	 * 取消匹配用户标签
	 *
	 * @param labelId  标签id
	 * @param matchers List&lt;Matcher&gt;对象， 支付宝用户匹配器列表，最多传入10条
	 * @param format   请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMatchuserLabelDeleteResponse 对象
	 */
	public AlipayOpenPublicMatchuserLabelDeleteResponse matchuserLabelDel(String labelId, List<Matcher> matchers, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMatchuserLabelDeleteRequest request = new AlipayOpenPublicMatchuserLabelDeleteRequest();
		AlipayOpenPublicMatchuserLabelDeleteModel model = new AlipayOpenPublicMatchuserLabelDeleteModel();
		AlipayOpenPublicMatchuserLabelDeleteResponse response = null;
		try {
			if (null != labelId && labelId.trim().length() > 0)
				model.setLabelId(labelId);
			else throw new Exception("取消匹配用户标签时labelId不能为空！");
			if (matchers == null)
				throw new Exception("取消匹配用户标签时matchers不能为空！");
			model.setMatchers(matchers);
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 取消匹配用户标签
	 *
	 * @param labelId  标签id
	 * @param matchers List&lt;Matcher&gt;对象， 支付宝用户匹配器列表，最多传入10条
	 * @return AlipayOpenPublicMatchuserLabelDeleteResponse 对象
	 */
	public AlipayOpenPublicMatchuserLabelDeleteResponse matchuserLabelDel(String labelId, List<Matcher> matchers) {
		return matchuserLabelDel(labelId, matchers, "JSON");
	}


	// FIXME 服务窗菜单相关接口

	/**
	 * 创建生活号默认菜单信息
	 *
	 * @param button List&lt;ButtonObject&gt;对象，一级菜单列表。最多有4个一级菜单，若开发者在后台打开了"咨询反馈"的开关，则只能有3个一级菜单.
	 * @param type   菜单类型，支持值为icon：icon型菜单，text：文本型菜单，不传时默认为"text"，当传值为"icon"时，菜单节点的icon字段必传。
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMenuCreateResponse 对象
	 */
	public AlipayOpenPublicMenuCreateResponse createMenu(List<ButtonObject> button, String type, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMenuCreateRequest request = new AlipayOpenPublicMenuCreateRequest();
		AlipayOpenPublicMenuCreateModel model = new AlipayOpenPublicMenuCreateModel();
		AlipayOpenPublicMenuCreateResponse response = null;
		try {
			if (button == null)
				throw new Exception("创建生活号默认菜单时button数组不能为空！");
			model.setButton(button);
			if (null == type || type.trim().length() == 0)
				type = "text";
			model.setType(type);
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 创建生活号默认菜单信息
	 *
	 * @param button List&lt;ButtonObject&gt;对象，一级菜单列表。最多有4个一级菜单，若开发者在后台打开了"咨询反馈"的开关，则只能有3个一级菜单.
	 * @param type   菜单类型，支持值为icon：icon型菜单，text：文本型菜单，不传时默认为"text"，当传值为"icon"时，菜单节点的icon字段必传。
	 * @return AlipayOpenPublicMenuCreateResponse 对象
	 */
	public AlipayOpenPublicMenuCreateResponse createMenu(List<ButtonObject> button, String type) {
		return createMenu(button, type, "JSON");
	}

	/**
	 * 生活号默认菜单更新
	 *
	 * @param button List&lt;ButtonObject&gt;对象，一级菜单列表。最多有4个一级菜单，若开发者在后台打开了"咨询反馈"的开关，则只能有3个一级菜单.
	 * @param type   菜单类型，支持值为icon：icon型菜单，text：文本型菜单，不传时默认为"text"，当传值为"icon"时，菜单节点的icon字段必传。
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMenuModifyResponse 对象
	 */
	public AlipayOpenPublicMenuModifyResponse modifyMenu(List<ButtonObject> button, String type, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMenuModifyRequest request = new AlipayOpenPublicMenuModifyRequest();
		AlipayOpenPublicMenuModifyModel model = new AlipayOpenPublicMenuModifyModel();
		AlipayOpenPublicMenuModifyResponse response = null;
		try {
			if (button == null)
				throw new Exception("生活号默认菜单更新时button数组不能为空！");
			model.setButton(button);
			if (null == type || type.trim().length() == 0)
				type = "text";
			model.setType(type);
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 生活号默认菜单更新
	 *
	 * @param button List&lt;ButtonObject&gt;对象，一级菜单列表。最多有4个一级菜单，若开发者在后台打开了"咨询反馈"的开关，则只能有3个一级菜单.
	 * @param type   菜单类型，支持值为icon：icon型菜单，text：文本型菜单，不传时默认为"text"，当传值为"icon"时，菜单节点的icon字段必传。
	 * @return AlipayOpenPublicMenuModifyResponse 对象
	 */
	public AlipayOpenPublicMenuModifyResponse modifyMenu(List<ButtonObject> button, String type) {
		return modifyMenu(button, type, "JSON");
	}

	/**
	 * 创建个性化菜单
	 *
	 * @param button    List&lt;ButtonObject&gt;对象，一级菜单列表。最多有4个一级菜单，若开发者在后台打开了"咨询反馈"的开关，则只能有3个一级菜单.
	 * @param labelRule List&lt;ButtonObject&gt;对象， 标签规则，目前限定只能传入1条，在个性化菜单创建成功后，满足该标签规则的用户进入生活号首页，将看到该套菜单。
	 * @param type      菜单类型，支持值为icon：icon型菜单，text：文本型菜单，不传时默认为"text"，当传值为"icon"时，菜单节点的icon字段必传。
	 * @param fromat    请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicPersonalizedMenuCreateResponse 对象
	 */
	public AlipayOpenPublicPersonalizedMenuCreateResponse createPersonalizedMenu(List<ButtonObject> button, List<LabelRule> labelRule, String type, String
			fromat) {
		AlipayClient alipayClient = getAlipayClient(fromat);
		AlipayOpenPublicPersonalizedMenuCreateRequest request = new AlipayOpenPublicPersonalizedMenuCreateRequest();
		AlipayOpenPublicPersonalizedMenuCreateModel model = new AlipayOpenPublicPersonalizedMenuCreateModel();
		AlipayOpenPublicPersonalizedMenuCreateResponse response = null;
		try {
			if (null == type || type.trim().length() == 0)
				type = "text";
			model.setType(type);
			if (button != null)
				model.setButton(button);
			else throw new Exception("创建个性化菜单时button数组不能为空！");
			if (labelRule != null)
				model.setLabelRule(labelRule);
			else throw new Exception("创建个性化菜单时labelRule数组不能为空！");
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 创建个性化菜单
	 *
	 * @param button    List&lt;ButtonObject&gt;对象，一级菜单列表。最多有4个一级菜单，若开发者在后台打开了"咨询反馈"的开关，则只能有3个一级菜单.
	 * @param labelRule List&lt;ButtonObject&gt;对象， 标签规则，目前限定只能传入1条，在个性化菜单创建成功后，满足该标签规则的用户进入生活号首页，将看到该套菜单。
	 * @param type      菜单类型，支持值为icon：icon型菜单，text：文本型菜单，不传时默认为"text"，当传值为"icon"时，菜单节点的icon字段必传。
	 * @return AlipayOpenPublicPersonalizedMenuCreateResponse 对象
	 */
	public AlipayOpenPublicPersonalizedMenuCreateResponse createPersonalizedMenu(List<ButtonObject> button, List<LabelRule> labelRule, String type) {
		return createPersonalizedMenu(button, labelRule, type, "JSON");
	}

	/**
	 * 个性化菜单删除
	 *
	 * @param menuKey 要删除的个性化菜单key
	 * @param format  请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicPersonalizedMenuDeleteResponse 对象
	 */
	public AlipayOpenPublicPersonalizedMenuDeleteResponse deletePersonalizedMenu(String menuKey, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicPersonalizedMenuDeleteRequest request = new AlipayOpenPublicPersonalizedMenuDeleteRequest();
		AlipayOpenPublicPersonalizedMenuDeleteModel model = new AlipayOpenPublicPersonalizedMenuDeleteModel();
		AlipayOpenPublicPersonalizedMenuDeleteResponse response = null;
		try {
			if (null != menuKey && menuKey.trim().length() > 0)
				model.setMenuKey(menuKey);
			else throw new Exception("个性化菜单删除时menuKey不能为空！");
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 个性化菜单删除
	 *
	 * @param menuKey 要删除的个性化菜单key
	 * @return AlipayOpenPublicPersonalizedMenuDeleteResponse 对象
	 */
	public AlipayOpenPublicPersonalizedMenuDeleteResponse deletePersonalizedMenu(String menuKey) {
		return deletePersonalizedMenu(menuKey, "JSON");
	}

	/**
	 * 菜单批量查询
	 *
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMenuBatchqueryResponse 对象
	 */
	public AlipayOpenPublicMenuBatchqueryResponse menuBatchQuery(String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMenuBatchqueryRequest request = new AlipayOpenPublicMenuBatchqueryRequest();
		AlipayOpenPublicMenuBatchqueryResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 菜单批量查询
	 *
	 * @return AlipayOpenPublicMenuBatchqueryResponse 对象
	 */
	public AlipayOpenPublicMenuBatchqueryResponse menuBatchQuery() {
		return menuBatchQuery("JSON");
	}


	// FIXME 发消息接口

	/**
	 * 群发图文消息
	 *
	 * @param articles List&lt;Article&gt;对象， 图文消息，当msg_type为image-text，该值必须设置
	 * @param format   请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMessageTotalSendResponse 对象
	 */
	public AlipayOpenPublicMessageTotalSendResponse toAlipayGroupSendImgTextMsg(List<Article> articles, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMessageTotalSendRequest request = new AlipayOpenPublicMessageTotalSendRequest();
		AlipayImageTextMessage message = new AlipayImageTextMessage();
		AlipayOpenPublicMessageTotalSendResponse response = null;
		try {
			Gson gson = getGson();
			message.setMsg_type("image-text");
			message.setArticles(articles);
			request.setBizContent(gson.toJson(message));
			response = alipayClient.execute(request);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 群发图文消息
	 *
	 * @param articles List&lt;Article&gt;对象， 图文消息，当msg_type为image-text，该值必须设置
	 * @return AlipayOpenPublicMessageTotalSendResponse 对象
	 */
	public AlipayOpenPublicMessageTotalSendResponse toAlipayGroupSendImgTextMsg(List<Article> articles) {
		return toAlipayGroupSendImgTextMsg(articles, "JSON");
	}

	/**
	 * 群发纯文本消息
	 *
	 * @param text   Text对象， 文本消息内容，当msg_type为text，必须设置该值
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMessageTotalSendResponse 对象
	 */
	public AlipayOpenPublicMessageTotalSendResponse toAlipayGroupSendTextMsg(Text text, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMessageTotalSendRequest request = new AlipayOpenPublicMessageTotalSendRequest();
		AlipayImageTextMessage message = new AlipayImageTextMessage();
		AlipayOpenPublicMessageTotalSendResponse response = null;
		try {
			Gson gson = getGson();
			message.setMsg_type("text");
			message.setText(text);
			request.setBizContent(gson.toJson(message));
			response = alipayClient.execute(request);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 群发纯文本消息
	 *
	 * @param text Text对象， 文本消息内容，当msg_type为text，必须设置该值
	 * @return AlipayOpenPublicMessageTotalSendResponse 对象
	 */
	public AlipayOpenPublicMessageTotalSendResponse toAlipayGroupSendTextMsg(Text text) {
		return toAlipayGroupSendTextMsg(text, "JSON");
	}

	/**
	 * 单发模板消息 服务窗组发消息（标签组发消息接口）
	 *
	 * @param toUserId   消息接收用户的userid
	 * @param templateId 消息模板ID
	 * @param headColor  顶部色条的色值
	 * @param url        点击消息后承接页的地址
	 * @param actionName 底部链接描述文字，如“查看详情”
	 * @param keywords   Map&lt;String, AlipayColorValue&gt;对象， 请至少包饭first
	 * @param format     请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMessageSingleSendResponse 对象
	 */
	public AlipayOpenPublicMessageSingleSendResponse toAlipayLabelSendMsg(String toUserId, String templateId, String headColor, String url, String actionName,
																		  Map<String, AlipayColorValue> keywords, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMessageSingleSendRequest request = new AlipayOpenPublicMessageSingleSendRequest();
		Map<String, Object> bizMap = new HashMap<>();
		bizMap.put("to_user_id", toUserId);
		Map<String, Object> templateMap = new HashMap<>();
		templateMap.put("template_id", templateId);
		Map<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put("head_color", headColor);
		contextMap.put("url", url);
		contextMap.put("action_name", actionName);

		for (Map.Entry<String, AlipayColorValue> entry : keywords.entrySet()) {
			Map<String, Object> tmp = new HashMap<String, Object>();
			String keyword = entry.getKey();
			AlipayColorValue cv = entry.getValue();
			tmp.put("color", cv.getColor());
			tmp.put("value", cv.getValue());
			contextMap.put(keyword, tmp);
		}

		templateMap.put("context", contextMap);
		bizMap.put("template", templateMap);
		request.setBizContent(getGson().toJson(bizMap));
		AlipayOpenPublicMessageSingleSendResponse response = null;
		try {
			response = alipayClient.execute(request);
			System.out.println(response.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 单发模板消息 服务窗组发消息（标签组发消息接口）
	 *
	 * @param toUserId   消息接收用户的userid
	 * @param templateId 消息模板ID
	 * @param headColor  顶部色条的色值
	 * @param url        点击消息后承接页的地址
	 * @param actionName 底部链接描述文字，如“查看详情”
	 * @param keywords   Map&lt;String, AlipayColorValue&gt;对象， 请至少包饭first
	 * @return AlipayOpenPublicMessageSingleSendResponse 对象
	 */
	public AlipayOpenPublicMessageSingleSendResponse toAlipayLabelSendMsg(String toUserId, String templateId, String headColor, String url, String actionName,
																		  Map<String, AlipayColorValue> keywords) {
		return toAlipayLabelSendMsg(toUserId, templateId, headColor, url, actionName, keywords, "JSON");
	}

	/**
	 * 单发图文消息
	 *
	 * @param toUserId 消息接收用户的userid
	 * @param articles List&lt;Article&gt;对象, 图文消息，当msg_type为image-text时，必须存在相对应的值
	 * @param chat     是否是聊天消息。支持值：0，1，当值为0时，代表是非聊天消息，消息显示在生活号主页，当值为1时，代表是聊天消息，消息显示在咨询反馈列表页。默认值为0
	 * @param format   请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMessageCustomSendResponse 对象
	 */
	public AlipayOpenPublicMessageCustomSendResponse toAlipaySingleSendImgTextMsg(String toUserId, List<Article> articles, String chat, String
			format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicMessageCustomSendRequest request = new AlipayOpenPublicMessageCustomSendRequest();
		AlipayImageTextMessage message = new AlipayImageTextMessage();
		AlipayOpenPublicMessageCustomSendResponse response = null;
		try {
			message.setTo_user_id(toUserId);
			message.setMsg_type("image-text");
			message.setArticles(articles);
			message.setChat(chat);
			request.setBizContent(getGson().toJson(message));
			response = alipayClient.execute(request);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 单发图文消息
	 *
	 * @param toUserId 消息接收用户的userid
	 * @param articles List&lt;Article&gt;对象, 图文消息，当msg_type为image-text时，必须存在相对应的值
	 * @param chat     是否是聊天消息。支持值：0，1，当值为0时，代表是非聊天消息，消息显示在生活号主页，当值为1时，代表是聊天消息，消息显示在咨询反馈列表页。默认值为0
	 * @return AlipayOpenPublicMessageCustomSendResponse 对象
	 */
	public AlipayOpenPublicMessageCustomSendResponse toAlipaySingleSendImgTextMsg(String toUserId, List<Article> articles, String chat) {
		return toAlipaySingleSendImgTextMsg(toUserId, articles, chat, "JSON");
	}

	/**
	 * 单发纯文本消息
	 *
	 * @param toUserId    消息接收用户的userid
	 * @param textContent 文本消息的内容
	 * @param chat        是否是聊天消息。支持值：0，1，当值为0时，代表是非聊天消息，消息显示在生活号主页，当值为1时，代表是聊天消息，消息显示在咨询反馈列表页。默认值为0
	 * @param format      请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicMessageCustomSendResponse 对象
	 */
	public AlipayOpenPublicMessageCustomSendResponse toAlipaySingleSendImgTextMsg(String toUserId, String textContent, String chat, String
			format) {
		AlipayClient alipayClient = getAlipayClient(format);

		// 使用SDK，构建单发请求模型
		AlipayOpenPublicMessageCustomSendRequest request = new AlipayOpenPublicMessageCustomSendRequest();
		AlipayImageTextMessage message = new AlipayImageTextMessage();
		AlipayOpenPublicMessageCustomSendResponse response = null;
		try {
			message.setTo_user_id(toUserId);
			message.setMsg_type("text");
			Text text = new Text();
			text.setContent(textContent);
			message.setText(text);
			message.setChat(chat);
			request.setBizContent(getGson().toJson(message));
			// 使用SDK，调用单发接口发送图文消息
			response = alipayClient.execute(request);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 单发纯文本消息
	 *
	 * @param toUserId    消息接收用户的userid
	 * @param textContent 文本消息的内容
	 * @param chat        是否是聊天消息。支持值：0，1，当值为0时，代表是非聊天消息，消息显示在生活号主页，当值为1时，代表是聊天消息，消息显示在咨询反馈列表页。默认值为0
	 * @return AlipayOpenPublicMessageCustomSendResponse 对象
	 */
	public AlipayOpenPublicMessageCustomSendResponse toAlipaySingleSendImgTextMsg(String toUserId, String textContent, String chat) {
		return toAlipaySingleSendImgTextMsg(toUserId, textContent, chat, "JSON");
	}


	// FIXME 获取关注用户相关信息接口

	/**
	 * 获取服务窗关注者列表
	 *
	 * @param nextUserId 当关注者数量超过10000时使用，本次拉取数据中第一个用户的userId，从上次接口调用返回值中获取。第一次调用置空
	 * @param format     请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicFollowBatchqueryResponse 对象
	 */
	public AlipayOpenPublicFollowBatchqueryResponse getFollowlist(String nextUserId, String format) {
		if (nextUserId == null || nextUserId.trim().length() == 0)
			nextUserId = "{}";
		else {
			nextUserId = "{\"next_user_id\":\"" + nextUserId + "\"}";
		}
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicFollowBatchqueryRequest request = new AlipayOpenPublicFollowBatchqueryRequest();
		request.setBizContent(nextUserId);
		AlipayOpenPublicFollowBatchqueryResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 获取服务窗关注者列表
	 *
	 * @param nextUserId 当关注者数量超过10000时使用，本次拉取数据中第一个用户的userId，从上次接口调用返回值中获取。第一次调用置空
	 * @return AlipayOpenPublicFollowBatchqueryResponse 对象
	 */
	public AlipayOpenPublicFollowBatchqueryResponse getFollowlist(String nextUserId) {
		return getFollowlist(nextUserId, "JSON");
	}

	/**
	 * 获取用户地理位置
	 *
	 * @param userId 该用户的userId
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayOpenPublicGisQueryResponse 对象
	 */
	public AlipayOpenPublicGisQueryResponse getUserLocation(String userId, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipayOpenPublicGisQueryRequest request = new AlipayOpenPublicGisQueryRequest();
		AlipayOpenPublicGisQueryModel model = new AlipayOpenPublicGisQueryModel();
		AlipayOpenPublicGisQueryResponse response = null;
		try {
			if (null != userId && userId.trim().length() > 0)
				model.setUserId(userId);
			else throw new Exception("获取用户地理位置userId不能为空！");
			request.setBizModel(model);
			response = alipayClient.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 获取用户地理位置
	 *
	 * @param userId 该用户的userId
	 * @return AlipayOpenPublicGisQueryResponse 对象
	 */
	public AlipayOpenPublicGisQueryResponse getUserLocation(String userId) {
		return getUserLocation(userId, "JSON");
	}


	// FIXME 用户信息共享

	/**
	 * 获取用户Oauth认证的accesstoken和userId
	 *
	 * @param authCode Oauth认证返回的auth_code码
	 * @param format   请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipaySystemOauthTokenResponse 对象
	 */
	public AlipaySystemOauthTokenResponse getOauthAccessToken(String authCode, String format) {
		AlipayClient alipayClient = getAlipayClient(format);
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();

		request.setCode(authCode);
		request.setGrantType(GRANT_TYPE);
		AlipaySystemOauthTokenResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 获取用户Oauth认证的accesstoken和userId
	 *
	 * @param authCode Oauth认证返回的auth_code码
	 * @return AlipaySystemOauthTokenResponse 对象
	 */
	public AlipaySystemOauthTokenResponse getOauthAccessToken(String authCode) {
		return getOauthAccessToken(authCode, "JSON");
	}

	/**
	 * 网页授权获取用户信息
	 *
	 * @param authCode Oauth认证返回的auth_code码
	 * @param format   请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayUserUserinfoShareResponse 对象
	 */
	public AlipayUserUserinfoShareResponse getOauthUserInformation(String authCode, String format) {
		AlipaySystemOauthTokenResponse oauthTokenResponse = null;
		AlipayUserUserinfoShareResponse userinfoShareResponse = null;
		try {
			//3. 利用authCode获得authToken
			AlipaySystemOauthTokenRequest oauthTokenRequest = new AlipaySystemOauthTokenRequest();
			oauthTokenRequest.setCode(authCode);
			oauthTokenRequest.setGrantType(GRANT_TYPE);
			AlipayClient alipayClient = getAlipayClient(format);
			oauthTokenResponse = alipayClient.execute(oauthTokenRequest);

			//成功获得authToken
			if (null != oauthTokenResponse && oauthTokenResponse.isSuccess()) {

				//4. 利用authToken获取用户信息
				AlipayUserUserinfoShareRequest userinfoShareRequest = new AlipayUserUserinfoShareRequest();
				userinfoShareResponse = alipayClient.execute(userinfoShareRequest, oauthTokenResponse.getAccessToken());

				//成功获得用户信息
				if (null != userinfoShareResponse && userinfoShareResponse.isSuccess()) {

				} else {
					throw new AlipayApiException("获取用户信息失败！");
				}
			} else {
				throw new AlipayApiException("authCode换取authToken失败！");
			}
		} catch (AlipayApiException alipayApiException) {
			alipayApiException.printStackTrace();
		}
		return userinfoShareResponse;
	}

	/**
	 * 网页授权获取用户信息
	 *
	 * @param authCode Oauth认证返回的auth_code码
	 * @return AlipayUserUserinfoShareResponse 对象
	 */
	public AlipayUserUserinfoShareResponse getOauthUserInformation(String authCode) {
		return getOauthUserInformation(authCode, "JSON");
	}


	// FIXME 开发者网关，支付宝所有主动和开发者的交互会经过此网关进入开发者系统(配置在开放平台的应用网关)

	/**
	 * 开发者网关 -- 签名验证
	 *
	 * @param params 签名验证参数的map
	 * @throws AlipayApiException 异常处理
	 */
	public void verifySign(Map<String, String> params) throws AlipayApiException {
		if (!AlipaySignature.rsaCheckV2(params, ALIPAY_PUBLIC_KEY, SIGN_CHARSET, SIGN_TYPE)) {
			throw new AlipayApiException("verify sign fail.");
		}
	}

	/**
	 * 开发者网关 -- 加密或签名
	 *
	 * @param bizContent      加密或签名的内容
	 * @param alipayPublicKey 阿里应用公钥
	 * @param cusPrivateKey   用户私钥
	 * @param charset         字符编码 如utf-8,gbk,gb2312
	 * @param isEncrypt       是否加密
	 * @param isSign          是否签名
	 * @param signType        签名类型
	 * @return 处理完的字符串
	 * @throws AlipayApiException 异常处理
	 */
	public String encryptAndSign(String bizContent, String alipayPublicKey, String cusPrivateKey, String charset,
								 boolean isEncrypt, boolean isSign, String signType) throws AlipayApiException {
		StringBuilder sb = new StringBuilder();
		try {
			if (StringUtils.isEmpty(charset)) {
				charset = AlipayConstants.CHARSET_UTF8;
			}
			sb.append("<?xml version=\"1.0\" encoding=\"");
			sb.append(charset);
			sb.append("\"?>");
			if (isEncrypt) {// 加密
				sb.append("<alipay>");
				String encrypted = AlipaySignature.rsaEncrypt(bizContent, alipayPublicKey, charset);
				sb.append("<response>");
				sb.append(encrypted);
				sb.append("</response>");
				sb.append("<encryption_type>AES</encryption_type>");
				if (isSign) {
					String sign = AlipaySignature.rsaSign(encrypted, cusPrivateKey, charset, signType);
					sb.append("<sign>");
					sb.append(sign);
					sb.append("</sign>");
					sb.append("<sign_type>");
					sb.append(signType);
					sb.append("</sign_type>");
				}
				sb.append("</alipay>");
			} else if (isSign) {// 不加密，但需要签名
				sb.append("<alipay>");
				sb.append("<response>");
				sb.append(bizContent);
				sb.append("</response>");
				String sign = AlipaySignature.rsaSign(bizContent, cusPrivateKey, charset, signType);
				sb.append("<sign>");
				sb.append(sign);
				sb.append("</sign>");
				sb.append("<sign_type>");
				sb.append(signType);
				sb.append("</sign_type>");
				sb.append("</alipay>");
			} else {// 不加密，不加签
				sb.append(bizContent);
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 获取GateWay的响应字符串
	 *
	 * @param requestParamsMap request请求的paramsMap
	 * @param isEncrypt        是否加密
	 * @param isSign           是否签名
	 * @return 可以直接返回的给阿里服务器的字符串
	 * @throws AlipayApiException 异常处理
	 */
	public String getGateWayResponseMsg(Map<String, String> requestParamsMap, Boolean isEncrypt, Boolean isSign) throws AlipayApiException {
		//支付宝响应消息
		String responseMsg = "";
		if (isEncrypt == null)
			isEncrypt = false;
		if (isSign == null)
			isSign = false;
		try {
			//2. 验证签名
			verifySign(requestParamsMap);

			//3. 获取业务执行器   根据请求中的 service, msgType, eventType, actionParam 确定执行器
			ActionExecutor executor = Dispatcher.getExecutor(requestParamsMap, getAlipayClient("JSON"));

			//4. 执行业务逻辑
			responseMsg = executor.execute();

		} catch (AlipayApiException alipayApiException) {
			//开发者可以根据异常自行进行处理
			alipayApiException.printStackTrace();

		} catch (Exception exception) {
			//开发者可以根据异常自行进行处理
			exception.printStackTrace();

		} finally {
			//5. 响应结果加签及返回
			try {
				//对响应内容加签
				responseMsg = encryptAndSign(responseMsg, ALIPAY_PUBLIC_KEY, PRIVATE_KEY, CHARSET,
						isEncrypt, isSign, SIGN_TYPE);


			} catch (AlipayApiException alipayApiException) {
				alipayApiException.printStackTrace();
			}
		}
		return responseMsg;
	}


	// FIXME APP支付
	public AlipayTradeAppPayResponse tradeAppPay(AlipayTradeAppPayModel model, String notifyUrl, String format) throws AlipayApiException {
		AlipayClient client = getAlipayClient(format);
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		request.setBizModel(model);
		request.setNotifyUrl(notifyUrl);

		AlipayTradeAppPayResponse response = client.sdkExecute(request);
		return response;
	}

	public AlipayTradeAppPayResponse tradeAppPay(AlipayTradeAppPayModel model, String notifyUrl) throws AlipayApiException {
		return tradeAppPay(model, notifyUrl, "JSON");
	}

	public AlipayTradeAppPayResponse appPayHandleReuslt(Map<String, String> resultMap, String format) throws AlipayApiException {
		AlipayClient client = getAlipayClient(format);
		AlipayTradeAppPayResponse response = client.parseAppSyncResult(resultMap,
				AlipayTradeAppPayRequest.class);
		return response;
	}

	public AlipayTradeAppPayResponse appPayHandleReuslt(Map<String, String> resultMap) throws AlipayApiException {
		return appPayHandleReuslt(resultMap, "JSON");
	}

	// FIXME 移动支付

	/**
	 * 统一收单交易创建接口
	 *
	 * @param model  AlipayTradeCreateModel 对象
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayTradeCreateResponse 对象
	 */
	public AlipayTradeCreateResponse tradeCreate(AlipayTradeCreateModel model, String format) {
		AlipayClient client = getAlipayClient(format);
		AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
		request.setBizModel(model);
		AlipayTradeCreateResponse response = new AlipayTradeCreateResponse();
		try {
			response = client.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 统一收单交易创建接口
	 *
	 * @param model AlipayTradeCreateModel 对象
	 * @return AlipayTradeCreateResponse 对象
	 */
	public AlipayTradeCreateResponse tradeCreate(AlipayTradeCreateModel model) {
		return tradeCreate(model, "JSON");
	}

	/**
	 * 统一收单交易支付接口
	 *
	 * @param model  AlipayTradePayModel 对象
	 * @param format 请求数据格式 默认：JSON（暂时支持JSON，请根据官方API来填写，建议使用不带此参数的方法！）
	 * @return AlipayTradePayResponse 对象
	 */
	public AlipayTradePayResponse tradePay(AlipayTradePayModel model, String format) {
		AlipayClient client = getAlipayClient(format);
		AlipayTradePayRequest request = new AlipayTradePayRequest();
		request.setBizModel(model);
		AlipayTradePayResponse response = new AlipayTradePayResponse();
		try {
			response = client.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 统一收单交易支付接口
	 *
	 * @param model AlipayTradePayModel 对象
	 * @return AlipayTradePayResponse 对象
	 */
	public AlipayTradePayResponse tradePay(AlipayTradePayModel model) {
		return tradePay(model, "JSON");
	}


	// FIXME 私有方法
	private AlipayOpenPublicAccountResetModel getAlipayOpenPublicAccountResetModel(String bindAccountNo, String displayName, String fromUserId, String
			agreementId, String realName, String remark) {
		AlipayOpenPublicAccountResetModel model = new AlipayOpenPublicAccountResetModel();
		try {
			if (null != bindAccountNo && bindAccountNo.trim().length() > 0)
				model.setBindAccountNo(bindAccountNo);
			else
				throw new Exception("重新设置绑定商家会员号时bindAccountNo必须要有的参数！");
			if (null != displayName && displayName.trim().length() > 0)
				model.setDisplayName(displayName);
			else
				throw new Exception("重新设置绑定商家会员号时displayName必须要有的参数！");
			if (null != fromUserId && fromUserId.trim().length() > 0)
				model.setFromUserId(fromUserId);
			else
				throw new Exception("重新设置绑定商家会员号时fromUserId必须要有的参数！");
			if (null != agreementId && agreementId.trim().length() > 0)
				model.setAgreementId(agreementId);
			if (null != realName && realName.trim().length() > 0)
				model.setRealName(realName);
			if (null != remark && remark.trim().length() > 0)
				model.setRemark(remark);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	private AlipayOpenPublicAccountCreateModel getAlipayOpenPublicAccountCreateModel(String bindAccountNo, String displayName, String fromUserId, String
			agreementId, String realName, String remark) {
		AlipayOpenPublicAccountCreateModel model = new AlipayOpenPublicAccountCreateModel();
		try {
			if (null != bindAccountNo && bindAccountNo.trim().length() > 0)
				model.setBindAccountNo(bindAccountNo);
			else
				throw new Exception("添加绑定商户会员号时bindAccountNo必须要有的参数！");
			if (null != displayName && displayName.trim().length() > 0)
				model.setDisplayName(displayName);
			else
				throw new Exception("添加绑定商户会员号时displayName必须要有的参数！");
			if (null != fromUserId && fromUserId.trim().length() > 0)
				model.setFromUserId(fromUserId);
			else
				throw new Exception("添加绑定商户会员号时fromUserId必须要有的参数！");
			if (null != agreementId && agreementId.trim().length() > 0)
				model.setAgreementId(agreementId);
			if (null != realName && realName.trim().length() > 0)
				model.setRealName(realName);
			if (null != remark && remark.trim().length() > 0)
				model.setRemark(remark);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	private AlipayOpenPublicInfoModifyModel getAlipayOpenPublicInfoModifyModel(String appName, String authPic, String licenseUrl, String
			logoUrl, String publicGreeting, List<String> shopPics) {
		AlipayOpenPublicInfoModifyModel model = new AlipayOpenPublicInfoModifyModel();
		if (null != appName && appName.trim().length() > 0)
			model.setAppName(appName);
		if (null != authPic && authPic.trim().length() > 0)
			model.setAuthPic(authPic);
		if (null != licenseUrl && licenseUrl.trim().length() > 0)
			model.setLicenseUrl(licenseUrl);
		if (null != logoUrl && logoUrl.trim().length() > 0)
			model.setLogoUrl(logoUrl);
		if (null != publicGreeting && publicGreeting.trim().length() > 0)
			model.setPublicGreeting(publicGreeting);
		if (null != shopPics && shopPics.size() > 0)
			model.setShopPics(shopPics);
		return model;
	}

	private AlipayOpenPublicQrcodeCreateModel getAlipayOpenPublicQrcodeCreateModel(CodeInfo codeInfo, String codeType, String expireSecond, String
			showLogo) {
		AlipayOpenPublicQrcodeCreateModel model = new AlipayOpenPublicQrcodeCreateModel();
		if (null != codeInfo)
			model.setCodeInfo(codeInfo);
		if (null != codeType && codeType.trim().length() > 0)
			model.setCodeType(codeType);
		if (null != expireSecond && expireSecond.trim().length() > 0)
			model.setExpireSecond(expireSecond);
		if (null != showLogo && showLogo.trim().length() > 0)
			model.setShowLogo(showLogo);
		return model;
	}

	private AlipayOpenPublicQrcodeCreateModel getAlipayOpenPublicQrcodeCreateModel(String gotoUrl, String sceneId, String codeType, String expireSecond, String
			showLogo) {
		CodeInfo codeInfo = null;
		if (null != gotoUrl && gotoUrl.trim().length() > 0) {
			codeInfo = new CodeInfo();
			codeInfo.setGotoUrl(gotoUrl);
		}
		if (null != sceneId && sceneId.trim().length() > 0) {
			if (null == codeInfo)
				codeInfo = new CodeInfo();
			Scene scene = new Scene();
			scene.setSceneId(sceneId);
		}
		return getAlipayOpenPublicQrcodeCreateModel(codeInfo, codeType, expireSecond, showLogo);
	}

	private AlipayOfflineMaterialImageUploadRequest getAlipayOfflineMaterialImageUploadRequest(String imageName, String imageType, FileItem imageContent,
																							   String imagePid) {
		AlipayOfflineMaterialImageUploadRequest request = new AlipayOfflineMaterialImageUploadRequest();
		try {
			if (null != imageName && imageName.trim().length() > 0)
				request.setImageName(imageName);
			else
				throw new Exception("上传门店照片和视频时imageName必要参数！");
			if (null != imageType && imageType.trim().length() > 0)
				request.setImageType(imageType);
			else
				throw new Exception("上传门店照片和视频时imageType必要参数！");
			if (null != imageContent)
				request.setImageContent(imageContent);
			else
				throw new Exception("上传门店照片和视频时imageContent/filePath必要参数！");
			if (null != imagePid && imagePid.trim().length() > 0)
				request.setImagePid(imagePid);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return request;
	}

	private AlipayOfflineMaterialImageUploadRequest getAlipayOfflineMaterialImageUploadRequest(String imageName, String imageType, String filePath, String
			imagePid) {
		FileItem fileItem = null;
		if (null != filePath && filePath.trim().length() > 0) {
			fileItem = new FileItem(filePath);
		}
		return getAlipayOfflineMaterialImageUploadRequest(imageName, imageType, fileItem, imagePid);
	}

	private AlipayClient getAlipayClient(String format) {
		if (null == format || format.trim().length() == 0)
			format = "JSON";
		format = format.trim().toUpperCase();
		if (!format.equals("XML") && !format.equals("JSON"))
			format = "JSON";
		AlipayClient client = this.clientMap.get(format);
		if (null == client) {
			client = AlipayAPIClientFactory.getAlipayClient(ALIPAY_GATEWAY, APP_ID, PRIVATE_KEY, format, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
			this.clientMap.put(format, client);
		}
		return client;
	}

	private Gson getGson() {
		if (null == googleGson)
			googleGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return googleGson;
	}

}
