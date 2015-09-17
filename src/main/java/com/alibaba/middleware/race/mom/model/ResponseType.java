package com.alibaba.middleware.race.mom.model;

public  enum ResponseType
{
	SendResult,//ACK broker发送给producer的信息类型
	Message,//消息 由broker发送给Consumer的信息类型
	AckSubscript
}
