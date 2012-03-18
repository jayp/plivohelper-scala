package org.plivo.restapi

import org.plivo.{ HttpConfig, Phonenumber }
import java.net.URL

/**
 * Trait for all plivo's RESTAPI operations. Each operation is responsible for
 * creating the HTTP Request and parsing the results (if the operation was
 * successful).
 *
 * One might argue that the ops themselves should not be responsible for the
 * plumbing, but this is simple and easy to understand, so we'll keep it like
 * this for now.
 */
trait PlivoOperation[R] {

  /**
   * Create a dispatch request for the op.
   */
  def request(conf: HttpConfig): dispatch.Request

  /**
   * Return a function to parse the result for the op.
   */
  def parser: net.liftweb.json.JValue => R
}

case class RequestStatus(success: Boolean,
                         message: String,
                         requestUuid: Option[String])

/**
 * Dial a phone number.
 *
 * <b>Note:<b> We do not support IfMachine -> Hangup as this breaks
 * our state management. If Hangup were allowed,
 * we would get "active call" callbacks with state Completed
 * which is weird, and we would also not get a callback when the call
 * is completed which is inconsistent. If machine detection is used,
 * the response function must check the answeredBy field and rather
 * issue a Hangup manually.
 */
case class CallOperation(from: Phonenumber,
                         to: Phonenumber,
                         answerUrl: URL,
                         gateways: List[String],
                         callerName: Option[String] = None,
                         hangupUrl: Option[URL] = None,
                         ringUrl: Option[URL] = None,
                         extraDialString: Option[String] = None,
                         gatewayCodecs: Option[List[String]] = None,
                         gatewayTimeouts: Option[List[Int]] = None,
                         gatewayRetries: Option[List[Int]] = None,
                         sendDigits: Option[String] = None,
                         sendOnPreanswer: Option[Boolean] = None,
                         timeLimit: Option[Int] = None,
                         hangupOnRing: Option[Int] = None)
    extends PlivoOperation[RequestStatus] {

  def request(conf: HttpConfig) = {
    var params: Map[String, String] = Map(
      "From" -> from.toClassicFormat,
      "To" -> to.toClassicFormat,
      "AnswerUrl" -> answerUrl.toString,
      "Gateways" -> gateways.mkString(","))
    callerName.foreach { params += "CallerName" -> _ }
    hangupUrl.foreach { params += "HangupUrl" -> _.toString }
    ringUrl.foreach { params += "RingUrl" -> _.toString }
    extraDialString.foreach { params += "ExtraDialString" -> _ }
    gatewayCodecs.foreach { list =>
      assert(list.length == gateways.length)
      params += "GatewayCodecs" -> list.mkString(",")
    }
    gatewayTimeouts.foreach { list =>
      assert(list.length == gateways.length)
      params += "GatewayTimeouts" -> list.mkString(",")
    }
    gatewayRetries.foreach { list =>
      assert(list.length == gateways.length)
      params += "GatewayRetries" -> list.mkString(",")
    }
    sendDigits.foreach { params += "SendDigits" -> _ }
    sendOnPreanswer.foreach { params += "SendOnPreanswer" -> _.toString }
    timeLimit.foreach { params += "TimeLimit" -> _.toString }
    hangupOnRing.foreach { params += "HangupOnRing" -> _.toString }

    conf.apiBase / "Call/" << params
  }

  def parser = ParseRequestStatus.parse
}

case class HangupOperation(uuidType: UuidType,
                           uuid: String)
    extends PlivoOperation[RequestStatus] {

  def request(conf: HttpConfig) = {
    val paramType = if (uuidType == UuidType.Call) "CallUUID" else "RequestUUID"
    val params: Map[String, String] = Map(
      paramType -> uuid)

    conf.apiBase / "HangupCall/" << params
  }

  def parser = ParseRequestStatus.parse
}

abstract case class UuidType(value: String)

object UuidType {
  object Request extends UuidType("request")
  object Call extends UuidType("call")
}

object ParseRequestStatus {

  import net.liftweb.json._
  import net.liftweb.json.JsonParser._
  implicit val formats = DefaultFormats

  def parse(json: net.liftweb.json.JValue): RequestStatus =
    (json transform {
      case JField("Success", value)     => JField("success", value)
      case JField("Message", value)     => JField("message", value)
      case JField("RequestUUID", value) => JField("requestUuid", value)
    }).extract[RequestStatus]
}
