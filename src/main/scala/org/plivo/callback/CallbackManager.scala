package org.plivo.callback

import java.net.URL
import org.plivo.restxml.Response
import java.util.concurrent.atomic.AtomicLong
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object callbackFunctions {
  type CallbackFunc[T <: CallbackEvent, R] = (T) => R
  type ActiveCallHandler = (ActiveCall) => Response
  type CompletedCallNotifier = (CompletedCall) => Unit
  type CompletedDialHandler = (CompletedDial) => Response
  type DialCallbackNotifier = (DialCallback) => Unit
}

/**
 * Manages references between uniquely generated URLs and functions to manage
 * Plivo callback references.
 */
trait CallbackManager {
  import callbackFunctions._

  def register[T <: CallbackEvent, R](cbFunction: CallbackFunc[T, R], count: Int = 1): String

  def unregister[T <: CallbackEvent, R](callbackId: String): Option[CallbackFunc[T, R]]
}

trait InMemoryCallbackManager extends CallbackManager {
  import callbackFunctions._

  private val log = LoggerFactory.getLogger(getClass)

  /* TODO: add a "waiting_since" field. Use it to evict old callback handlers,
   * as stray callback handlers may add to memory pressure in a long-running
   * daemon. This may happen when the plivo doesn't invoke a callback. This may
   * happen for a variety of reasons, including some errors withing Plivo. I
   * believe Plivo doesn't perform the callback on the "action" attribute of
   * the <Dial/> RESTXML operation if the caller hangs up during the <Dial/>
   * operation (claim needs further investigation).
   */
  private val callbacks =
    new ConcurrentHashMap[String, Tuple2[CallbackFunc[_, _], Int]]

  private val counter =
    new AtomicLong(System.currentTimeMillis)

  private def generateId(): String =
    counter.addAndGet(1).toString // + RandomNumber(1000, 9999).toString // TODO

  override def register[T <: CallbackEvent, R](cbFunction: CallbackFunc[T, R], count: Int = 1): String = {
    val callbackId = generateId()
    callbacks.put(callbackId, (cbFunction, count))
    callbackId
  }

  override def unregister[T <: CallbackEvent, R](callbackId: String): Option[CallbackFunc[T, R]] = {
    callbacks.get(callbackId) match {
      case tuple: Tuple2[CallbackFunc[T, R], Int] => {
        val (cbFunction, count) = tuple
        val decrCount = count - 1
        if (decrCount == 0) {
          callbacks.remove(callbackId)
        } else if (decrCount >= 1) {
          log.debug("Reinserting " + callbackId + " in callbacks map; count = " + decrCount)
          callbacks.put(callbackId, (cbFunction, decrCount))
        }
        Some(cbFunction)
      }
      case _ => None
    }
  }
}
