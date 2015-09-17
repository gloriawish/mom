package com.alibaba.middleware.race.mom.broker;


import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订阅关系管理类
 * @author zz
 *
 */
public class ConsumerManager {
	//存储groupid -> GroupInfo
	private static final ConcurrentHashMap<String/* Group */, ConsumerGroupInfo> consumerTable =
            new ConcurrentHashMap<String, ConsumerGroupInfo>(1024);
	
	//存储每个group的状态的表   0 标识 正常  -1标识 连接断开  1标识超时
	private static final ConcurrentHashMap<String/* Group */, Integer> consumerStat =
            new ConcurrentHashMap<String, Integer>(1024);
	
	
	public static void putConsumerGroupStat(String group,Integer stat)
	{
		consumerStat.put(group, stat);
	}
	public static Integer getConsumerGroupStat(String group)
	{
		return consumerStat.get(group);
	}
	
	public static ConsumerGroupInfo findGroupByGroupID(final String grouId)
	{
		return consumerTable.get(grouId);
	}
	/**
	 * 按照groupid 和 topic和寻找
	 * @param grouId
	 * @param topic
	 * @return
	 */
	public static ConsumerGroupInfo findGroupByGroupID(final String grouId,final String topic)
	{
		ConsumerGroupInfo groupinfo=findGroupByGroupID(grouId);
		//判断这个组有没有订阅这个topic
		if(groupinfo.getSubscriptionTable().containsKey(topic))
		{
			return groupinfo;
		}
		return null;
		
	}
	
