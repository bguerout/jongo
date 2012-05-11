package org.jongo.marshall;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BSONMarshallerTest {


    @Test
    public void onBSONPrimitiveShouldCallBSONMarshaller() throws Exception {

        BSONMarshaller marshaller = new BSONMarshaller(mock(Marshaller.class));

        String value = marshaller.marshall("1");

        assertThat(value).isEqualTo("\"1\"");
    }

    @Test
    public void onNonBSONPrimitiveShouldFallback() throws Exception {

        Marshaller complexTypeMarshaller = mock(Marshaller.class);
        BSONMarshaller marshaller = new BSONMarshaller(complexTypeMarshaller);
        Object obj = new Object();

        marshaller.marshall(obj);

        verify(complexTypeMarshaller, times(1)).marshall(obj);
    }

}
