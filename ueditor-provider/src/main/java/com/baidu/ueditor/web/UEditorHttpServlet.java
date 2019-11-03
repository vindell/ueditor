package com.baidu.ueditor.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.baidu.ueditor.utils.ConfigUtils;

@SuppressWarnings("serial")
public class UEditorHttpServlet extends HttpServlet {
	
	protected final static Logger LOG = LoggerFactory.getLogger(UEditorHttpServlet.class);

	protected boolean initialized;
	protected File configFile;
	protected Map<String,String> configProperties;
	protected boolean autoReload;
	
	// Fields that may change during the lifecycle of the Servlet (if enableReload==true).
    // Modifications of these should be synchronized on configLock (below).
	protected volatile Servlet servlet;
	protected volatile long timestampOfFileAtLastLoad;
    // See above.
	protected final Object configLock = new Object();

    public static final String CONFIG_FILE_PARAM = "configFile";
    public static final String CONFIG_FILE_DEFAULT = "/WEB-INF/config.json";

    public static final String AUTO_RELOAD_PARAM = "autoReload";
    public static final boolean AUTO_RELOAD_DEFAULT = true;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.configProperties = getConfigProperties(config);
		this.autoReload = getAutoReload();
        LOG.debug("Auto reloading " + (autoReload ? "enabled" : "disabled"));
        synchronized (configLock) {
            deployNewServlet(setup());
        }
        initialized = true;
	}
	
	@Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	if (!initialized) {
            throw new ServletException(getClass().getName() + ".init(ServletConfig) was not called");
        }
        reloadIfNecessary();
        servlet.service(req, resp);
    }
	
	/**
     * Determine if a reload is required. Override this to change the behavior.
     */
    protected boolean reloadRequired() {
        return timestampOfFileAtLastLoad != configFile.lastModified();
    }

    /**
     * Whether the config file should be monitored for changes and automatically reloaded.
     */
    protected boolean getAutoReload() {
        String autoReload = configProperties.get(AUTO_RELOAD_PARAM);
        if (autoReload == null) {
            return AUTO_RELOAD_DEFAULT;
        } else {
            String lower = autoReload.toLowerCase();
            return lower.equals("1") || lower.equals("true") || lower.equals("yes");
        }
    }

    /**
     * Gets the UEditor JSON config file name. Looks for a 'configFile' property in the Servlet init-params. 
     * If not found, defaults to '/WEB-INF/config.json'.
     */
    protected String getConfigFileName() {
        String config = configProperties.get(CONFIG_FILE_PARAM);
        if (config == null || config.length() == 0) {
            config = CONFIG_FILE_DEFAULT;
        }
        return config;
    }
    
    protected Map<String, String> getConfigProperties(ServletConfig config) {
        Map<String, String> initParams = new HashMap<String, String>();
        for (Enumeration<String> initParameterNames = config.getInitParameterNames(); initParameterNames.hasMoreElements();) {
            String key = (String) initParameterNames.nextElement();
            String value = config.getInitParameter(key).trim();
            initParams.put(key, value);
        }
        return initParams;
    }
    
    protected Servlet setup() throws ServletException {
    	ActionConfig configManager = ActionConfig.getInstance(getServletConfig(),loadConfig(getServletConfig(), getConfigFileName()));
        return new ActionServlet(configManager);
    }

    protected void reloadIfNecessary() throws ServletException {
        // TODO: Allow finer grained control of reload strategies:
        // - don't check file timestamp on every single request (once per N seconds).
        // - periodically check in background, instead of blocking request threads.
        if (autoReload && reloadRequired()) {
            synchronized (configLock) { // Double check lock for performance (works in JDK5+, with volatile items).
                if (reloadRequired()) {
                	deployNewServlet(setup());
                }
            }
        }
    }

    protected void deployNewServlet(Servlet newServlet) throws ServletException {
    	Servlet oldFilter = servlet;
        if (newServlet == null) {
            throw new ServletException("Cannot deploy null Servlet");
        }
        newServlet.init(getServletConfig());
        servlet = newServlet;
        if (oldFilter != null) {
            oldFilter.destroy();
        }
    }
    
    /**
     * Load the JSON config file. Will try a number of locations until it finds the file.
     * <pre>
     * - Will first search for a file on disk relative to the root of the web-app.
     * - Then a file with the absolute path.
     * - Then a file as a resource in the ServletContext (allowing for files embedded in a .war file).
     * - If none of those find the file, null will be returned.
     * </pre>
     */
    protected JSONObject loadConfig(ServletConfig servletConfig, String configFilePath) throws ServletException {

    	File configFile = new File(configFilePath);

        ServletContext servletContext = servletConfig.getServletContext();

        if (servletContext.getRealPath(configFilePath) != null) {
        	configFile = new File(servletContext.getRealPath(configFilePath));
        }

        if (configFile.canRead()) {
        	InputStream input = null;
            try {
                timestampOfFileAtLastLoad = configFile.lastModified();
                LOG.debug("Loading UEditor 1.1.x config file: " + configFile.getAbsolutePath());
                input = new FileInputStream(configFile);
                return JSONObject.parseObject(ConfigUtils.readConfig(input));
            } catch (IOException e) {
                throw new ServletException("Could not parse " + configFile.getAbsolutePath(), e);
            } finally {
            	IOUtils.closeQuietly(input);
            }
        } else {
            InputStream input = servletContext.getResourceAsStream(configFilePath);
            if (input == null) {
                LOG.debug("No config file present - using defaults and init-params. Tried: " + configFile.getAbsolutePath() + " and ServletContext:" + configFilePath);
                return null;
            }
            try {
                LOG.debug("Loading UEditor 1.1.x config file from ServletContext " + configFilePath);
                return JSONObject.parseObject(ConfigUtils.readConfig(input));
            } catch (IOException e) {
                throw new ServletException("Could not parse " + configFilePath + " (loaded by ServletContext)", e);
            } finally {
            	IOUtils.closeQuietly(input);
            }
        }
    }
    
    public void destroy() {
        synchronized (configLock) {
            if (servlet != null) {
            	servlet.destroy();
            }
        }
    }
	
}