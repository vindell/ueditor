package com.baidu.ueditor.provider;

public enum ProviderEnum {

	FTP_BINARY_UPLOAD("ftp:binary-upload"),
	FTP_FILE_MANAGER("ftp:file-manager"),
	FTP_IMAGE_HUNTER("ftp:image-hunter");
	
	protected String key;
	
	private ProviderEnum(String key){
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
}

