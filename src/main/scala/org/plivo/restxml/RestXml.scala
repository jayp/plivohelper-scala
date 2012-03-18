package org.plivo.restxml

import scala.xml._

/**
 * Produces RESTXML from plivo action verbs.
 */
object RestXml {

  def apply(response: Response): Elem =
    <Response>
      { for (verb <- response.verbs) yield RestXml(verb) }
    </Response>

  def apply(verb: Verb): Elem =
    verb match {

      case s: Speak =>
        <Speak loop={ s.loop.toString }>{ s.what }</Speak>

      case p: Play =>
        <Play loop={ p.loop.toString }>{ p.audioUrl }</Play>

      case g: GetDigits =>
        <GetDigits action={ optional(g.onInput) }
            method={ g.method.toString }
            timeout={ g.timeout.toString }
            finishOnKey={ g.finishOnKey.toString }
            numDigits={ g.numDigits.toString }
            retries={ g.retries.toString }
            playBeep={ g.playBeep.toString }
            validDigits={ g.validDigits }
            invalidDigitsSounds={ optional(g.invalidDigitsSound) }>
          { for (verb <- g.nestedVerbs) yield RestXml(verb) }
        </GetDigits>

      case d: Dial =>
        <Dial action={ optional(d.onEnd) }
            method={ d.method.toString }
            hangupOnStar={ d.hangupOnStar.toString }
            timeLimit={ d.timeLimit.toString }
            timeout={ d.timeout.toString }
            callerId={ d.callerId map { p => xml.Text(p.toClassicFormat) } }
            callerName={ optional(d.callerName) }
            confirmSound={ optional(d.confirmSound) }
            confirmKey={ optional(d.confirmKey) }
            dialMusic={ optional(d.dialMusic) }
            callbackUrl={ optional(d.callbackUrl) }
            callbackMethod={ d.callbackMethod.toString }
            redirect={ d.redirect.toString }
            digitsMatch={ optional(d.digitsMatch) }>
          { for (number <- d.numbers) yield RestXml(number) }
        </Dial>

      case n: Number =>
        <Number sendDigits={ n.sendDigits map { xml.Text(_) } }
          sendOnPreanswer={ n.sendOnPreanswer.toString }
          gateways={ n.gateways.mkString(",") }
          gatewayCodes={ n.gatewayCodecs map { l => xml.Text(l.mkString(",")) } }
          gatewayTimeouts={ n.gatewayTimeouts map { l => xml.Text(l.mkString(",")) } }
          gatewayRetries={ n.gatewayRetries map { l => xml.Text(l.mkString(",")) } }
          extraDialString={ optional(n.extraDialString) }>
          { n.number.toClassicFormat }
        </Number>

      case h: Hangup =>
        <Hangup reason={ optional(h.reason) } schedule={ optional(h.schedule) } />

      case r: Redirect =>
        <Redirect method={ r.method.toString }>{ r.to.toString }</Redirect>

      case Wait(length) =>
        <Waith length={ length.toString }/>
    }

  private def optional[R](param: Option[R]): Option[xml.Text] =
    param map { r: R => xml.Text(r.toString) }
}
