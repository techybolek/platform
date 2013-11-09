/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.event.adaptor.kafka;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;

public class ConsumerTest implements Runnable {

    private KafkaStream m_stream;
    private InputEventAdaptorListener m_brokerListener;
    private ObjectMapper mapper = new ObjectMapper();
    private Map<String, Object> evento;

    public ConsumerTest(KafkaStream a_stream, InputEventAdaptorListener a_brokerListener) {
        m_stream = a_stream;
        m_brokerListener = a_brokerListener;
    }
    
    public void run() {
        ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while (it.hasNext()) {
            try {
                evento = mapper.readValue(new String(it.next().message()),
                        new TypeReference<Map<String, Object>>() {
                        });
                m_brokerListener.onEvent(evento);
            } catch (IOException ex) {
                Logger.getLogger(ConsumerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
