package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.File
import java.io.FileNotFoundException
import java.time.Duration

/**
 * @param knownHostsPath known_hosts file location in OpenSSH format
 */
data class SshConfiguration(
    @JsonProperty("authentication") val authentication: SshAuthentication? = null,
    @JsonProperty("knownHostsPath") val knownHostsPath: File? = null,
    @JsonProperty("keepAliveInterval") val keepAliveInterval: Duration = Duration.ofSeconds(60),
    @JsonProperty("debug") val debug: Boolean = false,
) {
    fun validate() {
        authentication?.let { 
            when(it) {
                is SshAuthentication.PasswordAuthentication -> Unit
                is SshAuthentication.PublicKeyAuthentication -> {
                    if (!it.key.exists()) {
                        throw FileNotFoundException("Private key not found at ${it.key.absolutePath}")
                    }
                }
            }
        }
        if (knownHostsPath?.exists() == false) {
            throw FileNotFoundException("knownhosts file not found at $knownHostsPath")
        }
    }
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SshAuthentication.PasswordAuthentication::class, name = "password"),
    JsonSubTypes.Type(value = SshAuthentication.PublicKeyAuthentication::class, name = "publicKey"),
)
sealed class SshAuthentication {
    data class PasswordAuthentication(@JsonProperty("username") val username: String, @JsonProperty("password") val password: String) : SshAuthentication() {
        override fun toString(): String {
            return "PasswordAuthentication(username='$username')"
        }
    }
    data class PublicKeyAuthentication(@JsonProperty("username") val username: String, @JsonProperty("key") val key: File) : SshAuthentication()
}
