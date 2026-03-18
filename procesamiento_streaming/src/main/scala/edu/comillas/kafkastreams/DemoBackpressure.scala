
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

object DemoBackpressure extends App {

  // Configuración básica
  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG,            "DemoBackpressure-##")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,         "localhost:9092")
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,   "org.apache.kafka.common.serialization.Serdes$StringSerde")
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde")
  props.put("consumer.interceptor.classes",                 "edu.comillas.mubd.procstreaming.kafkastreams.PollLoggingInterceptor")

  // Construcción de la topología manualmente
  val topology = new Topology()

  // Añadimos fuente
  topology.addSource("Source", "input-topic")

  // Añadimos un procesador personalizado
  topology.addProcessor("ToUpperProcessor", new ProcessorSupplier[String, String, String, String] {
    override def get(): Processor[String, String, String, String] = new Processor[String, String, String, String] {
      private var context: ProcessorContext[String, String] = _

      override def init(ctx: ProcessorContext[String, String]): Unit = {
        context = ctx
      }

      override def process(record: Record[String, String]): Unit = {
        if (record.value != null) {
          val upper = record.value.toUpperCase()
          println(s"[DEBUG] Processing message: '${record.value}' -> '$upper'")

          val metadataOpt = context.recordMetadata()

          if (metadataOpt.isPresent) {
            val metadata = metadataOpt.get()
            val topic = metadata.topic()
            val partition = metadata.partition()
            val offset = metadata.offset()

            println(s"[DEBUG] topic=$topic partition=$partition offset=$offset")

            val headers = record.headers()
            headers.add(new RecordHeader("topic", topic.getBytes(StandardCharsets.UTF_8)))
            headers.add(new RecordHeader("partition", partition.toString.getBytes(StandardCharsets.UTF_8)))
            headers.add(new RecordHeader("offset", offset.toString.getBytes(StandardCharsets.UTF_8)))

            context.forward(record.withHeaders(headers))
          }
        }
      }

      override def close(): Unit = {}
    }
  }, "Source")

  // Añadimos destino
  topology.addSink("Sink", "output-topic", "ToUpperProcessor")

  println(topology.describe())

  // Ejecutamos el stream
  val streams = new KafkaStreams(topology, props)
  streams.start()

  sys.addShutdownHook {
    streams.close()
  }

  Thread.sleep(5000000)
}

