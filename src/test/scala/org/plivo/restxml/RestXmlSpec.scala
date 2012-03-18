package org.plivo.restxml

import java.net.URL
import org.specs.Specification
import org.plivo._
import scala.xml._
import in.org.patel.xmldiff._

object RestXmlSpec extends Specification {

  val comp = new Comparison

  "Library" should {

    "generate correct RESTXML for Dial verb" in {
      Number.addDefaultGateway("sofia/gateway/att/")
      val r = Dial(numbers = Number("12125551234").toList)
      val generated = RestXml(r)
      println(generated)
      val expected =
        <Dial method="POST" timeLimit="14400" timeout="None"
          redirect="true" callbackMethod="POST" hangupOnStar="false">
        <Number gateways="sofia/gateway/att/" sendOnPreanswer="false">
          12125551234
        </Number>
      </Dial>
      comp(expected, generated) must_== NoDiff
    }

    "generate correct RESTXML for GetDigits verb" in {
      val r = GetDigits(onInput = Some(new URL("http://digits/input.xml")),
        timeout = 30, finishOnKey = '*', numDigits = 4)
      val generated = RestXml(r)
      val expected = <GetDigits action="http://digits/input.xml" finishOnKey="*" numDigits="4" timeout="30" />
      comp(expected, generated) must_== NoDiff
    }
  }
}
