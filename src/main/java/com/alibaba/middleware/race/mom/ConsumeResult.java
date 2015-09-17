/*    */ package com.alibaba.middleware.race.mom;
/*    */ 
/*    */ public class ConsumeResult
/*    */ {
/*  4 */   private ConsumeStatus status = ConsumeStatus.FAIL;
/*    */   private String info;
/*    */   private String groupID;
		   private String topic;
		   private String msgId;
		   
		   public String getMsgId() {
			return msgId;
		}
		public void setMsgId(String msgId) {
			this.msgId = msgId;
		}
		public String getGroupID() {
			   return groupID;
		   }
		   public void setGroupID(String groupID) {
			   this.groupID = groupID;
		   }
		   public String getTopic() {
			   return topic;
		   }
		   public void setTopic(String topic) {
			   this.topic = topic;
		   }
/*    */   public void setStatus(ConsumeStatus status)
/*    */   {
/*  7 */     this.status = status;
/*    */   }
/*    */   public ConsumeStatus getStatus() {
/* 10 */     return this.status;
/*    */   }
/*    */   public void setInfo(String info) {
/* 13 */     this.info = info;
/*    */   }
/*    */   public String getInfo() {
/* 16 */     return this.info;
/*    */   }
/*    */ }

/* Location:           C:\Users\sei.zz\Desktop\高性能中间件\消息队列\middleware-mom-1.0.jar
 * Qualified Name:     com.alibaba.middleware.race.mom.ConsumeResult
 * JD-Core Version:    0.6.1
 */