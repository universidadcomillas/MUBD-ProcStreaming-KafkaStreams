

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


class SimpleProcessor1 extends Processor[String, String, String, String] {

  private var context: ProcessorContext[String, String] = _

  override def init(ctx: ProcessorContext[String, String]): Unit = {
    context = ctx
  }

  override def process(record: Record[String, String]): Unit = {
       val key   = record.key 
       val value = record.value 
       // Parsear el JSON de entrada
       val parsed: Either[ParsingFailure, Json] = parse(value)

       parsed match {
         case Right(json) =>
           // Eliminar el campo "pings"

           // Convertir a String
           val newJsonString = ""

           // Añadir la cabecera para el indice (la necesita ElasticsearchProcessor)

           // Enviar a los procesador correspondientes
//           context.forward(newRecord2Proc2, "Processor-2")

         case Left(error) =>
           // Manejo básico de errores
           println(s"Error parseando JSON: $error")
       }

  }

  override def close(): Unit = {
    println(s"[Close] Cerrando processor1")
  }
}

