package org.gdeproxy

/**
 * The main class.
 */
class Deproxy {

    String REQUEST_ID_HEADER_NAME = 'Deproxy-Request-ID'
    def messageChainsLock = threading.Lock()
    def messageChains = dict()
    def endpointLock = threading.Lock()
    def DeproxyEndpoint[] endpoints = []


    Deproxy() {

    }

}

//    def make_request(self, url, method='GET', headers=None, request_body='',
//                     handler_function=default_handler,
//                     add_default_headers=True):
//        """Make an HTTP request to the given url and return a MessageChain."""
//        logger.debug('')
//
//        if headers is None:
//            headers = {}
//        else:
//            # if the caller passes in a headers object to specify what http
//            # headers to send, we need to copy it in order to avoid modifying
//            # it. Also, if the caller passes in the same object multiple times,
//            # subsequent calls to try_add_value_case_insensitive wouldn't
//            # change the values in the dictionary.
//            headers = dict(headers)
//
//        request_id = str(uuid.uuid4())
//        try_add_value_case_insensitive(headers, request_id_header_name,
//                                       request_id)
//
//        message_chain = MessageChain(handler_function)
//        self.add_message_chain(request_id, message_chain)
//
//        urlparts = list(urlparse.urlsplit(url, 'http'))
//        scheme = urlparts[0]
//        host = urlparts[1]
//        urlparts[0] = ''
//        urlparts[1] = ''
//        path = urlparse.urlunsplit(urlparts)
//
//        if add_default_headers:
//            try_add_value_case_insensitive(headers, 'Host', host)
//            try_add_value_case_insensitive(headers, 'Accept', '*/*')
//            try_add_value_case_insensitive(headers, 'Accept-Encoding',
//                                           'identity, deflate, compress, gzip')
//            try_add_value_case_insensitive(headers, 'User-Agent',
//                                           version_string)
//
//        request = Request(method, path, headers, request_body)
//
//        response = self.send_request(scheme, host, request)
//
//        self.remove_message_chain(request_id)
//
//        message_chain.sent_request = request
//        message_chain.received_response = response
//
//        return message_chain
//
//    def send_request(self, scheme, host, request):
//        """Send the given request to the host and return the Response."""
//        logger.debug('sending request (scheme="%s", host="%s")' %
//                     (scheme, host))
//        hostparts = host.split(':')
//        if len(hostparts) > 1:
//            port = hostparts[1]
//        else:
//            if scheme == 'https':
//                port = 443
//            else:
//                port = 80
//        hostname = hostparts[0]
//        hostip = socket.gethostbyname(hostname)
//
//        request_line = '%s %s HTTP/1.1\r\n' % (request.method, request.path)
//        lines = [request_line]
//
//        for name, value in request.headers.iteritems():
//            lines.append('%s: %s\r\n' % (name, value))
//        lines.append('\r\n')
//        lines.append(request.body)
//        lines.append('\r\n')
//        lines.append('\r\n')
//
//        #for line in lines:
//        # logger.debug(' ' + line)
//
//        logger.debug('Creating connection (hostname="%s", port="%s")' %
//                     (hostname, str(port)))
//        s = socket.create_connection((hostname, port))
//        s.send(''.join(lines))
//
//        rfile = s.makefile('rb', -1)
//
//        logger.debug('Reading response line')
//        response_line = rfile.readline(65537)
//        if (len(response_line) > 65536):
//            raise ValueError
//        logger.debug('Response line is ok: %s' % response_line)
//
//        words = response_line.split()
//
//        proto = words[0]
//        code = words[1]
//        message = ' '.join(words[2:])
//
//        logger.debug('Reading headers')
//        response_headers = dict(mimetools.Message(rfile, 0))
//        logger.debug('Headers ok')
//
//        logger.debug('Creating Response object')
//        response = Response(code, message, response_headers, rfile)
//
//        logger.debug('Returning Response object')
//        return response
//
//    def add_endpoint(self, server_address, name=None):
//        """Add a DeproxyEndpoint object to this Deproxy object's list of
//endpoints, giving it the specified server address, and then return the
//endpoint."""
//        logger.debug('')
//        endpoint = None
//        with self._endpoint_lock:
//            if name is None:
//                name = 'Endpoint-%i' % len(self._endpoints)
//            endpoint = DeproxyEndpoint(self, server_address, name)
//            self._endpoints.append(endpoint)
//            return endpoint
//
//    def _remove_endpoint(self, endpoint):
//        """Remove a DeproxyEndpoint from the list of endpoints. Returns True if
//the endpoint was removed, or False if the endpoint was not in the list.
//This method should normally not be called by user code. Instead, call
//the endpoint's shutdown method."""
//        logger.debug('')
//        with self._endpoint_lock:
//            count = len(self._endpoints)
//            self._endpoints = [e for e in self._endpoints if e != endpoint]
//            return (count != len(self._endpoints))
//
//    def shutdown_all_endpoints(self):
//        """Shutdown and remove all endpoints in use."""
//        logger.debug('')
//        endpoints = []
//        with self._endpoint_lock:
//            endpoints = list(self._endpoints)
//        # be sure we're not holding the lock when shutdown calls
//        # _remove_endpoint.
//        for e in endpoints:
//            e.shutdown()
//
//    def add_message_chain(self, request_id, message_chain):
//        """Add a MessageChain to the internal list for the given request ID."""
//        logger.debug('request_id = %s' % request_id)
//        with self._message_chains_lock:
//            self._message_chains[request_id] = message_chain
//
//    def remove_message_chain(self, request_id):
//        """Remove a particular MessageChain from the internal list."""
//        logger.debug('request_id = %s' % request_id)
//        with self._message_chains_lock:
//            del self._message_chains[request_id]
//
//    def get_message_chain(self, request_id):
//        """Return the MessageChain for the given request ID."""
//        logger.debug('request_id = %s' % request_id)
//        with self._message_chains_lock:
//            if request_id in self._message_chains:
//                return self._message_chains[request_id]
//            else:
//                #logger.debug('no message chain found for request_id %s' %
//                # request_id)
//                #for rid, mc in self._message_chains.iteritems():
//                # logger.debug(' %s - %s' % (rid, mc))
//                return None
//
//    def add_orphaned_handling(self, handling):
//        """Add the handling to all available MessageChains."""
//        logger.debug('Adding orphaned handling')
//        with self._message_chains_lock:
//            for mc in self._message_chains.itervalues():
//                mc.add_orphaned_handling(handling)
//
//