	//查找某个topic的订阅组
	public static List<ConsumerGroupInfo> findGroupByTopic(final String topic)
	{
		List<ConsumerGroupInfo> groups = new ArrayList<ConsumerGroupInfo>();
        Iterator<Map.Entry<String, ConsumerGroupInfo>> it = consumerTable.entrySet().iterator();
        while (it.hasNext()) 
        {
            Map.Entry<String, ConsumerGroupInfo> entry = it.next();
            ConcurrentHashMap<String, SubscriptionInfo> subscriptionTable = entry.getValue().getSubscriptionTable();
            //这个group 订阅了topic就返回
            if (subscriptionTable.containsKey(topic)) {
                groups.add(entry.getValue());
            }
        }
        return groups;
	}
	//查找订阅topic且过滤条件一致的组
	public static List<ConsumerGroupInfo> findGroupByTopic(final String topic,final String property,final String value)
	{
		List<ConsumerGroupInfo> groups = new ArrayList<ConsumerGroupInfo>();
        Iterator<Map.Entry<String, ConsumerGroupInfo>> it = consumerTable.entrySet().iterator();
        while (it.hasNext()) 
        {
            Map.Entry<String, ConsumerGroupInfo> entry = it.next();
            ConcurrentHashMap<String, SubscriptionInfo> subscriptionTable = entry.getValue().getSubscriptionTable();
            //这个group 订阅了topic并且过滤条件一致就返回
            if (subscriptionTable.containsKey(topic)) 
            {
            	//判断过滤条件是否一致，当过滤条件为空的时候 就是符合的
            	if(subscriptionTable.get(topic).getFitlerName()==property&&subscriptionTable.get(topic).getFitlerValue()==value)
            		groups.add(entry.getValue());
            	//如果该group 的过滤条件为空，则认为也是符合匹配的
            	else if(subscriptionTable.get(topic).getFitlerName().isEmpty()&&subscriptionTable.get(topic).getFitlerValue().isEmpty())
            	{
            		groups.add(entry.getValue());
            	}
            		
            }
        }
        return groups;
	}
	//通过clientId找到它所属的组
	public static ConsumerGroupInfo findGroupByClientID(final String clientID)
	{
		 Iterator<Map.Entry<String, ConsumerGroupInfo>> it = consumerTable.entrySet().iterator();
         while (it.hasNext()) 
         {
            Map.Entry<String, ConsumerGroupInfo> next = it.next();
            ConsumerGroupInfo info = next.getValue();
            if(info.findChannel(clientID)!=null)
            	return info;
         }
         return null;
	}
	//通过组id查找指定clientId的channelinfo
	public static ClientChannelInfo findChannelInfoByID(final String group,final String clientId)
	{
		ConsumerGroupInfo consumerGroupInfo = consumerTable.get(group);
        if (consumerGroupInfo != null) {
            return consumerGroupInfo.findChannel(clientId);
        }

        return null;
	}
	//添加一个channelinfo进入某个组，如果租不存在则新建
	public static void addGroupInfo(String group,ClientChannelInfo channelinfo)
	{
		if(findGroupByGroupID(group)==null)//不存在这个组
		{
			ConsumerGroupInfo newgroup=new ConsumerGroupInfo(group);
			newgroup.addChannel(channelinfo.getClientId(), channelinfo);
			//增加新的订阅信息
			newgroup.addSubscript(channelinfo.getSubcript());
			
			consumerTable.put(group, newgroup);//加入一个新的组
			
			System.out.println("create group:"+group+" topic:"+channelinfo.getSubcript().getTopic());
		}
		else if(findGroupByGroupID(group).findChannel(channelinfo.getClientId())!=null)
		{
			//之前存在这个clientId  说明是重新连接上的
			findGroupByGroupID(group).removeChannel(channelinfo.getClientId());
			//把之前关联的旧的channel删除
			findGroupByGroupID(group).addChannel(channelinfo.getClientId(), channelinfo);
			
			System.out.println("reconneted from consumer");
		}
		else
		{
			
			try {
				String filterName=channelinfo.getSubcript().getFitlerName();
				String topic=channelinfo.getSubcript().getTopic();
				
				//这个组已经存在，判断下订阅的主题和 过滤条件是否一致，实际上基本上都是一致的，因为consumer在stop时候
				//会发送消息从这个组里删除自己
				ConsumerGroupInfo groupInfo=findGroupByGroupID(group);
				
				if(groupInfo.getSubscriptionTable().containsKey(topic))
				{
					SubscriptionInfo sub=groupInfo.findSubscriptionData(topic);
					if(filterName==null)
					{
						if(sub.getFitlerName()==null)
						{
							//添加进入组
							findGroupByGroupID(group).addChannel(channelinfo.getClientId(), channelinfo);
							System.out.println("add into group 1");
						}
						else
						{
							//清除之前的组员  在添加进入
							
							System.out.println("update subcript 1");
							//如果加入的是 新的订阅   之前的就无效了
							findGroupByGroupID(group).getSubscriptionTable().clear();
							findGroupByGroupID(group).addSubscript(channelinfo.getSubcript());
							
							findGroupByGroupID(group).getChannelInfoTable().clear();
							findGroupByGroupID(group).addChannel(channelinfo.getClientId(), channelinfo);
						}
					}
					else
					{
						if(sub.getFitlerName()!=null&&sub.getFitlerName().equals(filterName))
						{
							//添加进入组
							findGroupByGroupID(group).addChannel(channelinfo.getClientId(), channelinfo);
							System.out.println("add into group 2");
						}
						else
						{
							//清除之前的组员 在添加进入
							
							System.out.println("update subcript 2");
							//如果加入的是 新的订阅   之前的就无效了
							findGroupByGroupID(group).getSubscriptionTable().clear();
							findGroupByGroupID(group).addSubscript(channelinfo.getSubcript());
							
							findGroupByGroupID(group).getChannelInfoTable().clear();
							findGroupByGroupID(group).addChannel(channelinfo.getClientId(), channelinfo);
						}
					}
				}
				else
				{
					//清除之前的组员 在添加进入
					
					System.out.println("update subcript 3");
					//如果加入的是 新的订阅   之前的就无效了
					findGroupByGroupID(group).getSubscriptionTable().clear();
					findGroupByGroupID(group).addSubscript(channelinfo.getSubcript());
					
					findGroupByGroupID(group).getChannelInfoTable().clear();
					findGroupByGroupID(group).addChannel(channelinfo.getClientId(), channelinfo);
				}
				
//				if(filterName==null
//						||filterName.length()==0
//						||!findGroupByGroupID(group).getSubscriptionTable().containsKey(channelinfo.getSubcript().getTopic())
//						||!findGroupByGroupID(group).findSubscriptionData(channelinfo.getSubcript().getTopic()).getFitlerName().equals(filterName))
//				{
//					System.out.println("update subcript");
//					//如果加入的是 新的订阅   之前的就无效了
//					findGroupByGroupID(group).getSubscriptionTable().clear();
//					findGroupByGroupID(group).addSubscript(channelinfo.getSubcript());
//					
//					findGroupByGroupID(group).getChannelInfoTable().clear();
//					findGroupByGroupID(group).addChannel(channelinfo.getClientId(), channelinfo);
//				}
//				else
//				{
//					findGroupByGroupID(group).addChannel(channelinfo.getClientId(), channelinfo);
//					System.out.println("add into group");
//				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			
			
			
		}
	}
	
	 //获取某个组某个主题的订阅信息
	 public static SubscriptionInfo findSubscriptionInfo(final String group, final String topic) {
	        ConsumerGroupInfo consumerGroupInfo = findGroupByGroupID(group);
	        if (consumerGroupInfo != null) {
	            return consumerGroupInfo.findSubscriptionData(topic);
	        }
	        return null;
	 }
	 
	 //当消费者与broker断开连接时
	 public static void consumerDisconnect(Channel channel)
	 {
		 Iterator<Map.Entry<String, ConsumerGroupInfo>> it = consumerTable.entrySet().iterator();
         while (it.hasNext()) 
         {
            Map.Entry<String, ConsumerGroupInfo> next = it.next();
            ConsumerGroupInfo info = next.getValue();

            info.removeChannel(channel);//移除对应的订阅这
         }
	 }
	 
	 
	 public static void stopConsumer(String clientId)
	 {
		 Iterator<Map.Entry<String, ConsumerGroupInfo>> it = consumerTable.entrySet().iterator();
         while (it.hasNext()) 
         {
            Map.Entry<String, ConsumerGroupInfo> next = it.next();
            
            String groupId=next.getKey();
            ConsumerGroupInfo info = next.getValue();
            
            if(info.findChannel(clientId)!=null)
            {
            	info.removeChannel(clientId);
            	System.out.println(clientId+" stop");
            }
            
            if(info.getChannelInfoTable().size()==0)//如果这个group里面没有剩余成员了，则就删除组
            {
            	consumerTable.remove(groupId);
            	System.out.println("remove group:"+groupId);
            }
         }
	 }
}
