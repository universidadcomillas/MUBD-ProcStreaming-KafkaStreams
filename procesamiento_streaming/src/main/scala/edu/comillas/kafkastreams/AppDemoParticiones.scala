
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

object AppDemoParticiones extends App {

  // Configuración básica
  val props = new Properties()
  // Hay que modificar estos dos parametros....
  props.put(StreamsConfig.APPLICATION_ID_CONFIG,            "AppDemoParticiones-mbdXX")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,         "localhost:9092")

  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,   "org.apache.kafka.common.serialization.Serdes$StringSerde")
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde")

  // Construcción de la topología manualmente
  val topology = new Topology()

  // Añadimos fuente
  topology.addSource("Source", "input-topic-v2")

  // Añadimos un procesador personalizado
  topology.addProcessor("MyProcessor", new ProcessorSupplier[String, String, String, String] {
    override def get(): Processor[String, String, String, String] = new Processor[String, String, String, String] {
      private var context: ProcessorContext[String, String] = _

      override def init(ctx: ProcessorContext[String, String]): Unit = {
        context = ctx
      }

      override def process(record: Record[String, String]): Unit = {
        if (record.value != null) {
//           println(s"Procesando mensaje: '${record.value}'")

          val metadataOpt = context.recordMetadata()

          if (metadataOpt.isPresent) {
            val metadata = metadataOpt.get()
            val topic = metadata.topic()
            val partition = metadata.partition()

            println(s"[DEBUG] topic=$topic partition=$partition")

          }
        }
      }

      override def close(): Unit = {}
    }
  }, "Source")

  // Añadimos destino
  topology.addSink("Sink", "output-topic", "MyProcessor")

  println(topology.describe())

  // Ejecutamos el stream
  val streams = new KafkaStreams(topology, props)
  streams.start()

  sys.addShutdownHook {
    streams.close()
  }

  // Esto se pone para que espere indefinidamente
  Thread.sleep(5000000)
}

