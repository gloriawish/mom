package com.alibaba.middleware.race.mom.broker;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerGroupInfo {
	
	private  int  offset=0;//偏移
	private final String groupId;
	//这个组有订阅哪些topic
	private final ConcurrentHashMap<String/* Topic */, SubscriptionInfo> subscriptionTable =
            new ConcurrentHashMap<String, SubscriptionInfo>();
	
	private final ConcurrentHashMap<String/* client id */, ClientChannelInfo> channelInfoTable =
            new ConcurrentHashMap<String, ClientChannelInfo>(16);
	
	public ConsumerGroupInfo(String groupId)
	{
		this.groupId=groupId;
	}

	public String getGroupId() {
		return groupId;
	}

	public ConcurrentHashMap<String, SubscriptionInfo> getSubscriptionTable() {
		return subscriptionTable;
	}

	public ConcurrentHashMap<String, ClientChannelInfo> getChannelInfoTable() {
		return channelInfoTable;
	}
	//向这个组里添加新的channelinfo
	public void addChannel(String clientId,ClientChannelInfo channelinfo)
	{
		if(findChannel(channelinfo.getClientId())==null)//不存在这个client
		{
			channelInfoTable.put(clientId, channelinfo);
		}
		else
		{
			System.out.println("exists this consumer clientId:"+clientId);
		}
	}
	public void removeChannel(String clientId)
	{
		channelInfoTable.remove(clientId);
	}
	
	public void removeChannel(Channel channel)
	{
		String clientId="";
		Iterator<Map.Entry<String, ClientChannelInfo>> it = this.channelInfoTable.entrySet().iterator();
        while (it.hasNext()) {
        	Map.Entry<String, ClientChannelInfo> next = it.next();
            
        	if(next.getValue().getChannel().equals(channel))
        	{
        		clientId=next.getKey();//获取这个要删除的ID
        		break;
        	}
        if(!clientId.isEmpty())
        	removeChannel(clientId);//删除这个channelinfo
        }
	}
	
	//往这个group中添加订阅信息
	public void addSubscript(SubscriptionInfo subscript)
	{
		//如果之前有相同主题的订阅会被直接覆盖掉
		this.subscriptionTable.put(subscript.getTopic(), subscript);
	}
	
	//根据topic查找订阅信息
	public SubscriptionInfo findSubscriptionData(final String topic) {
        return this.subscriptionTable.get(topic);
    }
	//查找这个group中 clientid对应的信息
	public ClientChannelInfo findChannel(final String clientId) {
        Iterator<Map.Entry<String, ClientChannelInfo>> it = this.channelInfoTable.entrySet().iterator();
        while (it.hasNext()) {
        	Map.Entry<String, ClientChannelInfo> next = it.next();
            if (next.getValue().getClientId().equals(clientId)) {
                return next.getValue();
            }
        }
        return null;
    }
	//获取所有的channel的clientId
	public List<String> getAllChannelId() {
        List<String> result = new ArrayList<String>();

        result.addAll(this.channelInfoTable.keySet());

        return result;
    }
	//获取下一个发送
	public ClientChannelInfo getNextChannelInfo()
	{
		List<ClientChannelInfo> list=new ArrayList<ClientChannelInfo>();
		Iterator<Map.Entry<String, ClientChannelInfo>> it = this.channelInfoTable.entrySet().iterator();
        while (it.hasNext())
        {
        	Map.Entry<String, ClientChannelInfo> next = it.next();
        	list.add(next.getValue());
        }
        int size=list.size();
        int pos=0;
        while(size>0)
        {
        	pos=offset%list.size();
        	offset++;
        	Channel channel=list.get(pos).getChannel();
        	if(channel.isActive()&&channel.isOpen()&&channel.isWritable())
        	{
        		break;
        	}
        	else
        	{
        		size--;
        	}
        }
        return list.get(pos);
	}
}
