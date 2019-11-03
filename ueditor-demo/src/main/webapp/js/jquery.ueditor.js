;(function($) {
	
	$.fn.extend({
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
				
				ue.addListener('contentchange',function(){
	                this.sync();
	                //1.2.4+以后可以直接给textarea的id名字就行了
	                $('textarea').valid();
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
	
})(jQuery);