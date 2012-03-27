package org.plivo.callback

import org.plivo.Phonenumber

/**
 * Trait for all Plivo triggered callback events such.
 */
sealed trait CallbackEvent

/**
 *  Represents an active, ongoing call. In- or out-going.
 */
case class ActiveCall(
  callUuid: String,
  from: Phonenumber,
  to: Phonenumber,
  callerName: String,
  forwardedFrom: Option[Phonenumber],
  callStatus: Option[CallStatus],
  direction: String,
  scheduledHangupId: Option[String],
  aLegUuid: Option[String],
  aLegRequestUuid: Option[String],
  digits: Option[String])
    extends CallbackEvent

object ActiveCall {
  def parse(p: Map[String, String]) = {
    ActiveCall(
      p("CallUUID"),
      Phonenumber(p("From")),
      Phonenumber(p("To")),
      p("CallerName"),
      Phonenumber(p.get("ForwardedFrom")),
      p.get("CallStatus") match {
        case Some("in-progress") => Some(InProgress)
        case Some("ringing")     => Some(Ringing)
        case Some(s)             => Some(Unknown(s))
        case None                => None
      },
      p("Direction"),
      p.get("ScheduledHangupID"),
      p.get("ALegUUID"),
      p.get("ALegRequestUUID"),
      p.get("Digits"))
  }
}

case class CompletedCall(
  callUuid: String,
  from: Phonenumber,
  to: Phonenumber,
  callerName: Option[String], // Plivo bug? ActiveCall vs. CompletedCall
  forwardedFrom: Option[Phonenumber],
  direction: String,
  scheduledHangupId: Option[String],
  aLegUuid: Option[String],
  aLegRequestUuid: Option[String],
  hangupCause: String)
    extends CallbackEvent

object CompletedCall {
  def parse(p: Map[String, String]) = {
    CompletedCall(
      p("CallUUID"),
      Phonenumber(p("From")),
      Phonenumber(p("To")),
      p.get("CallerName"),
      Phonenumber(p.get("ForwardedFrom")),
      p("Direction"),
      p.get("ScheduledHangupID"),
      p.get("ALegUUID"),
      p.get("ALegRequestUUID"),
      p("HangupCause"))
  }
}

case class CompletedDial(
  callUuid: String,
  from: Phonenumber,
  to: Phonenumber,
  callerName: Option[String], // Plivo bug? ActiveCall vs. CompletedCall
  forwardedFrom: Option[Phonenumber],
  direction: String,
  scheduledHangupId: Option[String],
  aLegUuid: Option[String],
  aLegRequestUuid: Option[String],
  dialCallStatus: Option[CallStatus],
  dialRingStatus: Option[Boolean],
  dialHangupCause: Option[String],
  dialALegUuid: Option[String],
  dialBLegUuid: Option[String])
    extends CallbackEvent

object CompletedDial {
  def parse(p: Map[String, String]) = {
    CompletedDial(
      p("CallUUID"),
      Phonenumber(p("From")),
      Phonenumber(p("To")),
      p.get("CallerName"),
      Phonenumber(p.get("ForwardedFrom")),
      p("Direction"),
      p.get("ScheduledHangupID"),
      p.get("ALegUUID"),
      p.get("ALegRequestUUID"),
      p.get("DialCallStatus") match {
        case Some("in-progress") => Some(InProgress)
        case Some("ringing")     => Some(Ringing)
        case Some("completed")   => Some(Completed)
        case Some(s)             => Some(Unknown(s))
        case None                => None
      },
      p.get("DialRingStatus") map { _.toBoolean },
      p.get("DialHangupCause"),
      p.get("DialALegUUID"),
      p.get("DialBLegUUID"))
  }
}

case class DialCallback(
  callUuid: String,
  dialBLegStatus: DialStatus,
  dialALegUuid: String,
  dialBLegUuid: String,
  dialBLegHangupCause: Option[String])
    extends CallbackEvent

object DialCallback {
  def parse(p: Map[String, String]) = {
    DialCallback(
      p("CallUUID"),
      p("DialBLegStatus") match {
        case "answer" => Answered
        case "hangup" => Hungup
        case s        => Unknown(s)
      },
      p("DialALegUUID"),
      p("DialBLegUUID"),
      p.get("DialBLegHangupCause"))
  }
}

/**
 * Status of an active call.
 */
sealed trait CallStatus
case object Ringing extends CallStatus
case object InProgress extends CallStatus
case object Completed extends CallStatus

sealed trait DialStatus
case object Answered extends DialStatus
case object Hungup extends DialStatus

/**
 * Safe-guard in case Plivo extends its API.
 */
case class Unknown(msg: String)
  extends CallStatus with DialStatus

