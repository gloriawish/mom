package com.alibaba.middleware.race.mom;

public abstract interface MessageListener
{
  public abstract ConsumeResult onMessage(Message paramMessage);
}

/* Location:           C:\Users\sei.zz\Desktop\高性能中间件\消息队列\middleware-mom-1.0.jar
 * Qualified Name:     com.alibaba.middleware.race.mom.MessageListener
 * JD-Core Version:    0.6.1
 */