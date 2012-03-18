package org.plivo

import org.specs.Specification
import java.lang.IllegalArgumentException

object PhonenumberSpec extends Specification {

  val pn = new Phonenumber("47", "90055383")

  "Phonenumber" should {
    "accept valid numbers in E.164 format" in {
      Phonenumber("+4790055383") must_== pn
    }
    "accept valid numbers in classic format" in {
      Phonenumber("4790055383") must_== pn
    }
    "not accept invalid numbers" in {
      Phonenumber("foobar") must throwAn[IllegalArgumentException]
    }
    "format itself to E.164 as default" in {
      pn.toString must_== "+4790055383"
    }
    "format itself in classic format" in {
      pn.toClassicFormat must_== "4790055383"
    }
  }
}
