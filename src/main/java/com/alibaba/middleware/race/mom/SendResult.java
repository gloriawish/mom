/*    */ package com.alibaba.middleware.race.mom;
/*    */ 
/*    */ public class SendResult
/*    */ {
/*    */   private String info;
/*    */   private SendStatus status;
/*    */   private String msgId;
/*    */ 
/*    */   public String getInfo()
/*    */   {
/*  5 */     return this.info;
/*    */   }
/*    */   public void setInfo(String info) {
/*  8 */     this.info = info;
/*    */   }
/*    */   public SendStatus getStatus() {
/* 11 */     return this.status;
/*    */   }
/*    */   public void setStatus(SendStatus status) {
/* 14 */     this.status = status;
/*    */   }
/*    */   public String getMsgId() {
/* 17 */     return this.msgId;
/*    */   }
/*    */   public void setMsgId(String msgId) {
/* 20 */     this.msgId = msgId;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 27 */     return new StringBuilder().append("msg ").append(this.msgId).append("  send ").append(this.status == SendStatus.SUCCESS ? "success" : "fail").append("   info:").append(this.info).toString();
/*    */   }
/*    */ }

/* Location:           C:\Users\sei.zz\Desktop\高性能中间件\消息队列\middleware-mom-1.0.jar
 * Qualified Name:     com.alibaba.middleware.race.mom.SendResult
 * JD-Core Version:    0.6.1
 */