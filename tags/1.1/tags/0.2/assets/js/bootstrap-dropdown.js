/* ============================================================
 * bootstrap-dropdown.js v2.0.0
 * http://twitter.github.com/bootstrap/javascript.html#dropdowns
 * ============================================================
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
 * ============================================================ */

!function(a){function d(){a(b).parent().removeClass("open")}"use strict";var b='[data-toggle="dropdown"]',c=function(b){var c=a(b).on("click.dropdown.data-api",this.toggle);a("html").on("click.dropdown.data-api",function(){c.parent().removeClass("open")})};c.prototype={constructor:c,toggle:function(b){var c=a(this),e=c.attr("data-target"),f,g;if(!e){e=c.attr("href");e=e&&e.replace(/.*(?=#[^\s]*$)/,"")}f=a(e);f.length||(f=c.parent());g=f.hasClass("open");d();!g&&f.toggleClass("open");return false}};a.fn.dropdown=function(b){return this.each(function(){var d=a(this),e=d.data("dropdown");if(!e)d.data("dropdown",e=new c(this));if(typeof b=="string")e[b].call(d)})};a.fn.dropdown.Constructor=c;a(function(){a("html").on("click.dropdown.data-api",d);a("body").on("click.dropdown.data-api",b,c.prototype.toggle)})}(window.jQuery)