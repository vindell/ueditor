package com.baidu.ueditor.provider.def;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

import com.baidu.ueditor.PathFormat;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.FileType;
import com.baidu.ueditor.define.State;
import com.baidu.ueditor.provider.ProviderEnum;
import com.baidu.ueditor.provider.UploadProvider;
import com.baidu.ueditor.utils.StoreUtils;

public class LocalBase64UploadProvider implements UploadProvider {

	@Override
	public String getName() {
		return ProviderEnum.LOCAL_BASE64_UPLOAD.getKey();
	}
	
	@Override
	public State doExec(HttpServletRequest request, Map<String, Object> conf) {
		
		String filedName = (String) conf.get("fieldName");
		String content = request.getParameter(filedName);
		byte[] data = decode(content);

		long maxSize = ((Long) conf.get("maxSize")).longValue();

		if (!validSize(data, maxSize)) {
			return new BaseState(false, AppInfo.MAX_SIZE);
		}

		String suffix = FileType.getSuffix("JPG");

		String savePath = PathFormat.parse((String) conf.get("savePath"), (String) conf.get("filename"));
		
		savePath = savePath + suffix;
		String physicalPath = (String) conf.get("rootPath") + savePath;

		State storageState = StoreUtils.saveBinaryFile(data, physicalPath);

		if (storageState.isSuccess()) {
			storageState.putInfo("url", PathFormat.format(savePath));
			storageState.putInfo("type", suffix);
			storageState.putInfo("original", "");
		}

		return storageState;
	}

	private static byte[] decode(String content) {
		return Base64.decodeBase64(content);
	}

	private static boolean validSize(byte[] data, long length) {
		return data.length <= length;
	}
	
}
