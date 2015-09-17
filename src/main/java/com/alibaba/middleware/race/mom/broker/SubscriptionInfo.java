package com.alibaba.middleware.race.mom.broker;

/**
 * 订阅信息
 * @author zz
 *
 */
public class SubscriptionInfo {
	//主题
	private String topic;
	private String subString;
	//属性过滤
	private String fitlerName;
	//值
	private String fitlerValue;
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getSubString() {
		return subString;
	}
	public void setSubString(String subString) {
		this.subString = subString;
	}
	public String getFitlerName() {
		return fitlerName;
	}
	public void setFitlerName(String fitlerName) {
		this.fitlerName = fitlerName;
	}
	public String getFitlerValue() {
		return fitlerValue;
	}
	public void setFitlerValue(String fitlerValue) {
		this.fitlerValue = fitlerValue;
	}
	
}
