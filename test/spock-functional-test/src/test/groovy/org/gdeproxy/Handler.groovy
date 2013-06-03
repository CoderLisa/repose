package org.gdeproxy


class Handler {

    // Handler function.
    // Returns a 200 OK Response, with no additional headers or response body.
    def default_handler(request) {
        return Response(200, 'OK', {}, '')
    }

    // Handler function.
    // Returns a 200 OK Response, with the same headers and body as the request.
    def echo_handler(request) {
        return Response(200, 'OK', request.headers, request.body)
    }

}