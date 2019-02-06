package com.malinskiy.marathon.ios

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.report.debug.timeline.TimelineSummarySerializer
import com.malinskiy.marathon.vendor.VendorConfiguration
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.common.KeyType
import net.schmizz.sshj.transport.kex.Curve25519SHA256
import net.schmizz.sshj.transport.random.BouncyCastleRandom
import org.slf4j.LoggerFactory

class IOSLogConfigurator: MarathonLogConfigurator  {
    override fun configure(vendorConfiguration: VendorConfiguration) {
        val iosConfiguration = vendorConfiguration as? IOSConfiguration

        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        val compactOutput = iosConfiguration?.compactOutput ?: false
        val layout = PatternLayout()
        layout.pattern = if (compactOutput) {
            "%highlight(%.-1level [%thread] <%logger{48}> %msg%n)"
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
        consoleAppender.name = "ios-custom-console-appender"
        consoleAppender.encoder = encoder
        consoleAppender.start()

        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)

        // replace the default appenders
        rootLogger.detachAndStopAllAppenders()
        rootLogger.addAppender(consoleAppender)

        // silence the noise
        listOf(
            TimelineSummarySerializer::class.java.simpleName,
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
