package com.baidu.ueditor.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.baidu.ueditor.provider.def.LocalBase64UploadProvider;
import com.baidu.ueditor.provider.def.LocalBinaryUploadProvider;
import com.baidu.ueditor.provider.def.LocalFileManagerProvider;
import com.baidu.ueditor.provider.def.LocalImageHunterProvider;

public class ProviderManager {

	
	protected static ConcurrentMap<String, UploadProvider> COMPLIED_UPLOAD_PROVIDER = new ConcurrentHashMap<String, UploadProvider>();
	protected static ConcurrentMap<String, FileManagerProvider> COMPLIED_FILEMANAGER_PROVIDER = new ConcurrentHashMap<String, FileManagerProvider>();
	protected static ConcurrentMap<String, ImageHunterProvider> COMPLIED_IMAGEHUNTER_PROVIDER = new ConcurrentHashMap<String, ImageHunterProvider>();
	
	static {
		
		COMPLIED_UPLOAD_PROVIDER.put(ProviderEnum.LOCAL_BASE64_UPLOAD.getKey(), new LocalBase64UploadProvider());
		COMPLIED_UPLOAD_PROVIDER.put(ProviderEnum.LOCAL_BINARY_UPLOAD.getKey(), new LocalBinaryUploadProvider());

		COMPLIED_FILEMANAGER_PROVIDER.put(ProviderEnum.LOCAL_FILE_MANAGER.getKey(), new LocalFileManagerProvider());
		COMPLIED_IMAGEHUNTER_PROVIDER.put(ProviderEnum.LOCAL_IMAGE_HUNTER.getKey(), new LocalImageHunterProvider());
		
	}

	/**
	 * 注册上传接口提供者
	 * @param provider
	 */
	public static void register(UploadProvider provider) {
		if (provider != null ) {
			COMPLIED_UPLOAD_PROVIDER.putIfAbsent( provider.getName(), provider);
		}
	}

	public static UploadProvider getUploadProvider(String provider) {
		if (provider != null) {
			UploadProvider ret = COMPLIED_UPLOAD_PROVIDER.get(provider);
			if (ret != null) {
				return ret;
			}
		}
		return COMPLIED_UPLOAD_PROVIDER.get(ProviderEnum.LOCAL_BINARY_UPLOAD.getKey());
	}

	public static FileManagerProvider getFileManagerProvider(String provider) {
		if (provider != null) {
			FileManagerProvider ret = COMPLIED_FILEMANAGER_PROVIDER.get(provider);
			if (ret != null) {
				return ret;
			}
		}
		return COMPLIED_FILEMANAGER_PROVIDER.get(ProviderEnum.LOCAL_FILE_MANAGER.getKey());
	}
	
	public static ImageHunterProvider getImageHunterProvider(String provider) {
		if (provider != null) {
			ImageHunterProvider ret = COMPLIED_IMAGEHUNTER_PROVIDER.get(provider);
			if (ret != null) {
				return ret;
			}
		}
		return COMPLIED_IMAGEHUNTER_PROVIDER.get(ProviderEnum.LOCAL_IMAGE_HUNTER.getKey());
	}

}

