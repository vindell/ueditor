package com.baidu.ueditor.provider;

import java.util.Map;

import com.baidu.ueditor.define.State;

public interface FileManagerProvider {

	String getName();
	
	FileManagerProvider config(Map<String, Object> conf);

	State listFile(int start);

}
