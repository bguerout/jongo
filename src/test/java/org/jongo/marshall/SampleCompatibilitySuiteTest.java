package org.jongo.marshall;

import org.jongo.marshall.jackson.JacksonProcessor;
import org.jongo.util.TestContext;

import static org.junit.runners.Parameterized.Parameters;

//@RunWith(CompatibilitySuite.class) remove comment to execute this suite.
public class SampleCompatibilitySuiteTest {

    @Parameters
    public static TestContext context() {
        return new TestContext(new JacksonProcessor(), new JacksonProcessor());
    }

}
