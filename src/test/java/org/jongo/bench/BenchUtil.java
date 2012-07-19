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

package org.jongo.bench;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jongo.model.Coordinate;
import org.jongo.model.Friend;

public class BenchUtil {

    public static Friend createFriend(int id) {
        return new Friend("John" + id, "Address" + id, new Coordinate(1, id));
    }

    public static DBObject createDBOFriend(int id) {
        DBObject dbo = new BasicDBObject();
        dbo.put("name", "John" + id);
        dbo.put("address", "Address" + id);

        BasicDBObject coordinate = new BasicDBObject();
        coordinate.put("lat", 1);
        coordinate.put("lng", id);

        dbo.put("coordinate", coordinate);

        return dbo;
    }
}
