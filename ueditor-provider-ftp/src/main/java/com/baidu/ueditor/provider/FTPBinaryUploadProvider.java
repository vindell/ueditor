package com.baidu.ueditor.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.baidu.ueditor.PathFormat;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.FileType;
import com.baidu.ueditor.define.State;
import com.baidu.ueditor.utils.StoreUtils;

public class FTPBinaryUploadProvider implements UploadProvider {

	public static final int BUFFER_SIZE = 8192;
	protected FTPClient ftpClient;
	
	@Override
	public String getName() {
		return ProviderEnum.FTP_BINARY_UPLOAD.getKey();
	}

	@Override
	public State doExec(HttpServletRequest request, Map<String, Object> conf) {

		FileItemStream fileStream = null;
		boolean isAjaxUpload = request.getHeader("X_Requested_With") != null;

		if (!ServletFileUpload.isMultipartContent(request)) {
			return new BaseState(false, 5);
		}

		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

		if (isAjaxUpload) {
			upload.setHeaderEncoding("UTF-8");
		}
		try {
			FileItemIterator iterator = upload.getItemIterator(request);

			while (iterator.hasNext()) {
				
				fileStream = iterator.next();

				if (!fileStream.isFormField()){
					break;
				}
				fileStream = null;
			}

			if (fileStream == null) {
				return new BaseState(false, AppInfo.NOTFOUND_UPLOAD_DATA );
			}

			String savePath = (String) conf.get("savePath");
			String originFileName = fileStream.getName();
			String suffix = FileType.getSuffixByFilename(originFileName);

			originFileName = originFileName.substring(0, originFileName.length() - suffix.length());
			savePath = savePath + suffix;

			long maxSize = ((Long) conf.get("maxSize")).longValue();

			if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
				return new BaseState(false, 8);
			}

			savePath = PathFormat.parse(savePath, originFileName);

			String remoteDir = "";

			int pos = savePath.lastIndexOf("/");
			if (pos > -1) {
				remoteDir = savePath.substring(0, pos + 1);
			}

			String physicalPath = (String) conf.get("rootPath") + savePath;

			boolean keepLocalFile = "false".equals(conf.get("keepLocalFile")) ? false : true;
			InputStream is = fileStream.openStream();
			State storageState = saveFtpFileByInputStream(is, remoteDir, physicalPath, maxSize, keepLocalFile);
			is.close();

			if (storageState.isSuccess()) {
				// storageState.putInfo("url", savePath); 这里返回地址为了返回的是，ftp服务器上的地址，所以这里注释掉
				storageState.putInfo("type", suffix);
				storageState.putInfo("original", originFileName + suffix);
			}

			return storageState;
		} catch (FileUploadException e) {
			return new BaseState(false, 6);
		} catch (IOException localIOException) {
		}
		return new BaseState(false, 4);
	}

	@SuppressWarnings("rawtypes")
	protected boolean validType(String type, String[] allowTypes) {
		List list = Arrays.asList(allowTypes);
		return list.contains(type);
	}

	/**
	 * 上传FTP文件
	 * @param is
	 * @param path
	 * @param maxSize
	 * @return
	 */
	public static State saveFtpFileByInputStream(InputStream is,String remoteDir, String path, long maxSize, boolean keepLocalFile) {
		State state = null;

		File tmpFile = StoreUtils.getTmpFile();

		byte[] dataBuf = new byte[2048];
		BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile), BUFFER_SIZE);

			int count = 0;
			while ((count = bis.read(dataBuf)) != -1) {
				bos.write(dataBuf, 0, count);
			}
			bos.flush();
			bos.close();

			if (tmpFile.length() > maxSize) {
				tmpFile.delete();
				return new BaseState(false, AppInfo.MAX_SIZE);
			}

			state = saveFtpTmpFile(tmpFile, remoteDir, path, keepLocalFile);

			if (!state.isSuccess()) {
				tmpFile.delete();
			}

			return state;
		} catch (IOException localIOException) {
		}
		return new BaseState(false, AppInfo.IO_ERROR);
	}

	private static State saveFtpTmpFile(File tmpFile, String remoteDir,
			String path, boolean keepLocalFile) {
		State state = null;
		File targetFile = new File(path);

		if (targetFile.canWrite())
			return new BaseState(false, AppInfo.PERMISSION_DENIED );
		try {
			FileUtils.moveFile(tmpFile, targetFile);
		} catch (IOException e) {
			return new BaseState(false, AppInfo.IO_ERROR );
		}
		// 这里增加自己的上传到ftp服务器的代码</span>
		double d = Math.random();
		String fileName = UUIDGenerator.getUUID() + d + ".jpg";
		String ftpPath;
		try {
			File file = targetFile;
			if (file != null) {
				FileUtil.ftpUpload(file, fileName, "/news/ueditor");
			}
			ftpPath = picturePath + "ueditor/" + fileName;
		} catch (Exception e) {
			return new BaseState(false, AppInfo.IO_ERROR );
		}

		try {
			if (!keepLocalFile) {
				targetFile.delete();
			}
		} catch (Exception e) {

		}

		state = new BaseState(true);
		state.putInfo("size", targetFile.length());
		state.putInfo("title", fileName);
		// 这里返回的是ftp服务器地址
		state.putInfo("url", ftpPath);

		return state;
	}

	/***
	 * ftp图片上传
	 * 
	 * @param srcFile文件流
	 * @param destFileName上传后文件名
	 * @param destFoldName上传后文件包名
	 */
	public static void ftpUpload(File srcFile, String fileName, String foldName) {
		FTPClient ftpClient = new FTPClient();
		FileInputStream fis = null;

		try {

			ftpClient.connect(server);
			ftpClient.login(uname, pwd);

			fis = new FileInputStream(srcFile);
			// 设置上传目录
			ftpClient.changeWorkingDirectory(foldName);
			ftpClient.setBufferSize(1024);
			ftpClient.enterLocalPassiveMode();
			if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
					"OPTS UTF8", "ON"))) {
				// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
				LOCAL_CHARSET = "UTF-8";
			}
			ftpClient.setControlEncoding(LOCAL_CHARSET);
			fileName = new String(fileName.getBytes(LOCAL_CHARSET),
					SERVER_CHARSET);
			// 设置文件类型（二进制）
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.storeFile(fileName, fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("关闭FTP连接发生异常！", e);
			}
		}
	}

}
