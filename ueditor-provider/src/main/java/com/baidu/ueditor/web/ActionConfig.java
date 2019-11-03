package com.baidu.ueditor.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.ueditor.define.ActionMap;
import com.baidu.ueditor.provider.ProviderEnum;
import com.baidu.ueditor.utils.StringUtils;

public final class ActionConfig {

	protected String rootPath = null;
	protected JSONObject jsonConfig = null;
	// 涂鸦上传filename定义
	protected final static String SCRAWL_FILE_NAME = "scrawl";
	// 远程图片抓取filename定义
	protected final static String REMOTE_FILE_NAME = "remote";
	
	/*
	 * 通过一个给定的路径构建一个配置管理器， 该管理器要求地址路径所在目录下必须存在config.properties文件
	 */
	private ActionConfig (ServletConfig servletConfig,JSONObject config) throws FileNotFoundException, IOException {
		this.rootPath = servletConfig.getServletContext().getRealPath(File.separator);
		this.jsonConfig = config;
	}
	
	public static ActionConfig getInstance (ServletConfig servletConfig,JSONObject config) {
		try {
			return new ActionConfig(servletConfig,config);
		} catch ( Exception e ) {
			return null;
		}
		
	}
	
	// 验证配置文件加载是否正确
	public boolean valid () {
		return this.jsonConfig != null;
	}
	
	public JSONObject getAllConfig () {
		return this.jsonConfig;
	}
	
	public Map<String, Object> getConfig ( int type ) {
		
		Map<String, Object> conf = new HashMap<String, Object>();
		String savePath = null;
		
		switch ( type ) {
		
			case ActionMap.UPLOAD_FILE:
				conf.put( "isBase64", "false" );
				conf.put( "maxSize", this.jsonConfig.getLong( "fileMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "fileAllowFiles" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "fileFieldName" ) );
				savePath = this.jsonConfig.getString( "filePathFormat" );
				break;
				
			case ActionMap.UPLOAD_IMAGE:
				conf.put( "isBase64", "false" );
				conf.put( "maxSize", this.jsonConfig.getLong( "imageMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "imageAllowFiles" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "imageFieldName" ) );
				savePath = this.jsonConfig.getString( "imagePathFormat" );
				break;
				
			case ActionMap.UPLOAD_VIDEO:
				conf.put( "maxSize", this.jsonConfig.getLong( "videoMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "videoAllowFiles" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "videoFieldName" ) );
				savePath = this.jsonConfig.getString( "videoPathFormat" );
				break;
				
			case ActionMap.UPLOAD_SCRAWL:
				conf.put( "filename", ActionConfig.SCRAWL_FILE_NAME );
				conf.put( "maxSize", this.jsonConfig.getLong( "scrawlMaxSize" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "scrawlFieldName" ) );
				conf.put( "isBase64", "true" );
				savePath = this.jsonConfig.getString( "scrawlPathFormat" );
				break;
				
			case ActionMap.CATCH_IMAGE:
				conf.put( "filename", ActionConfig.REMOTE_FILE_NAME );
				conf.put( "filter", this.getArray( "catcherLocalDomain" ) );
				conf.put( "maxSize", this.jsonConfig.getLong( "catcherMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "catcherAllowFiles" ) );
				conf.put( "fieldName", this.jsonConfig.getString( "catcherFieldName" ) + "[]" );
				savePath = this.jsonConfig.getString( "catcherPathFormat" );
				break;
				
			case ActionMap.LIST_IMAGE:
				conf.put( "allowFiles", this.getArray( "imageManagerAllowFiles" ) );
				conf.put( "dir", this.jsonConfig.getString( "imageManagerListPath" ) );
				conf.put( "count", this.jsonConfig.getIntValue("imageManagerListSize" ) );
				break;
				
			case ActionMap.LIST_FILE:
				conf.put( "allowFiles", this.getArray( "fileManagerAllowFiles" ) );
				conf.put( "dir", this.jsonConfig.getString( "fileManagerListPath" ) );
				conf.put( "count", this.jsonConfig.getIntValue( "fileManagerListSize" ) );
				break;
				
		}
		
		conf.put("savePath", savePath );
		conf.put("rootPath", this.rootPath );
		
		//扩展外部存储服务参数
	    conf.put("upload_provider", StringUtils.getSafeStr(this.jsonConfig.getString("upload_provider"), ProviderEnum.LOCAL_BINARY_UPLOAD.getKey()));
	    conf.put("file_manager_provider", StringUtils.getSafeStr(this.jsonConfig.getString("file_manager_provider"), ProviderEnum.LOCAL_FILE_MANAGER.getKey()));  
	    conf.put("image_hunter_provider", StringUtils.getSafeStr(this.jsonConfig.getString("image_hunter_provider"), ProviderEnum.LOCAL_IMAGE_HUNTER.getKey())); 
	    conf.put("keeplocal", StringUtils.getSafeBoolean(this.jsonConfig.getString("keeplocal"), "true" ));  
		
		return conf;
		
	}
	
	public String[] getArray ( String key ) {
		JSONArray jsonArray = this.jsonConfig.getJSONArray( key );
		String[] result = new String[ jsonArray.size() ];
		for ( int i = 0, len = jsonArray.size(); i < len; i++ ) {
			result[i] = jsonArray.getString( i );
		}
		return result;
	}
	
}
