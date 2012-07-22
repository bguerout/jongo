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

package org.jongo.util.compatibility;

import org.jongo.marshall.Marshaller;
import org.jongo.marshall.Unmarshaller;

public class TestContext {

    private final String contextName;
    private final Unmarshaller unmarshaller;
    private final Marshaller marshaller;

    public TestContext(String contextName, Marshaller marshaller, Unmarshaller unmarshaller) {
        this.contextName = contextName;
        this.unmarshaller = unmarshaller;
        this.marshaller = marshaller;
    }

    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public String getContextName() {
        return contextName;
    }
}
