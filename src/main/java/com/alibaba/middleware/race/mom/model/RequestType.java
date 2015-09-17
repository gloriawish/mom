package com.alibaba.middleware.race.mom.model;

public enum RequestType
{
	Message,//Producer 发送消息
	ConsumeResult,//Consumer 消费消息的结果
	Subscript,//Consumer 订阅消息的请求
	Stop,//退订消息
}
