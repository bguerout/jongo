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

package org.jongo;

import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import org.jongo.util.DBObjectResultHandler;
import org.jongo.util.JongoTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class CommandTest extends JongoTestCase {

    private Jongo jongo;

    @Before
    public void setUp() throws Exception {
        jongo = getJongo();

    }

    @Test
    public void canRunACommand() throws Exception {
        DBObject result = jongo.runCommand("{ buildInfo: 1 }").map(new DBObjectResultHandler());

        assertThat(result).isNotNull();
        assertThat(result.get("version")).isNotNull();
        assertThat(result.get("ok")).isEqualTo(1.0);
    }

    @Test
    public void canRunACommandWithParameter() throws Exception {

        String collectionName = "friends";
        createEmptyCollection(collectionName).withConcern(WriteConcern.SAFE).insert("{test:1}");

        DBObject result = jongo.runCommand("{ count: #}", collectionName).map(new DBObjectResultHandler());

        assertThat(result.get("n")).isEqualTo(1.0);
    }

    @Test
    public void canRunACommandAs() throws Exception {
        ServerStatus status = jongo.runCommand("{ serverStatus: 1 }").as(ServerStatus.class);

        assertThat(status.host).isNotNull();
        assertThat(status.pid).isNotNull();
        assertThat(status.ok).isEqualTo("1.0");
    }

    @Test
    public void canRunInvalidCommand() throws Exception {
        ServerStatus status = jongo.runCommand("{forceerror:1}").as(ServerStatus.class);

        assertThat(status.ok).isEqualTo("0.0");
    }

    @Test
    public void canThrowExceptionOnInvalidCommand() throws Exception {
        try {
            jongo.runCommand("{forceerror:1}").throwOnError().as(ServerStatus.class);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("\"errmsg\" : \"exception: forced error\"");
        }
    }

    private static class ServerStatus {
        String ok, pid, host;
    }
}
