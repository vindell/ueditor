package com.baidu.ueditor.web;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baidu.ueditor.define.ActionMap;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.State;
import com.baidu.ueditor.provider.ProviderManager;

@SuppressWarnings("serial")
class ActionServlet extends HttpServlet {

	protected ActionConfig actionConfig = null;

	public ActionServlet(ActionConfig actionConfig) {
		this.actionConfig = actionConfig;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding( "UTF-8" );
		response.setHeader("Content-Type" , "text/html");
		//响应处理结果
		response.getWriter().write( exec(request) );
	}
	
	public String exec (HttpServletRequest request) {
		
		String callbackName = request.getParameter("callback");
		
		if ( callbackName != null ) {

			if ( !validCallbackName( callbackName ) ) {
				return new BaseState( false, AppInfo.ILLEGAL ).toJSONString();
			}
			
			return callbackName+"("+this.invoke(request)+");";
			
		} else {
			return this.invoke(request);
		}

	}
	
	public String invoke(HttpServletRequest request) {
		
		String actionType = request.getParameter( "action" );
		
		if ( actionType == null || !ActionMap.mapping.containsKey( actionType ) ) {
			return new BaseState( false, AppInfo.INVALID_ACTION ).toJSONString();
		}
		
		if ( this.actionConfig == null || !this.actionConfig.valid() ) {
			return new BaseState( false, AppInfo.CONFIG_ERROR ).toJSONString();
		}
		
		State state = null;
		
		int actionCode = ActionMap.getType( actionType );
		
		Map<String, Object> conf = null;
		
		switch ( actionCode ) {
		
			case ActionMap.CONFIG:{
				return this.actionConfig.getAllConfig().toString();
			}
			case ActionMap.UPLOAD_IMAGE:
			case ActionMap.UPLOAD_SCRAWL:
			case ActionMap.UPLOAD_VIDEO:
			case ActionMap.UPLOAD_FILE:{
				conf = this.actionConfig.getConfig( actionCode );
				//扩展上传实现
				String provider = (String) conf.get("upload_provider");
				state = ProviderManager.getUploadProvider(provider).doExec( request, conf);
			};break;
				
			case ActionMap.CATCH_IMAGE:{
				conf = this.actionConfig.getConfig( actionCode );
				String[] list = request.getParameterValues( (String)conf.get( "fieldName" ) );
				//扩展上传实现
				String provider = (String) conf.get("image_hunter_provider");
				//state = new ImageHunter( conf ).capture( list );
				state = ProviderManager.getImageHunterProvider(provider).config(conf).capture( list );
			};break;
				
			case ActionMap.LIST_IMAGE:
			case ActionMap.LIST_FILE:{
				conf = this.actionConfig.getConfig( actionCode );
				int start = this.getStartIndex(request);
				//扩展上传实现
				String provider = (String) conf.get("file_manager_provider");
				//state = new FileManager( conf ).listFile( start );
				state = ProviderManager.getFileManagerProvider(provider).config(conf).listFile( start );
			};break;
				
		}
		
		return state.toJSONString();
		
	}
	
	public int getStartIndex (HttpServletRequest request) {
		
		String start = request.getParameter( "start" );
		
		try {
			return Integer.parseInt( start );
		} catch ( Exception e ) {
			return 0;
		}
		
	}
	
	/**
	 * callback参数验证
	 */
	public boolean validCallbackName ( String name ) {
		
		if ( name.matches( "^[a-zA-Z_]+[\\w0-9_]*$" ) ) {
			return true;
		}
		
		return false;
		
	}
	

}
