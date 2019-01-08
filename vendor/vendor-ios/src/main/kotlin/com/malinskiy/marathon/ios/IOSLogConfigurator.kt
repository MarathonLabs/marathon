package com.malinskiy.marathon.ios

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.common.KeyType
import net.schmizz.sshj.transport.kex.Curve25519SHA256
import net.schmizz.sshj.transport.random.BouncyCastleRandom
import org.slf4j.LoggerFactory

class IOSLogConfigurator: ContextAwareBase(), Configurator, MarathonLogConfigurator  {
    private var iosConfiguration: IOSConfiguration? = null

    override fun configure(vendorConfiguration: VendorConfiguration) {
        iosConfiguration = vendorConfiguration as IOSConfiguration

        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        configure(context)
    }

    override fun configure(loggerContext: LoggerContext?) {
        loggerContext?.let {
            addInfo("Setting up default configuration.")

            val compactOutput = iosConfiguration?.compactOutput ?: false
            val layout = PatternLayout()
            layout.pattern = if (compactOutput) {
                "%highlight(%.-1level [%thread] <%logger{40}> %msg%n)"
            } else {
                "%highlight(%.-1level %d{HH:mm:ss.SSS} [%thread] <%logger{40}> %msg%n)"
            }
            layout.context = loggerContext
            layout.start();

            val encoder = LayoutWrappingEncoder<ILoggingEvent>()
            encoder.context = loggerContext
            encoder.layout = layout

            val consoleAppender = ConsoleAppender<ILoggingEvent>()
            consoleAppender.isWithJansi = true
            consoleAppender.context = loggerContext
            consoleAppender.name = "console"
            consoleAppender.encoder = encoder
            consoleAppender.start()

            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(consoleAppender)

            listOf(
                Curve25519SHA256::class.java.name,
                BouncyCastleRandom::class.java.name,
                DefaultConfig::class.java.name,
                KeyType::class.java.name,
                "net.schmizz.sshj.common.ECDSAVariationsAdapter"
            ).forEach {
                loggerContext.getLogger(it).level = Level.ERROR
            }
        }
    }
}
