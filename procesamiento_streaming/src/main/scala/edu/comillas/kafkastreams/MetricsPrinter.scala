
package edu.comillas.kafkastreams

import java.util.concurrent.{Executors, TimeUnit}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import java.io._
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter


object MetricsPrinter {

  def startMetricsExport(streams: KafkaStreams, seconds: Int, filename: String): Unit = {

    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    scheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {

          var fw : FileWriter = null
          var writer: PrintWriter = null
          val now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
          val readableTime = now.format(formatter)

          if ( filename != null ) {
            fw = new FileWriter(filename, true)
            writer = new PrintWriter(fw)
          } else {
            println("===== MÉTRICAS KAFKA STREAMS =====")
          }

          var counter: Int = 0 

          val metrics = streams.metrics()

          metrics.forEach { case (name, metric) =>
            counter = counter +1
            if ( filename == null ) {
              println(s"$counter - ${name.group()}.${name.name()} -> ${metric.metricValue()}")
              println(s"${name.tags()}") 
            } else {
              writer.println(s"$readableTime;${name.group()}.${name.name()};${metric.metricValue()}")
            }

          }

          if ( filename != null ) {
            fw.flush()
            fw.close() 
          } else {
            println("===================================")
          }
        }
      },
      seconds,        // espera inicial
      seconds,        // intervalo
      TimeUnit.SECONDS
    )
  }
}

