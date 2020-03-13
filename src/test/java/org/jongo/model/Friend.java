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

package org.jongo.model;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.MongoId;

public class Friend {

    @Id //see DeprecatedAnnotationsCompatibilitySuiteTest for more informations
    @MongoId
    private ObjectId id;
    private String name;
    private String address;
    private Coordinate coordinate;
    private Gender gender;

    public Friend(String name) {
        this.name = name;
    }

    public Friend(ObjectId id, String name) {
        this.id = id;
        this.name = name;
    }

    public Friend(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public Friend(String name, Coordinate coordinate) {
        this.name = name;
        this.coordinate = coordinate;
    }

    public Friend() {
    }

    public Friend(String name, String address, Coordinate coordinate) {
        this.name = name;
        this.address = address;
        this.coordinate = coordinate;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend)) return false;

        Friend friend = (Friend) o;

        if (address != null ? !address.equals(friend.address) : friend.address != null) return false;
        if (coordinate != null ? !coordinate.equals(friend.coordinate) : friend.coordinate != null) return false;
        if (id != null ? !id.equals(friend.id) : friend.id != null) return false;
        if (name != null ? !name.equals(friend.name) : friend.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (coordinate != null ? coordinate.hashCode() : 0);
        return result;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }
}
