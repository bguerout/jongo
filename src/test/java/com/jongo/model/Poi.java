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

package com.jongo.model;

import javax.persistence.Id;

public class Poi {

    @Id
    public String id;
    public String address;
    public Coordinate coordinate;

    Poi() {
    }

    public Poi(String address) {
        this.address = address;
    }

    public Poi(String id, String address) {
        this.id = id;
        this.address = address;
    }

    public Poi(String address, int lat, int lng) {
        this.address = address;
        this.coordinate = new Coordinate(lat, lng);
    }

    public Poi(String id, int lat, int lng, int alt) {
        this.id = id;
        this.coordinate = new Coordinate3D(lat, lng, alt);
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}
