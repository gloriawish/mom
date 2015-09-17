package com.alibaba.middleware.race.mom.tool;

import java.io.Serializable;

public class testmsg implements Serializable
{
	private static final long serialVersionUID = 1L;
	public String msg;
	public testmsg(String info)
	{
		this.msg=info;
	}
}