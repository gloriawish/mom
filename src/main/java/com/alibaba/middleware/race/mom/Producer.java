package com.alibaba.middleware.race.mom;

public abstract interface Producer
{
  public abstract void start();

  public abstract void setTopic(String paramString);

  public abstract void setGroupId(String paramString);

  public abstract SendResult sendMessage(Message paramMessage);

  public abstract void asyncSendMessage(Message paramMessage, SendCallback paramSendCallback);

  public abstract void stop();
}

/* Location:           C:\Users\sei.zz\Desktop\高性能中间件\消息队列\middleware-mom-1.0.jar
 * Qualified Name:     com.alibaba.middleware.race.mom.Producer
 * JD-Core Version:    0.6.1
 */