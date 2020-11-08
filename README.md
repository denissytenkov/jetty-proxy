####About
Quick sketch of jetty + async client proxy server. The server will proxy a request to the host provided in x-destination header with url of initial request.

####How to run
Start server with gradle run

Send a request with curl -v -H"x-destination:vertx.io" "http://localhost:8090/docs/vertx-core/java/"