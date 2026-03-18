
package edu.comillas.kafkastreams

import org.apache.kafka.streams._
import org.apache.kafka.streams.processor.api._
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.state._
import org.apache.kafka.common.header.internals.RecordHeader
import java.nio.charset.StandardCharsets
import java.util.Properties

object AppDemoTopologia extends App {

  // Configuración básica
  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "AppDemoTopologia-mbd##x")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde")
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde")

  // Construcción de la topología manualmente
  val topology = new Topology()

  // Añadimos fuente
 
  // Añadimos los procesadores 
  
  // Añadimos destino/s
 
  // Falta añadir el otro procesador y el otro sink
  println(topology.describe())

  // Ejecutamos el stream
  val streams = new KafkaStreams(topology, props)
  streams.start()

  // Añadimos un controlador para que cuando llegue un Control-C se indique al stream que se debe parar
  sys.addShutdownHook {
    streams.close()
  }

  // Esperamos indefinidamente
  Thread.sleep(500000000)
}

