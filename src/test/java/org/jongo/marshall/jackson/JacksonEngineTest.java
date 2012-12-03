package org.jongo.marshall.jackson;

import com.mongodb.DBObject;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.MarshallingException;
import org.jongo.model.Fox;
import org.jongo.model.Friend;
import org.jongo.util.ErrorObject;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static org.jongo.util.BsonUtil.bsonify;


public class JacksonEngineTest {

    JacksonEngine engine = new JacksonEngine(new JacksonMapper.Builder().innerMapping());

    @Test(expected = MarshallingException.class)
    public void shouldFailWhenUnableToMarshall() throws Exception {

        engine.marshall(new ErrorObject());
    }

    @Test
    public void shouldFailWhenUnableToUnmarshall() throws Exception {

        try {
            engine.unmarshall(bsonify("{'error':'notADate'}"), ErrorObject.class);
            fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(MarshallingException.class);
        }
    }

    @Test
    public void canMarshall() {

        BsonDocument doc = engine.marshall(new Fox("fantastic", "roux"));

        DBObject dbo = doc.toDBObject();
        assertThat(dbo.get("_class")).isEqualTo("org.jongo.model.Fox");
        assertThat(dbo.get("name")).isEqualTo("fantastic");
        assertThat(dbo.get("color")).isEqualTo("roux");
    }

    @Test
    public void canUnmarshallBson() throws IOException {

        BsonDocument document = bsonify("{'address': '22 rue des murlins'}");

        Friend friend = engine.unmarshall(document, Friend.class);

        assertThat(friend.getAddress()).isEqualTo("22 rue des murlins");
    }

}
