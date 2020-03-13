/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
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

package org.jongo;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.jongo.util.JongoTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jongo.RawResultHandler.asRaw;

public class GridFsTest extends JongoTestBase {

    GridFS gridFS;

    @Before
    public void setUp() throws Exception {
        gridFS = new GridFS(getDatabase());
        insertTestFileInGridFS();
    }

    @Test
    public void shouldAllowDualAccessToFilesCollection() throws Exception {
        queryWithJongoAndMapToCustomClass();
        queryWithJongoAndMapToGridFSDBFile();
        queryWithGridFS();
    }

    @After
    public void tearDown() throws Exception {
        dropGridFsCollections();
    }

    private void insertTestFileInGridFS() {
        GridFSInputFile gridFile = gridFS.createFile(new byte[]{
                (byte) 0xCA,
                (byte) 0xFE,
                (byte) 0xBA,
                (byte) 0xBE});
        gridFile.setFilename("test.txt");
        gridFile.save();
    }

    private void queryWithJongoAndMapToCustomClass() {
        CustomFileDescriptor descriptor = getJongo().getCollection("fs.files")
                .findOne()
                .as(CustomFileDescriptor.class);
        assertThat(descriptor.filename).isEqualTo("test.txt");
    }

    private void queryWithJongoAndMapToGridFSDBFile() {
        GridFSDBFile gridFile = getJongo().getCollection("fs.files")
                .findOne()
                .map(asRaw(GridFSDBFile.class));
        assertThat(gridFile.getFilename()).isEqualTo("test.txt");
    }

    private void queryWithGridFS() {
        GridFSDBFile gridFile = gridFS.findOne(new BasicDBObject());
        assertThat(gridFile.getFilename()).isEqualTo("test.txt");
    }

    private void dropGridFsCollections() throws Exception {
        getDatabase().getCollection("fs.files").drop();
        getDatabase().getCollection("fs.chunks").drop();
    }

    static class CustomFileDescriptor {
        String filename;
    }
}
