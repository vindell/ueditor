package com.baidu.ueditor.provider;

import java.util.Map;

import com.baidu.ueditor.define.State;

public interface ImageHunterProvider {
	
	String getName();
	
	ImageHunterProvider config(Map<String, Object> conf);
	
	State capture (String[] list);
	
}
