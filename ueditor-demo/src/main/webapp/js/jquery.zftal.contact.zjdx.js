;(function($) {
	$.fn.extend({
		createKindEditor: function(options) {
			var isReadonly = $.defined(options) && options.readonly ? true : false,
				container = $.defined(options) && options.container ? options.container : undefined;
			var validEditValue = function() {
				var valid = true;
				if ($.fn.simpleValidate) {
					var $container = $(container).defined() ? $(container) : $(this).closest("div");
		    		if ((!$(this).valid()) && $(this.form).defined()) {
		    			valid = false;
		    			$container.errorClass($(this.form).validate().errorMap[$(this).attr("id")]);
		    		} else {
		    			valid = true;
		    			$container.successClass();
		    		}
		    	}
				return valid;
			}
			KindEditor.options.filterMode = false;	
			$(this).each(function(i, item) {
				var nodeId = $(item).attr("id");				
				if ($(item).is("textarea") && $.founded(nodeId) && $.defined(newsOption)) {
					KindEditor.remove('#' + nodeId);
					var editor = KindEditor.create('#' + nodeId, $.extend({},newsOption,{
						//pasteType: 1,
						afterCreate: function () {
							var selfEdit = this;							
							selfEdit.sync();
							selfEdit.readonly(isReadonly);
							if (!isReadonly) {
								selfEdit.focus();	
							}
							if ($(selfEdit.form).defined()) {
								$(selfEdit.form).validateForm({
									beforeSubmit: function(formData, jqForm, options) {
										selfEdit.sync();
										return validEditValue.call(item);
									}
								});
							}
					    },
					    afterBlur: function () {
					    	this.sync();
					    	validEditValue.call(item);
					    } 
					}, options));	
					window.setTimeout(function(){
						editor.readonly(isReadonly);
					}, 1000);
				}
			});
		},		
		createUEditor: function(options) {
			var isReadonly = $.defined(options) && options.readonly ? true : false,
				container = $.defined(options) && options.container ? options.container : undefined;
			var createContainer = function() {
				var contId = "ue_container_" + $(this).attr("id"),
					contName = "ue_content_" + $(this).attr("id");
				if (!$("#" + contId).defined()) {
					var $cont = $("<script id=\"" + contId + "\" name=\"" + contName 
						+ "\" type=\"text/plain\"><\/script>");
					$(this).after($cont);
					return $cont;
				} else {
					return $("#" + contId);
				}
			}, 
			validEditValue = function() {
				var valid = true;
				if ($.fn.simpleValidate) {
					var $container = $(container).defined() ? $(container) : $(this).closest("div");
		    		if ((!$(this).valid()) && $(this.form).defined()) {
		    			valid = false;
		    			$container.errorClass($(this.form).validate().errorMap[$(this).attr("id")]);
		    		} else {
		    			valid = true;
		    			$container.successClass();
		    		}
		    	}
				return valid;
			},
			fontZhName = {"宋体": "SimSun", "微软雅黑": "Microsoft YaHei", "楷体": "SimKai",
				"楷体_GB2312": "SimKai", "黑体": "SimHei", "隶书": "SimLi"},
			filterHtmlContent = function(content) {
				if (content && (typeof(content) == "string")) {
					return content.replace( /(<[a-z][^>]*)\sstyle=(["'])([^\2]*?)\2/gi, function(str, tag, tmp, style) {
						var n = [], s = UE.utils.html(style).split( /;\s*/g );
						for ( var i = 0,v; v = s[i];i++ ) {
							var name, value, 
								parts = v.replace(/['"]/g, '').split( ":" );
							if (parts.length == 2) {
	                            name = parts[0].toLowerCase();
	                            value = parts[1];
	                            switch ( name ) {
	                                case "font-family":
	                                	value && (value = $.trim(value.split(",")[0]));
	                                	fontZhName[value] && (value = fontZhName[value]);
	                                	value && (n[i] = name + ":" + value);
	                                	continue;
	                            }
	                            n[i] = name + ":" + parts[1];
	                        }
						}
						return tag + (n.length ? ' style="' + n.join( ';').replace(/;{2,}/g,';') + '"' : '');
					});
				} else {
					return content;
				}
			};
			$(this).each(function(i, item) {
				var containId = createContainer.call(item).attr("id");
				UE.delEditor(containId);
				var ue = UE.getEditor(containId, {
					autoHeight: false,
					readonly: isReadonly,
					initialContent: $(item).val(),
					initialFrameHeight: $(item).height() - 65,
					zIndex: (function() {
						var index;
						$(this).defined() && (index = $(this).css("z-index"));
						isNaN(index) && (index = arguments.callee.call($(this).parent()));
						isNaN(index) && (index = 999); 
						return index + 1000;
					}).call(item)
				});
				
				ue.addListener('ready',function(){
					$(item).hide();
					if ($(item.form).defined()) {
						var selfUE = this;
						$(item.form).validateForm({
							beforeSubmit: function(formData, jqForm, options) {
								$(item).val(filterHtmlContent(selfUE.getAllHtml()));
								return validEditValue.call(item);
							}
						});
					}
				});
				ue.addListener('fullscreenchanged', function(cmd,f) {
					if (f) {
						var editor = this;
						window.setTimeout(function(){
							UE.dom.domUtils.setViewportOffset(editor.ui.getDom(), {left: 0, top: 0});
						}, 300);
					}
				});
				ue.addListener('blur',function(){
					$(item).val(filterHtmlContent(this.getAllHtml()));
					validEditValue.call(item);
				});
				ue.addListener('beforepaste',function(e, html, root) {
					html.html = filterHtmlContent(html.html || root.toHtml());
				})
			});
		}
	});
	
	var isValidNode = function(nodeId) {
		return $.founded(nodeId) && $(nodeId).defined();
	},	
	isSelectNode = function(nodeId) {
		return (isValidNode(nodeId) && $(nodeId).is("select"));
	};	

	$.extend({		
		bindPyfaChangeEvent: function(options, callback){	
			var bindOptions = {
				triggered: false,//是否触发change事件
				headerValue: "全部",		
				//id：下拉框对应节点ID；url：获取数据的Action地址；changes：级联字段；params：获取数据需要的参数；
				//defaultValue：默认值；triggered：是否触发change事件；callback：回调函数
				//年级代码下拉框ID
				njdm_id: {id: "#nj_cx", url: _path+"/xtgl/comm_cxNjList.html", 
					changes:["zydl_id","gjzy_id","zyh_id","zyfx_id","bh_id"], params: [], 
					listKey: "njdm", listValue: "njmc", headerValue:null, defaultValue: null, triggered: null, callback: null},
				//系代码下拉框ID
				jg_id: {id: "#xy_cx", url: _path+"/xtgl/comm_cxXydmList.html", 
					changes:["xx_id", "zydl_id","gjzy_id","zyh_id","zyfx_id","bh_id"], params: [], 
					listKey: "jgdm", listValue: "jgmc", headerValue:null, defaultValue: null, triggered: null, callback: null},
				//专业大类代码下拉框ID
				xx_id: {id: "#xx_cx", url: _path+"/xtgl/comm_cxXxdmList.html", changes:[], params: ["jg_id"], 
					listKey: "jgdm", listValue: "jgmc", headerValue:null, defaultValue: null, triggered: null, callback: null},
				//专业大类代码下拉框ID
				zydl_id: {id: "#zydl_cx", url: _path+"/xtgl/comm_cxPyfaZydlList.html", 
					changes:["gjzy_id","zyh_id","zyfx_id","bh_id"], params: ["njdm_id", "jg_id", "zydl_id"], 
					listKey: "zydldm", listValue: "zydlmc", headerValue:null, defaultValue: null, triggered: null, callback: null},
				//国家专业代码下拉框ID
				gjzy_id: {id: "#gjzy_cx", url: _path+"/xtgl/comm_cxPyfaGjzyList.html", 
					changes:["zyh_id","zyfx_id","bh_id"], params: ["njdm_id", "jg_id", "zydl_id"], 
					listKey: "gjzydm", listValue: "gjzymc", headerValue:null, defaultValue: null, triggered: null, callback: null},
				//专业大类代码下拉框ID
				zyh_id: {id: "#zy_cx", url: _path+"/xtgl/comm_cxPyfaZyxxList.html", 
					changes:["zyfx_id","bh_id"], params: ["njdm_id", "jg_id", "zydl_id", "gjzy_id"], 
					listKey: "zydm", listValue: "zymc", headerValue:null, defaultValue: null, triggered: null, callback: null},
				//专业方向下拉框ID
				zyfx_id: {id: "#zyfx_cx", url: _path+"/xtgl/comm_cxPyfaZyfxList.html", 
					changes:["bh_id"], params: ["njdm_id", "jg_id", "zydl_id", "gjzy_id", "zyh_id"], 
					listKey: "zyfxdm", listValue: "zyfxmc", headerValue:null, defaultValue: null, triggered: null, callback: null},
				//班级代码下拉框ID
				bh_id: {id: "#bj_cx", url: _path+"/xtgl/comm_cxPyfaBjdmList.html", changes:[], params: ["njdm_id", "jg_id", "zydl_id", "gjzy_id", "zyh_id", "zyfx_id"], 
					listKey: "bjdm", listValue: "bjmc", headerValue:null, defaultValue: null, triggered: null, callback: null}
			}, defaultValues = {},	fieldMapper = {}, selectOptions = {},
			//获取有效的关联字段
			getChangeFields = function(changes) {
				var fields = [];
				if (changes && $.isArray(changes)) {
					for (var i = 0, iLen = changes.length; i < iLen; i++) {
						var paramOpt = selectOptions[changes[i]];
						if ($.defined(paramOpt) && isSelectNode(paramOpt.id)
								&& $.founded(paramOpt.url)) {	
							fields.push(changes[i]);
						}
					}
				}
				return fields;				
			},			
			//获取下拉字段需要的参数数据
			getChangeParamData = function(params) {
				var paramData = {};
				if (params && $.isArray(params) && (params.length > 0)) {
					for (var i = 0, iLen = params.length; i < iLen; i++) {
						var	field = params[i];
						paramData[field] = defaultValues[field]; 	
						if ($.defined(fieldMapper[field])) {
							paramData[field] = $(fieldMapper[field]).val();
						}
					}
				}
				return paramData;
			},			
			//获取字段change事件方法
			getChangeEventFunc = function(option) {
				var changeFunc = null, refFields = getChangeFields(option.changes);
				if ($.isArray(refFields) && (refFields.length > 0)) {
					changeFunc = function() {
						for (var i = 0, iLen = refFields.length; i < iLen; i++) {
							var field = refFields[i], paramOpt = selectOptions[field];
							if (isSelectNode(paramOpt.id) && $.founded(paramOpt.url)) {
								$(paramOpt.id).refreshSelectList(paramOpt.url, 
									getChangeParamData(paramOpt.params),paramOpt);
							}
						}
					};
				}
				return changeFunc;
			};
			
			//合并级联设置
			for (var field in options) {
				if ($.defined(options[field])) {
					if (typeof(options[field]) == "object") {
						bindOptions[field] = $.extend(bindOptions[field], options[field]);
					} else {
						bindOptions[field] = options[field];
					}
				}
			}
			
			for (var field in bindOptions) {
				var option = bindOptions[field];
				if ($.defined(option.defaultValue)) {
					defaultValues[field] = option.defaultValue;	//各字段默认值
				}				
				if ($.defined(option.id) && isValidNode(option.id)) {
					fieldMapper[field] = option.id;				//各字段与节点映射
					if (isSelectNode(option.id)) {
						if (!$.defined(option.headerValue)) {
							option.headerValue = bindOptions.headerValue;
						}
						if (!$.defined(option.triggered)) {
							option.triggered = bindOptions.triggered;
						}
						selectOptions[field] = option;			//各下拉字段设置信息
					}
				}
			}
			
			for (var field in selectOptions) {
				var option = selectOptions[field],
					changeFunc = getChangeEventFunc(option);
				if (changeFunc && $.isFunction(changeFunc)) {
					$(option.id).change(changeFunc);
				}
			}
		}
	});
})(jQuery);