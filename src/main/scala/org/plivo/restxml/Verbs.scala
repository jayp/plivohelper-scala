package org.plivo.restxml

import org.plivo.Phonenumber
import java.net.URL
import scala.collection.mutable.ListBuffer

/**
 * Defines a response to an incoming voice call.
 */
case class Response(verbs: Verb*)

object EmptyResponse extends Response()

/**
 * Trait for all ResponseXML Verbs.
 */
sealed trait Verb

object Verb {
  implicit def singleVerb2VoiceResponse(v: Verb): Response =
    Response(v)
}

trait NestableVerb extends Verb

/**
 * Say something to the caller using TTS.
 */
case class Speak(
  what: String,
  loop: Int = 1)
    extends NestableVerb

/**
 * Play an audio recording to the caller.
 */
case class Play(audioUrl: String,
                loop: Int = 1)
    extends NestableVerb

case class GetDigits(onInput: Option[URL] = None, // "action" attribute
                     method: Method = Method.Post,
                     timeout: Int = 5,
                     finishOnKey: Char = '#',
                     numDigits: Int = 99,
                     retries: Int = 1,
                     playBeep: Boolean = false,
                     validDigits: String = "1234567890*#",
                     invalidDigitsSound: Option[URL] = None,
                     nestedVerbs: List[NestableVerb] = List())
    extends Verb

/**
 * Dials a number. Can be used both as a op and a response.
 */
case class Dial(numbers: List[Number],
                onEnd: Option[URL] = None, // "action" attribute
                method: Method = Method.Post,
                hangupOnStar: Boolean = false,
                timeLimit: Int = 14400,
                timeout: Option[Int] = None,
                callerId: Option[Phonenumber] = None,
                callerName: Option[String] = None,
                confirmSound: Option[URL] = None,
                confirmKey: Option[Char] = None,
                dialMusic: Option[URL] = None,
                callbackUrl: Option[URL] = None,
                callbackMethod: Method = Method.Post,
                redirect: Boolean = true,
                digitsMatch: Option[String] = None)
    extends Verb

case class Number(number: Phonenumber,
                  gateways: List[String],
                  sendDigits: Option[String] = None,
                  sendOnPreanswer: Boolean = false,
                  gatewayCodecs: Option[List[String]] = None,
                  gatewayTimeouts: Option[List[Int]] = None,
                  gatewayRetries: Option[List[Int]] = None,
                  extraDialString: Option[String] = None)
    extends Verb {

  def toList: List[Number] =
    List(this) // list with single item
}

object Number {

  private val defaultGateways =
    ListBuffer[String]()

  def addDefaultGateway(gateway: String) =
    defaultGateways += gateway

  def apply(number: String): Number = {
    require(defaultGateways.size >= 1) // at least one gateway must be defined
    new Number(Phonenumber(number), defaultGateways.toList)
  }
}

case class Hangup(reason: Option[Reason] = None,
                  schedule: Option[Int] = None)
    extends Verb

case class Redirect(to: URL,
                    method: Method = Method.Post)
    extends Verb

case class Wait(length: Int = 1)
  extends NestableVerb

//////////////////////

abstract case class Method(value: String) {
  override def toString: String = value
}

abstract case class Reason(value: String) {
  override def toString: String = value
}

object Method {
  object Get extends Method("GET")
  object Post extends Method("POST")
}

object Reason {
  object Busy extends Reason("busy")
  object Rejected extends Reason("rejected")
}
