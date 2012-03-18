package org.plivo

import java.net.URL

import org.plivo.restapi._
import dispatch._

/**
 * Holds configuration data for the Plivo RESTAPI.
 */
trait HttpConfig {
  val plivoBase: URL
  val authId: String
  val authToken: String
  val apiVersion = "v0.1"
  lazy val authenticatedPlivoBase = url(plivoBase.toString()) as_! (authId, authToken)
  lazy val apiBase = authenticatedPlivoBase / apiVersion
  val http = new Http
}

/**
 * Low level client for the Plivo RESTAPI. Operates on PlivoOperation instances.
 */
class RestClient(
  val plivoBase: URL,
  val authId: String,
  val authToken: String)
    extends HttpConfig with util.Logging {

  import dispatch.liftjson.Js._

  def execute[R](op: PlivoOperation[R]): R = {
    val req = op.request(this)
    log.debug("Sending req {}", req)
    try {
      http(req ># { json =>
        log.debug("Plivo response:\n{}", json)
        op.parser.apply(json)
      })
    } catch {
      case e: dispatch.StatusCode =>
        throw new PlivoException(e.code, e.contents)
    }
  }
}

class PlivoException(
  val code: Int,
  val message: String)
    extends RuntimeException("Error code " + code + ". " + message)

/**
 * Client service interface hiding REST operation details.
 */
class PlivoClient(private val restClient: RestClient) {

  def execute[R](op: PlivoOperation[R]): R =
    restClient.execute(op)

  def call(call: CallOperation): RequestStatus =
    restClient.execute(call)

  def hangup(hangup: HangupOperation): RequestStatus =
    restClient.execute(hangup)
}

object PlivoClient {
  def apply() =
    new PlivoClient(new RestClient(
      new URL(System.getenv("PLIVO_BASE_URL")),
      System.getenv("PLIVO_AUTH_ID"),
      System.getenv("PLIVO_AUTH_TOKEN")))

  def apply(plivoBase: URL, authId: String, authToken: String) =
    new PlivoClient(new RestClient(plivoBase, authId, authToken))
}
