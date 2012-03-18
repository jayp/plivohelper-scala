# Plivo Scala helper Library

Plivo Scala helper Library is a Scala API for the Plivo framework. It is
heavily derived from Dag Liodden's [Scwilio
library](https://github.com/daggerrz/Scwilio). It is currently under initial
development, with only a small subset of the Plivo functionality supported. The
API aims to deliver Plivo functionality in several layers of abstraction:

1. Basic Plivo methods and RESTXML generation 2. Higher level "phone devices"
where all HTTP and URL plumbing is abstracted away and replace with plain
functions

## Basic Plivo methods and RESTXML generation

To invoke Plivo methods, get an `PlivoClient` instance:

    import org.plivo._

    val plivoClient = PlivoClient(new URL("http://127.0.0.1:8088/"),
      "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")

Then invoke a method, e.g. for dialing a number:

    val call =  CallOperation(from = "12125550001",
                              to = "12125551234",
                              gateways = List("sofia/gateway/att/"),
                              answerUrl = new URL("http://example.com/answer.xml"))

    plivoClient.execute(call)

To generate some RESTXML, put this in a handler of whatever web framework you
are using:

    import org.plivo.restxml._

    val response = Response(
       Speak("Hi. This is an automated call."),
       Wait(2),
       Play(new URL("http://example.com/cowbell.mp3")))

    val stringResponse = ResponseXml(response).toString

## Phone devices

Making stateful Plivo services can be a pain. Using `Phone` instances, the HTTP
plumbing can can be removed completely. This allows for code like this:

    import org.plivo._
    import org.plivo.uf._

    val plivoClient = PlivoClient(new URL("http://127.0.0.1:8088/"),
      "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")

    // active phone instance to receive Plivo callbacks
    val port = 8181
    val phone = new UnfilteredPhone(port, new URL("http://localhost:8181/"))
    phone.activate()

    // implicitly convert lamda functions to URLs
    import phone.URLMaker._

    val call = CallOperation(from = "12125550001",
                             to = "12125551234",
                             gateways = List("sofia/gateway/att/"),
                             answerUrl = (call: ActiveCall) => {
                               Response(
                                 Speak("You will now hear some cow bells."),
                                 Wait(2),
                                 Play(new URL("http://example.com/cowbell.mp3")))
                             })

    plivoClient.execute(call)

Neat, huh?
