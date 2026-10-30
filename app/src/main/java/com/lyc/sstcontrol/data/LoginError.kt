package com.lyc.sstcontrol.data

enum class LoginError {
    EMPTY_FIELDS,

    INVALID_EMAIL,
    INVALID_CREDENTIALS,
    NO_NETWORK,
    TOO_MANY_ATTEMPTS,
    NO_PROFILE,
    INACTIVE,
    UNKNOWN
}

class LoginException(val error: LoginError) : Exception()