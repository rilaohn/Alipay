package com.alipay.domain;

import com.alipay.api.AlipayObject;
import com.alipay.api.domain.Article;
import com.alipay.api.domain.Text;

import java.util.List;

public class AlipayImageTextMessage extends AlipayObject {
	private static final long serialVersionUID = 2165138117969154872L;
	private List<Article> articles;
	private Text text;
	private String msg_type;
	private String to_user_id;
	private String chat;

	public List<Article> getArticles() {
		return articles;
	}

	public void setArticles(List<Article> articles) {
		this.articles = articles;
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public String getMsg_type() {
		return msg_type;
	}

	public void setMsg_type(String msg_type) {
		this.msg_type = msg_type;
	}

	public String getTo_user_id() {
		return to_user_id;
	}

	public void setTo_user_id(String to_user_id) {
		this.to_user_id = to_user_id;
	}

	public String getChat() {
		return chat;
	}

	public void setChat(String chat) {
		this.chat = chat;
	}
}
