package com.baidu.ueditor.provider;

public enum ProviderEnum {

	LOCAL_BASE64_UPLOAD("local:base64-upload"),
	LOCAL_BINARY_UPLOAD("local:binary-upload"),
	LOCAL_FILE_MANAGER("local:file-manager"),
	LOCAL_IMAGE_HUNTER("local:image-hunter");
	
	protected String key;
	
	private ProviderEnum(String key){
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
}
