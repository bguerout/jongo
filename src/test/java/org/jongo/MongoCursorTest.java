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

import com.mongodb.DBCursor;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MongoCursorTest {

    private DBCursor dbCursor;
    private MongoCursor<String> mongoCursor;

    @Before
    public void setUp() throws Exception {
        dbCursor = mock(DBCursor.class);
        mongoCursor = new MongoCursor<String>(dbCursor, mock(ResultHandler.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailWhenNoMoreElements() throws Exception {

        when(dbCursor.hasNext()).thenReturn(false);

        mongoCursor.next();
    }

    @Test
    public void shouldCloseDbCursor() throws Exception {

        mongoCursor.close();

        verify(dbCursor).close();
    }

    @Test
    public void shouldReturnACopyOfDbCursor() throws Exception {

        when(dbCursor.copy()).thenReturn(dbCursor);

        Iterator<String> iterator = mongoCursor.iterator();

        assertThat(iterator).isNotNull();
        assert mongoCursor != iterator;
        verify(dbCursor).copy();
    }

}
