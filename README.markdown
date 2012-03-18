# Plivo Scala helper Library

Plivo Scala help Library is a Scala API for the Plivo framework. It is heavily
derived from Dag Liodden's Scwilio library. It is currently under initial
development, with only a small subset of the Plivo functionality supported. The
API aims to deliver Plivo functionality in several layers of abstraction:

1. Basic Plivo methods and RESTXML generation
2. Higher level "phone devices" where all HTTP and URL plumbing is abstracted
   away and replace with plain functions

## Basic Plivo methods and RESTXML generation

To invoke Plivo methods, get an `PlivoClient` instance:

    import org.plivo._
    val client = PlivoClient(PLIVO_BASE_URL, AUTH_ID, AUTH_TOKEN)

Then, invoke a method, e.g. for dialing a number or send an SMS:

    val call =  CallOperation(from = "12125551234",
                              to = "12125550001",
                              gateways = List("sofia/gateway/att/"),
                              answerUrl = new URL("http://example.com/play.xml"))
    client.execute(call)

To generate some RESTML, put this in a handler of whatever web framework you
are using:

    import org.plivo.restxml._

    val response = Response(
       Speak("This is an automated call. You will now hear some cow bells."),
       Wait(2),
      Speak("http://example.com/cowbell.mp3"))
    val stringResponse = ResponseXml(response).toString
    // Write the response

## Phone devices

Making stateful Plivo services can be a pain. Using `Phone` instances, the HTTP
plumbing can can be removed completely. This allows for code like this:


    val port = 8181
    val plivoClient = PlivoClient(new URL("http://127.0.0.1:8088/"),
      "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
    val phone = new UnfilteredPhone(port, new URL("http://localhost:8181/"))
    phone.activate()

    import phone.URLMaker._

    Number.addDefaultGateway("sofia/gateway/att/")

    val call = CallOperation(from = "12125550001",
      to = "12125551234",
      gateways = List("sofia/gateway/att/"),
      answerUrl = (call: ActiveCall) => {
        Response(
         Speak("This is an automated call. You will now hear some cow bells."),
         Wait(2),
         Speak("http://example.com/cowbell.mp3"))
      })

    plivoClient.execute(call)

Neat, huh?
