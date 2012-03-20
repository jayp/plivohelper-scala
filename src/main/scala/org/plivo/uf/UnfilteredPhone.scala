package org.plivo.uf

import java.net.URL

import org.plivo.callback.callbackFunctions._
import org.plivo.callback._
import org.plivo.restxml._
import org.plivo.util.Logging
import org.plivo._

import Helpers._
import ResponseDocument._
import unfiltered.filter._
import unfiltered.request._
import unfiltered.response._

/**
 * An implementation of a Phone device using the Unfiltered Jetty implementation.
 */
class UnfilteredPhone(
  val port: Int,
  val absoluteBaseUrl: Option[URL] = None)
    extends Phone with InMemoryCallbackManager with Logging {

  def callBackPlan = CallbackPlan()

  def activate() {
    unfiltered.jetty.Http(port).filter(callBackPlan).start
  }
  /**
   * Unfiltered plan for handling callbacks
   */
  object CallbackPlan {
    def apply() = Planify(intent)

    def intent: Plan.Intent = {

      case POST(Path(Seg("incoming" :: "voice" :: Nil)) & Params(p)) =>
        handleIncomingCall(ActiveCall.parse(p))

      case POST(Path(Seg("callback" :: "active-call" :: callbackId :: Nil)) & Params(p)) =>
        handleActiveCall(callbackId, ActiveCall.parse(p))

      case POST(Path(Seg("callback" :: "completed-call" :: callbackId :: Nil)) & Params(p)) =>
        handleCompletedCall(callbackId, CompletedCall.parse(p))
        Ok

      case POST(Path(Seg("callback" :: "completed-dial" :: callbackId :: Nil)) & Params(p)) =>
        handleCompletedDial(callbackId, CompletedDial.parse(p))

      case POST(Path(Seg("callback" :: "dial-callback" :: callbackId :: Nil)) & Params(p)) =>
        handleDialCallback(callbackId, DialCallback.parse(p))
        Ok
    }
  }

  private def makeUrl(callbackId: String): Option[URL] = {
    val baseUrlString = absoluteBaseUrl match {
      case Some(url) => {
        val urlString = url.toString
        urlString + (if (urlString.endsWith("/")) "" else "/")
      }
      case None => "http://localhost:" + port + "/"
    }
    Some(new URL(baseUrlString + callbackId))
  }

  /**
   * Implicit conversions for converting callback functions into URLs which
   * match the Unfiltered Plan.
   */
  object URLMaker {

    implicit def activeCallHandler2UrlOption(f: ActiveCallHandler): Option[URL] =
      makeUrl("callback/active-call/" + register(f))

    implicit def activeCallHandler2Url(f: ActiveCallHandler): URL =
      activeCallHandler2UrlOption(f).get

    implicit def callCompletedNotifier2Url(f: CompletedCallNotifier): Option[URL] =
      makeUrl("callback/completed-call/" + register(f))

    implicit def CompletedDialHandler2Url(f: CompletedDialHandler): Option[URL] =
      makeUrl("callback/completed-dial/" + register(f))

    implicit def dialCallbackNotifier2Url(f: DialCallbackNotifier): Option[URL] =
      makeUrl("callback/dial-callback/" + register(f, 2))
  }
}

