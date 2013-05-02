package org.jongo.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.jongo.RawResultHandler;
import org.jongo.util.JongoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.RawResultHandler.asRaw;

public class GridFsTest extends JongoTestCase {

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
