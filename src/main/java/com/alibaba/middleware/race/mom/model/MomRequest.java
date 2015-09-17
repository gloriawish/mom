package com.alibaba.middleware.race.mom.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by huangsheng.hs on 2015/5/7.
 */
public class MomRequest implements Serializable {
	private static final long serialVersionUID = 5606111910428846773L;
	//一次请求的id
	private String requestId;
	//请求的参数
	private Object parameters;
	
	//消息是从哪里来的
	private RequestResponseFromType fromType;
	
	//请求的类型
	private RequestType requestType;
	
	public RequestType getRequestType() {
		return requestType;
	}
	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}
	public RequestResponseFromType getFromType() {
		return fromType;
	}
	public void setFromType(RequestResponseFromType fromType) {
		this.fromType = fromType;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public Object getParameters() {
		return parameters;
	}
	public void setParameters(Object parameters) {
		this.parameters = parameters;
	}

}

