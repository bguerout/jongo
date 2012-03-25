/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var jsdom = require('jsdom');
var fs = require('fs');
var less = require('less');
var path = require('path');
var util = require('util')
var wrench = require('wrench');


var outputFolder = path.resolve("./release-site");
var scriptFile = path.resolve("./apply_changes.sh");


task('prepare', [], function (params) {
    if (path.existsSync(outputFolder)) {
        console.log("Deleting folder: " + outputFolder);
        wrench.rmdirSyncRecursive(outputFolder);
    }
    if (path.existsSync(scriptFile)) {
        fs.unlinkSync(scriptFile)
    }
    wrench.mkdirSyncRecursive(outputFolder);
});

desc("Compile less file into a css file")
task('lessify', ['prepare'], function (params) {

    wrench.mkdirSyncRecursive(path.join(outputFolder, "assets/css"));
    var cssFile = path.join(outputFolder, "assets/css/jongo.css");

    fs.readFile('./assets/bootstrap/jongo.less', 'utf-8', function (err, data) {
        if (err) throw err;

        var parser = new (less.Parser)({
            paths:['./assets/bootstrap'],
            optimization:true,
            filename:'style.less'
        });

        parser.parse(data, function (err, tree) {
            if (err) throw err;

            var treeAsCss = tree.toCSS({compress:true, yuicompress:false});
            fs.writeFileSync(cssFile, treeAsCss, "utf8");
        });
    });
});

desc("Update index.html with prod-ready resources")
task('weave-html', ['prepare'], function (params) {

    var source = fs.readFileSync("index.html", "utf8");
    var htmlFile = path.join(outputFolder, "index.html");

    jsdom.env({
        html:source,
        scripts:['http://code.jquery.com/jquery-1.7.1.min.js'],
        done:function (errors, window) {
            var $ = window.$;

            //remove dev resources
            $('*[data-env="dev"]').remove();

            //Add compiles LESS CSS
            $('head').append('<link href="assets/css/jongo.css" type="text/css" rel="stylesheet"/>');

            //Add Google analytics : cannot use JQuery, cause it calls evalScript method on append
            var gaScript = fs.readFileSync("scripts/google-analytics.js", "utf8");
            var script = window.document.createElement("script");
            script.type = "text/javascript";
            window.document.getElementsByTagName("head")[0].appendChild(script);
            script.text = gaScript;

            fs.writeFileSync(htmlFile, $("html").html(), "utf8");
        }
    });
});

task('dns', ['prepare'], function (params) {
    var cnameFile = path.join(outputFolder, "CNAME");
    fs.writeFileSync(cnameFile, "jongo.org");
});

task('package', ['prepare', 'lessify', 'weave-html', 'dns'], function (params) {

    wrench.copyDirSyncRecursive('assets/img', path.join(outputFolder, "assets/img"));
    wrench.copyDirSyncRecursive('assets/js', path.join(outputFolder, "assets/js"));
    wrench.copyDirSyncRecursive('assets/css', path.join(outputFolder, "assets/css"));
});

desc("Create Jongo site")
task('default', ['package'], function (params) {
});

