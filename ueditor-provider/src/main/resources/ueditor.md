#UEditor Servlet 配置
 

# web应用 配置 

> 依赖包
<dependency>
	<groupId>com.baidu</groupId>
	<artifactId>ueditor</artifactId>
	<version>最新版本 Maven 为准</version>
</dependency>
<dependency>
	<groupId>com.baidu</groupId>
	<artifactId>ueditor-plus</artifactId>
	<version>最新版本 Maven 为准</version>
</dependency>

> web.xml 配置

<!-- UEditor Servlet -->
<filter>
	<filter-name>ueditor-servlet</filter-name>
	<filter-class>com.baidu.ueditor.web.UEditorHttpServlet</filter-class>
	<!-- 是否自动重新载入配置文件 -->
    <init-param>
    	<param-name>autoReload</param-name>
    	<param-value>ture</param-value>
    </init-param>
    <!-- 初始化配置文件路径 -->
    <init-param>
    	<param-name>configFile</param-name>
    	<param-value>/WEB-INF/ueditor.json</param-value>
    </init-param>
</filter>
<filter-mapping>
	<filter-name>ueditor-servlet</filter-name>
	<url-pattern>/ueditor</url-pattern>
</filter-mapping>

 
