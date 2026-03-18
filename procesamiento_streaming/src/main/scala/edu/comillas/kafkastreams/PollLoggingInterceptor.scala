package edu.comillas.kafkastreams

import org.apache.kafka.clients.consumer.{ConsumerInterceptor, ConsumerRecords, OffsetAndMetadata}
import org.apache.kafka.common.TopicPartition
import java.util
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

class PollLoggingInterceptor extends ConsumerInterceptor[String, String] {

  private val formatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z")

  override def onConsume(records: ConsumerRecords[String, String]): ConsumerRecords[String, String] = {
    val now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
    val readableTime = now.format(formatter)

    println(s"--------------------- [POLL] fecha=$readableTime | totalRegistros=${records.count()}")

    records.partitions().forEach { tp: TopicPartition =>
      val count = records.records(tp).size()
      println(s"[POLL]   → ${tp.topic()}-${tp.partition()} = $count registros")
    }
    records
  }

  override def onCommit(offsets: util.Map[TopicPartition, OffsetAndMetadata]): Unit = {}

  override def close(): Unit = {}

  override def configure(configs: util.Map[String, _]): Unit = {}
}

