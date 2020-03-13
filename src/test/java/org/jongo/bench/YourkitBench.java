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

package org.jongo.bench;

import org.junit.Ignore;
import org.junit.Test;

public class YourkitBench {

    private static int ITERATION = 100000;


    @Test
    @Ignore
    public void saveWithDriver() throws Exception {
        SaveBench saveBench = new SaveBench();
        saveBench.setUp();
        saveBench.timeDriverSave(ITERATION);
    }

    @Test
    @Ignore
    public void saveWithBsonJongo() throws Exception {
        SaveBench saveBench = new SaveBench();
        saveBench.setUp();
        saveBench.timeJongoSave(ITERATION);
    }

    @Test
    @Ignore
    public void findWithDriver() throws Exception {
        FindBench findBench = new FindBench();
        findBench.setUp();
        findBench.timeDriverFind(ITERATION);
    }

    @Test
    @Ignore
    public void findWithBsonJongo() throws Exception {
        FindBench findBench = new FindBench();
        findBench.setUp();
        findBench.timeJongoFind(ITERATION);
    }

    @Test
    @Ignore
    public void decodeWithBsonJongo() throws Exception {
        DecoderBench bench = new DecoderBench();
        bench.timeDecodeWithBsonJongo(1);
    }
}
