package org.gdeproxy

/**
 *  An object containing the initial request sent via the make_request method,
 *  and all request/response pairs (Handling objects) processed by
 *  DeproxyEndpoint objects.
 */
class MessageChain {

    def sentRequest
    def receivedResponse
    def handlerFunction
    def handlings = []
    def orphanedHandlings = []

    def addHandling(handling) {
        this.handlings.add(handling)
    }

    def addOrphanedHandling(handling) {
        this.orphanedHandlings.add(handling)
    }

    String toString() {
        sprintf('MessageChain(handler_function=%r, sent_request=%r, handlings=%r, received_response=%r, orphaned_handlings=%r)',
                handlerFunction, sentRequest, handlings, receivedResponse, orphanedHandlings)
    }
}