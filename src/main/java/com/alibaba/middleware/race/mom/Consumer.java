package com.alibaba.middleware.race.mom;

public abstract interface Consumer
{
  public abstract void start();

  public abstract void subscribe(String paramString1, String paramString2, MessageListener paramMessageListener);

  public abstract void setGroupId(String paramString);

  public abstract void stop();
}

/* Location:           C:\Users\sei.zz\Desktop\高性能中间件\消息队列\middleware-mom-1.0.jar
 * Qualified Name:     com.alibaba.middleware.race.mom.Consumer
 * JD-Core Version:    0.6.1
 */