package org.bdch


class Session {

    Long id
    Long version
    String sessionKey
    User owner
    Long cts // Two timestamps I guess
    Long user_id
    Long creation_timestamp

    static belongsTo = [owner: User]

    static mapping = {
        table 'user_session'
        sessionKey column: 'session_key', unique: true
        owner column: 'owner'
        cts column: 'cts'
        user_id column: 'user_id'
        creation_timestamp column: 'creation_timestamp'
    }

    static constraints = {
        sessionKey nullable: false, blank: false, unique: true
        owner nullable: false
    }
}
