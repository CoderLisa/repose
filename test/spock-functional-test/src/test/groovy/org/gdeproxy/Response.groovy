package org.gdeproxy

import org.apache.http.Header

class Response {

    def code
    def message
    def Header[] headers
    def String body

    def getHeader(String name) {
        def String foundValue

        headers.each { header ->
            if (header.name.equals(name)) {
                foundValue = header.value
            }
        }
        return foundValue
    }

    @Override
    String toString() {
        sprintf('Response(code=%r, message=%r, headers=%r, body=%r)', code, message, headers, body)
    }
}
