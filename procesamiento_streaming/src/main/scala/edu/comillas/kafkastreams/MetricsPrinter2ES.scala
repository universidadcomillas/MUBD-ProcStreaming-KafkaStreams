
package edu.comillas.kafkastreams

import java.net.http.HttpClient
import java.security.cert.X509Certificate
import javax.net.ssl._
import java.security.SecureRandom
import io.circe.syntax._
import io.circe.Json
import org.apache.kafka.streams.KafkaStreams

import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.{Executors, TimeUnit}
import java.time.format.DateTimeFormatter

object MetricsPrinter2ES {

  def createHttpClient(): HttpClient = {
    val trustAll = Array[TrustManager](
      new X509TrustManager {
        def checkClientTrusted(c: Array[X509Certificate], a: String) = ()
        def checkServerTrusted(c: Array[X509Certificate], a: String) = ()
        def getAcceptedIssuers() = Array.empty[X509Certificate]
      }
    )

    val ssl = SSLContext.getInstance("TLS")
    ssl.init(null, trustAll, new SecureRandom())

    val params = new SSLParameters()
    params.setEndpointIdentificationAlgorithm(null)

    HttpClient.newBuilder()
      .sslContext(ssl)
      .sslParameters(params)
      .build()
  }

  def startMetricsExport(streams: KafkaStreams, seconds: Int): Unit = {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z")

    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val client    = createHttpClient()  // o HttpClient.newHttpClient()

    val esUrl     = "https://localhost:9200/streams-metrics/_doc"
    val username  = "elastic"
    val password  = "pass4icai"

    val basicAuth = {
      val encoded = Base64.getEncoder.encodeToString(s"$username:$password".getBytes(StandardCharsets.UTF_8))
      s"Basic $encoded"
    }

    scheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {

          val ts = System.currentTimeMillis()

          // Convertir cada métrica a JSON
          val metricsJson = streams.metrics().entrySet().toArray().map { entryObj =>
            val entry = entryObj.asInstanceOf[java.util.Map.Entry[_, _]]
            val name  = entry.getKey.asInstanceOf[org.apache.kafka.common.MetricName]
            val metric = entry.getValue.asInstanceOf[org.apache.kafka.common.Metric]

            Json.obj(
              "group"   -> name.group().asJson,
              "name"    -> name.name().asJson,
//              "tags"    -> name.tags().asScala.toMap.asJson,
//              "value"   -> Option(metric.metricValue()).map(_.toString.toDoubleOption.getOrElse(0.0)).asJson
              "value"   -> metric.metricValue().toString.asJson
            )
          }

          val rootJson = Json.obj(
            "@timestamp" -> ts.asJson,
            "hostname"  -> java.net.InetAddress.getLocalHost.getHostName.asJson,
            "streamId"  -> streams.toString.asJson,
            "metrics"   -> metricsJson.asJson
          )

          val jsonString = rootJson.noSpaces

          // Crear request
          val request = HttpRequest.newBuilder()
            .uri(URI.create(esUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", basicAuth)
            .POST(HttpRequest.BodyPublishers.ofString(jsonString))
            .build()

          // Enviar
          try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            println(s"[ES] status=${response.statusCode()} body=${response.body()}")
          } catch {
            case ex: Exception => println(s"[ES ERROR] ${ex.getMessage}")
          }
        }
      },
      seconds, // retraso inicial
      seconds, // intervalo en segundos
      TimeUnit.SECONDS
    )
  }
}

