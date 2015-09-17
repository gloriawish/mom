package com.alibaba.middleware.race.mom.model;

import java.io.Serializable;

import com.alibaba.middleware.race.mom.Message;


/**
 * 一次发送任务
 * @author zz
 *
 */
public class SendTask implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4242238019951149280L;

	private String groupId;
	
	private String topic;
	
	private Message message;
	
	@Override
	public boolean equals(Object obj)
	{
    	if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SendTask other = (SendTask) obj;
    	
        if(groupId==null)
        {
        	if(other.getGroupId()!=null)
        		return false;
        } else {
        	if(other.getGroupId()==null)
        		return false;
        	else if(!groupId.equals(other.getGroupId()))
        		return false;
        }
        
        if(topic==null)
        {
        	if(other.getTopic()!=null)
        		return false;
        } else {
        	if(other.getTopic()==null)
        		return false;
        	else if (!topic.equals(other.getTopic())) {
				return false;
			}
        }
        
        if(message==null) {
        	if(other.getMessage()!=null)
        		return false;
        } else {
        	if(other.getMessage()==null)
        		return false;
        	else if(!message.getMsgId().equals(other.getMessage().getMsgId()))
        		return false;
        }
        
		return true;
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

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

}
