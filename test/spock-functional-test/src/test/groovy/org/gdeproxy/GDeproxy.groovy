package org.gdeproxy

import org.gdeproxy.http.SimpleHttpClient

class GDeproxy extends SimpleHttpClient {

    String REQUEST_ID_HEADER_NAME = 'Deproxy-Request-ID'
    def Map<String, MessageChain> messageChains = new HashMap()
    def List<DeproxyEndpoint> endpoints = new ArrayList()

    GDeproxy(String endpoint) {
        super(endpoint)
    }

    /**
     * Make an HTTP request to the given url and return a MessageChain.
     */
    def makeRequest(path, method = 'GET', headers, requestBody, handlerFunction) {

        def requestId = UUID.randomUUID().toString()
        MessageChain messageChain = new MessageChain(handlerFunction)
        addMessageChain(requestId, messageChain)


        Request request = new Request(method, path, headers, requestBody)
        Response response

        switch (method) {
            case 'GET':
                response = this.doGet(path, headers)
                break
            case 'POST':
                response = this.doPost(path, headers, requestBody)
                break
            case 'PUT':
                response = this.doPut(path, headers, requestBody)
                break
            case 'DELETE':
                response = this.doDelete(path, headers, requestBody)
                break
            default:
                throw new UnsupportedOperationException("GDeproxy does not yet support ${method} requests")
        }

        this.removeMessageChain(requestId)
        messageChain.sentRequest = request
        messageChain.receivedResponse = response

        messageChain
    }

    def addEndpoint(serverAddress, name = "") {
        if (!name) {
            name = 'Endpoint-' + endpoints.size() // POSSIBLE BUG: should this be size+1 ?
        }
        def endpoint = new DeproxyEndpoint(this, serverAddress, name)
        endpoints.add(endpoint)
        return endpoint
    }

    def shutdownAllEndpoints() {
        endpoints.each { e ->
            e.shutdown()
        }
    }


    def addMessageChain(requestId, messageChain) {
        messageChains.put(requestId, messageChain)
    }

    def removeMessageChain(requestId) {
        messageChains.remove(requestId)
    }

    def getMessageChain(requestId) {
        return messageChains.get(requestId)
    }

    def addOrphanedHandling(handling) {
        messageChains.each { id, mc ->
            mc.addOrphanedHandling(handling)
        }
    }

}
