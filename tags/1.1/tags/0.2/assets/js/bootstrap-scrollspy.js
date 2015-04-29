/* =============================================================
 * bootstrap-scrollspy.js v2.0.4
 * http://twitter.github.com/bootstrap/javascript.html#scrollspy
 * =============================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============================================================== */

!function(a){
  function b(b,c){var d=a.proxy(this.process,this),e=a(b).is("body")?a(window):a(b),f;this.options=a.extend({},a.fn.scrollspy.defaults,c);this.$scrollElement=e.on("scroll.scroll.data-api",d);this.selector=(this.options.target||(f=a(b).attr("href"))&&f.replace(/.*(?=#[^\s]+$)/,"")||"")+" .nav li > a";this.$body=a("body");this.refresh();this.process()}"use strict";b.prototype={constructor:b,refresh:function(){var b=this,c;this.offsets=a([]);this.targets=a([]);c=this.$body.find(this.selector).map(function(){var b=a(this),c=b.data("target")||b.attr("href"),d=/^#\w/.test(c)&&a(c);return d&&c.length&&[[d.position().top,c]]||null}).sort(function(a,b){return a[0]-b[0]}).each(function(){b.offsets.push(this[0]);b.targets.push(this[1])})},process:function(){var a=this.$scrollElement.scrollTop()+this.options.offset,b=this.$scrollElement[0].scrollHeight||this.$body[0].scrollHeight,c=b-this.$scrollElement.height(),d=this.offsets,e=this.targets,f=this.activeTarget,g;if(a>=c){return f!=(g=e.last()[0])&&this.activate(g)}for(g=d.length;g--;){f!=e[g]&&a>=d[g]&&(!d[g+1]||a<=d[g+1])&&this.activate(e[g])}},activate:function(b){var c,d;this.activeTarget=b;a(this.selector).parent(".active").removeClass("active");d=this.selector+'[data-target="'+b+'"],'+this.selector+'[href="'+b+'"]';c=a(d).parent("li").addClass("active");if(c.parent(".dropdown-menu")){c=c.closest("li.dropdown").addClass("active")}c.trigger("activate")}};a.fn.scrollspy=function(c){return this.each(function(){var d=a(this),e=d.data("scrollspy"),f=typeof c=="object"&&c;if(!e)d.data("scrollspy",e=new b(this,f));if(typeof c=="string")e[c]()})};a.fn.scrollspy.Constructor=b;a.fn.scrollspy.defaults={offset:10};a(function(){a('[data-spy="scroll"]').each(function(){var b=a(this);b.scrollspy(b.data())})})
  $('.nav-collapse').scrollspy();
}(window.jQuery)