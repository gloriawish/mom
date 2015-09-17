package com.alibaba.middleware.race.mom.model;

/**
 * 订阅请求信息
 * @author sei.zz
 *
 */
public class SubscriptRequestInfo {
	
	private String groupId;
	private String topic;
	private String propertieName;
	private String propertieValue;
	private String clientKey;
		
	public String getClientKey() {
		return clientKey;
	}
	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}
	public SubscriptRequestInfo()
	{
		
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getPropertieName() {
		return propertieName;
	}
	public void setPropertieName(String propertieName) {
		this.propertieName = propertieName;
	}
	public String getPropertieValue() {
		return propertieValue;
	}
	public void setPropertieValue(String propertieValue) {
		this.propertieValue = propertieValue;
	}
	
}
