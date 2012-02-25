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


var outputFolder = path.resolve("./gh-pages");

task('init', [], function (params) {
    var exists = path.existsSync(outputFolder);
    if (exists) {
        console.log("Deleting folder: " + outputFolder);
        wrench.rmdirSyncRecursive(outputFolder);
    }
    wrench.mkdirSyncRecursive(path.join(outputFolder, "assets/css"));
    wrench.mkdirSyncRecursive(path.join(outputFolder, "assets/img"));
    wrench.mkdirSyncRecursive(path.join(outputFolder, "assets/js"));
});

task('lessify', ['init'], function (params) {

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

task('transform', ['init','lessify'], function (params) {
    var source = fs.readFileSync("index.html", "utf8");
    var htmlFile = path.join(outputFolder, "index.html");
    jsdom.env({
        html:source,
        scripts:['http://code.jquery.com/jquery-1.7.1.min.js'],
        done:function (errors, window) {
            var $ = window.$;
            $('*[data-env="dev"]').remove();
            $('head').append('<link href="assets/css/jongo.css" type="text/css" rel="stylesheet"/>');
            fs.writeFileSync(htmlFile, $("html").html(), "utf8");
        }
    });
});

task('gh-pages', ['init','transform'], function (params) {

    wrench.copyDirSyncRecursive('assets/img', path.join(outputFolder, "assets/img"));
    wrench.copyDirSyncRecursive('assets/js', path.join(outputFolder, "assets/js"));
    wrench.copyDirSyncRecursive('assets/css', path.join(outputFolder, "assets/css"));
});
