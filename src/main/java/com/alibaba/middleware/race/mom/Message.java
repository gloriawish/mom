/*    */ package com.alibaba.middleware.race.mom;
/*    */ 
/*    */ import java.io.Serializable;
/*    */ import java.util.HashMap;
import java.util.Map;

/*    */ 
/*    */ public class Message
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 5295808332504208830L;
/*    */   private String topic;
/*    */   private byte[] body;
/*    */   private String msgId;
/*    */   private long bornTime;
/* 18 */   private Map<String, String> properties = new HashMap();
/*    */   public Map<String,String> getProperties()
		   {
			 return properties;
		   }
			
public void setProperties(Map<String, String> properties) {
	this.properties = properties;
}

/*    */   public void setTopic(String topic) {
/* 21 */     this.topic = topic;
/*    */   }
/*    */   public String getMsgId() {
/* 24 */     return this.msgId;
/*    */   }
/*    */   public void setMsgId(String msgId) {
/* 27 */     this.msgId = msgId;
/*    */   }
/*    */   public String getTopic() {
/* 30 */     return this.topic;
/*    */   }
/*    */ 
/*    */   public void setBody(byte[] body) {
/* 34 */     this.body = body;
/*    */   }
/*    */ 
/*    */   public byte[] getBody() {
/* 38 */     return this.body;
/*    */   }
/*    */ 
/*    */   public String getProperty(String key) {
/* 42 */     return (String)this.properties.get(key);
/*    */   }
/*    */ 
/*    */   public void setProperty(String key, String value)
/*    */   {
/* 50 */     this.properties.put(key, value);
/*    */   }
/*    */ 
/*    */   public void removeProperty(String key)
/*    */   {
/* 57 */     this.properties.remove(key);
/*    */   }
/*    */   public long getBornTime() {
/* 60 */     return this.bornTime;
/*    */   }
/*    */   public void setBornTime(long bornTime) {
/* 63 */     this.bornTime = bornTime;
/*    */   }
            @Override
			public boolean equals(Object obj)
			{
            	if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                Message other = (Message) obj;
            	
                if(topic==null)
                {
                	if(other.getTopic()!=null)
                		return false;
                }
                else
                {
                	if(other.getTopic()==null)
                		return false;
                	else if(!topic.equals(other.getTopic()))
                		return false;
                }
                
                if(body==null)
                {
                	if(other.getBody()!=null)
                		return false;
                }
                else
                {
                	if(other.getBody()==null)
                		return false;
                	if(body.length!=other.getBody().length)
                		return false;
                	else
                		for (int i = 0; i < body.length; i++) {
							if(body[i]!=other.getBody()[i])
								return false;
						}
                }
                
                if(properties==null)
                {
                	if(other.getProperties()!=null)
                		return false;
                }
                else
                {
                	if(other.getProperties()==null)
                		return false;
                	else if(!properties.equals(other.getProperties()))
                		return false;
                }
                
				return true;
			}
            @Override
            public int hashCode()
            {
            	int result=topic.hashCode();
            	result+=properties.hashCode();
            	if(body!=null)
	            	for (int i = 0; i < body.length; i++) {
						result+=body[i];
					}
            	return result;
            }
            
/*    */ }

/* Location:           C:\Users\sei.zz\Desktop\高性能中间件\消息队列\middleware-mom-1.0.jar
 * Qualified Name:     com.alibaba.middleware.race.mom.Message
 * JD-Core Version:    0.6.1
 */