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

package org.jongo.model;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonProperty;

public class People {

    @JsonProperty("_id")
    private ObjectId id;
    private String name;
    private String address;
    private Coordinate coordinate;

    public People(String name) {
        this.name = name;
    }

    public People(ObjectId id, String name) {
        this.id = id;
        this.name = name;
    }

    public People(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public People(String name, Coordinate coordinate) {
        this.name = name;
        this.coordinate = coordinate;
    }

    public People() {
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
}
