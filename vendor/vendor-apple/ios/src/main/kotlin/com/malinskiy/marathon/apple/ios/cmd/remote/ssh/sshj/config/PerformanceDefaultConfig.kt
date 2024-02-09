package com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj.config

import ch.qos.logback.classic.Level
import com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj.SshjCommandExecutor
import com.malinskiy.marathon.log.MarathonLogging
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.common.Factory
import net.schmizz.sshj.common.LoggerFactory
import net.schmizz.sshj.common.SecurityUtils
import net.schmizz.sshj.transport.random.BouncyCastleRandom
import net.schmizz.sshj.transport.random.JCERandom
import net.schmizz.sshj.transport.random.Random
import net.schmizz.sshj.transport.random.SingletonRandomFactory
import org.slf4j.Logger

internal class PerformanceDefaultConfig(
    private val verbose: Boolean = false,
) : DefaultConfig() {
    companion object {
        val bcFactory = MemoizingFactory(BouncyCastleRandom.Factory())
        val jceFactory = MemoizingFactory(JCERandom.Factory())
    }

    init {
        val loggerFactory = object : LoggerFactory {
            override fun getLogger(clazz: Class<*>?): Logger {
                val name = clazz?.simpleName ?: SshjCommandExecutor::class.java.simpleName
                return MarathonLogging.logger(
                    name = name,
                    level = if (verbose) {
                        Level.DEBUG
                    } else {
                        Level.ERROR
                    }
                )
            }

            override fun getLogger(name: String?): Logger = MarathonLogging.logger(
                name = name ?: "",
                level = if (verbose) {
                    Level.DEBUG
                } else {
                    Level.ERROR
                }
            )
        }
        setLoggerFactory(loggerFactory)
        setRandomFactory {
            SingletonRandomFactory(if (SecurityUtils.isBouncyCastleRegistered()) bcFactory else jceFactory)
        }
    }

    class MemoizingFactory(private val factory: Factory<Random>) : Factory<Random> {
        val random: Random by lazy { factory.create() }
        override fun create(): Random {
            return random
        }
    }
}
