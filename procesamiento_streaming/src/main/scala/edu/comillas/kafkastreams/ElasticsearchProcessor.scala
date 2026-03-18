
package edu.comillas.kafkastreams

import org.apache.kafka.streams._
import org.apache.kafka.streams.processor.api._
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.state._
import org.apache.kafka.common.header.internals.RecordHeader

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.net.http.HttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl._
import java.net.http.HttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl._


class ElasticsearchProcessor extends Processor[String, String, String, String] {

  private var context: ProcessorContext[String, String] = _
  private val id = this.hashCode() // identificador único por instancia

  private val esUrl = "http://localhost:9200"  // Cambia si hace falta

  // Basic Auth admin/pass4adm
  private val username = "elastic"
  private val password = "pass4icai"
  private val basicAuthHeader: String = {
    val encoded = Base64.getEncoder.encodeToString(s"$username:$password".getBytes(StandardCharsets.UTF_8))
    s"Basic $encoded"
  }

  private var httpclient: HttpClient = _ 

  override def init(ctx: ProcessorContext[String, String]): Unit = {
    context = ctx
    val threadName = Thread.currentThread().getName
    println(s"[Init] Nueva instancia de LoggingProcessor id=$id en hilo $threadName")


    // 1. Trust manager que acepta cualquier certificado
    val trustAllCerts: Array[TrustManager] = Array(
      new X509TrustManager {
        override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = ()
        override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = ()
        override def getAcceptedIssuers: Array[X509Certificate] = Array.empty
      }
    )

    // 2. SSLContext con trust-all
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAllCerts, new SecureRandom())
    val sslParams = new SSLParameters()
    sslParams.setEndpointIdentificationAlgorithm(null) // 🔥 CLAVE: desactiva hostname verification

    // 3. HttpClient que ignora certificados y hostname
    httpclient = HttpClient.newBuilder()
      .sslContext(sslContext)
      .sslParameters(sslParams)
      .build()
  }

//  override def process(key: String, value: String): Unit = {
  override def process(record: Record[String, String]): Unit = {

    // Obtener el registro original para acceder a los headers
//    val record: ConsumerRecord[_, _] = context.record()

    // Recuperar header "index"
    val headerValue = Option(record.headers().lastHeader("index")) match {
      case Some(h) => new String(h.value(), StandardCharsets.UTF_8)
      case None =>
        println("[WARN] Registro sin header 'index'. Se ignora.")
        return
    }
    val indexName = headerValue.trim
    val key = record.key() 
    val value = record.value() 

    println(s"[INFO] Indexando en índice: $indexName")

    // 3️⃣ Construcción del body para Elasticsearch (usa el value del registro)
/*
    val jsonPayload =
      s"""{
         |  "kafka_key": "${Option(key).getOrElse("")}",
         |  "message": "$value"
         |}""".stripMargin
*/

//    val jsonPayloadV2 = RecordJsonBuilder.toJson(record, context)
    val jsonPayloadV2 = record.value


    println(s"$jsonPayloadV2") ;
    // 4️⃣ Construir request HTTP para Elasticsearch
    val request = HttpRequest.newBuilder()
      .uri(URI.create(s"$esUrl/$indexName/_doc"))
      .header("Content-Type", "application/json")
      .header("Authorization", basicAuthHeader)
      .POST(HttpRequest.BodyPublishers.ofString(jsonPayloadV2))
      .build()

    // 5️⃣ Enviar la petición
    val response = httpclient.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      println(s"[OK] Documento indexado en [$indexName]: ${response.body()}")
    } else {
      println(s"[ERROR] Falló indexación en [$indexName]: ${response.statusCode()} -- ${response.body()}")
    }
  }

  override def close(): Unit = {}
}

