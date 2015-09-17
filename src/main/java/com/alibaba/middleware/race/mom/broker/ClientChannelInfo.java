package com.alibaba.middleware.race.mom.broker;

import io.netty.channel.Channel;

public class ClientChannelInfo {
	
	//关联的channel
	private final Channel channel;
	//表示客户端的id
	private final String clientId;
	//该客户端的订阅信息
	private SubscriptionInfo subcript;
	public SubscriptionInfo getSubcript() {
		return subcript;
	}
	public void setSubcript(SubscriptionInfo subcript) {
		this.subcript = subcript;
	}
	public Channel getChannel() {
		return channel;
	}
	public String getClientId() {
		return clientId;
	}
    public ClientChannelInfo(Channel channel, String clientId)
    {
        this.channel = channel;
        this.clientId = clientId;
    }
}
