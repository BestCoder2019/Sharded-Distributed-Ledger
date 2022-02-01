package com.example.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class BadTransactionException(statusCode: HttpStatus, exception_reason: String)
    : ResponseStatusException(statusCode,exception_reason)