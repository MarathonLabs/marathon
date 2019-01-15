package com.malinskiy.marathon.ios.cmd.remote

import net.schmizz.concurrent.Promise
import net.schmizz.keepalive.KeepAlive
import net.schmizz.sshj.common.DisconnectReason
import net.schmizz.sshj.common.SSHPacket
import net.schmizz.sshj.connection.ConnectionException
import net.schmizz.sshj.connection.ConnectionImpl
import net.schmizz.sshj.transport.TransportException
import java.util.*

class SshjCommandKeepAlive(conn: ConnectionImpl, hostname: String): KeepAlive(conn, "$hostname keep-alive") {

    /** The max number of keep-alives that should be unanswered before killing the connection.  */
    @get:Synchronized
    @set:Synchronized
    var maxAliveCount = 5

    /** The queue of promises.  */
    private val queue = LinkedList<Promise<SSHPacket, ConnectionException>>()

    @Throws(TransportException::class, ConnectionException::class)
    override fun doKeepAlive() {
        // Ensure the service is set... This means that the key exchange is done and the connection is up.
        if (conn == conn.transport.service) {
            emptyQueue(queue)
            checkMaxReached(queue)
            queue.add(conn.sendGlobalRequest("keepalive@openssh.com", true, ByteArray(0)))
        }
    }

    @Throws(ConnectionException::class)
    private fun checkMaxReached(queue: Queue<Promise<SSHPacket, ConnectionException>>) {
        if (queue.size >= maxAliveCount) {
            throw ConnectionException(
                DisconnectReason.CONNECTION_LOST,
                java.lang.String.format("Did not receive any keep-alive response for %s seconds", maxAliveCount * keepAliveInterval)
            )
        }
    }

    private fun emptyQueue(queue: Queue<Promise<SSHPacket, ConnectionException>>) {
        var peek: Promise<SSHPacket, ConnectionException>? = queue.peek()
        while (peek != null && peek.isFulfilled) {
            log.debug("Received response from server to our keep-alive.")
            queue.remove()
            peek = queue.peek()
        }
    }
}
