
package edu.comillas.kafkastreams

import org.apache.kafka.streams._
import org.apache.kafka.streams.processor.api._
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.state._
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import java.time.Instant

class SimpleProcessor2 extends Processor[String, String, String, String] {

  private var context: ProcessorContext[String, String] = _

  override def init(ctx: ProcessorContext[String, String]): Unit = {
    context = ctx
  }

  override def process(record: Record[String, String]): Unit = {
    val key   = record.key()
    val value = record.value()

    val parsed = parse(value)

    parsed match {
      case Right(json) =>

        // Extraer MAC y timestamp
        val macOpt        = json.hcursor.get[String]("MAC").toOption
        val tsOpt         = json.hcursor.get[String]("@timestamp").toOption
        val pingsOpt      = json.hcursor.get[Vector[Json]]("pings").toOption

        (macOpt, tsOpt, pingsOpt) match {
          case (Some(mac), Some(ts), Some(pings)) =>

            // Para cada ping, crear un nuevo JSON enriquecido
            pings.foreach { pingJson =>

              val newHeaders = new RecordHeaders()
              val newRecord = new Record(key, "texto", record.timestamp(), newHeaders)

              // Enviar a los sinks
              context.forward(newRecord, "")
            }

          case _ =>
            println("JSON incompleto: falta MAC, @timestamp o pings")
        }

      case Left(err) =>
        println(s"Error parseando JSON: $err")
    }


  }

  override def close(): Unit = {
    println(s"[Close] Cerrando processor2 ")
  }
}

