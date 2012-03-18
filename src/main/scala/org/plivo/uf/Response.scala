package org.plivo.uf

import unfiltered.response._
import org.plivo.restxml._

/**
 *  Unfiltered responder
 */
case class ResponseDocument(r: Response)
  extends ChainResponse(
    ContentType("application/xml") ~> ResponseString(RestXml(r).toString))

object ResponseDocument {

  implicit def response2ResponseDoc(r: Response): ResponseDocument =
    ResponseDocument(r)

  implicit def verb2ResponseDoc(v: Verb): ResponseDocument =
    ResponseDocument(Response(v))
}
