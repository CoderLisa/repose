package org.gdeproxy

/**
 * A simple HTTP Request, with method, path, headers, and body.
 */
class Request {

    def method
    def path
    def headers
    def body

    String toString() {
        sprintf('Request(method=%r, path=%r, headers=%r, body=%r)', method, path, headers, body);
    }

}
