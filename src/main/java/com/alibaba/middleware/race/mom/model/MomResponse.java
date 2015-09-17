package com.alibaba.middleware.race.mom.model;

import java.io.Serializable;

/**
 * Created by huangsheng.hs on 2015/3/27.
 */
public class MomResponse implements Serializable{
    static private final long serialVersionUID = -4364536436151723421L;
    private String requestId;
    private Object response;
    //消息来源
    private RequestResponseFromType fromType;
    //响应类型
    private ResponseType responseType;
	public RequestResponseFromType getFromType() {
		return fromType;
	}
	public void setFromType(RequestResponseFromType fromType) {
		this.fromType = fromType;
	}
	public ResponseType getResponseType() {
		return responseType;
	}
	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public Object getResponse() {
		return response;
	}
	public void setResponse(Object response) {
		this.response = response;
	}
    
}
