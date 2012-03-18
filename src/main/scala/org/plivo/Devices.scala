package org.plivo

import callback._
import restxml._
import util._

/**
 * Represents a phone device. Must be mixed with a CallbackManager to handle
 * callback functions. See scwilio.uf.UnfilteredPhone for an example.
 */
trait Phone { self: CallbackManager with Logging =>

  /**
   * Set this to handle incoming calls.
   */
  def incomingCallHandler: Option[(ActiveCall) => Response] = None

  /**
   * Called when an incoming call arrives.
   */
  def handleIncomingCall(call: ActiveCall): Response = {
    log.debug("Incoming call: " + call)
    incomingCallHandler match {
      case Some(f) => f.apply(call)
      case _ => Speak("Hello, thanks for calling, but " +
        "incoming calls are not supported by this server.")
    }
  }

  /**
   * Callback for active calls.
   */
  def handleActiveCall(callbackId: String,
                       call: ActiveCall): Response = {
    log.debug("Call connected: " + call)
    unregister[ActiveCall, Response](callbackId) match {
      case Some(callback) => callback.apply(call)
      case _ => Speak("Sorry, an error has occured. " +
        "Do not know how to handle this call.")
    }
  }

  /**
   * Callback when a call ends.
   */
  def handleCompletedCall(callbackId: String,
                          outcome: CompletedCall): Unit = {
    log.debug("Call completed: " + outcome)
    unregister[CompletedCall, Unit](callbackId) match {
      case Some(callback) => callback.apply(outcome)
      case _              => log.warn("No handler for call end " + callbackId)
    }
  }

  /**
   * Callback when an outgoing dial (i.e., via <Dial/>) call ends.
   */
  def handleCompletedDial(callbackId: String,
                          outcome: CompletedDial): Response = {
    log.debug("Dial completed: " + outcome)
    unregister[CompletedDial, Response](callbackId) match {
      case Some(callback) => callback.apply(outcome)
      case _ => Speak("Sorry, an error has occured. " +
        "Do not know how to handle this call.")
    }
  }

  /**
   * Callback when a <Dial/> ends.
   */
  def handleDialCallback(callbackId: String,
                         call: DialCallback): Unit = {
    log.debug("Dial callback: " + call)
    unregister[DialCallback, Unit](callbackId) match {
      case Some(callback) => callback.apply(call)
      case _ => log.warn("No handler for dial callback " +
        callbackId)
    }
  }
}
