package com.alibaba.middleware.race.mom.model;

public interface InvokeListener<T> {

	void onResponse(T t);
	
}