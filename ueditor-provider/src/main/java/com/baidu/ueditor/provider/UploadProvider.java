package com.baidu.ueditor.provider;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.baidu.ueditor.define.State;

public interface UploadProvider {

	String getName();
	
	State doExec(HttpServletRequest request, Map<String, Object> conf);
			
}
