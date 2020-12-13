package com.malinskiy.marathon.android.configuration

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.slf4j.LoggerFactory

class AndroidLogConfigurator : MarathonLogConfigurator {
    override fun configure(vendorConfiguration: VendorConfiguration) {
        val loggerContext = LoggerFactory.getILoggerFactory() as? LoggerContext ?: return

        val layout = PatternLayout()
        layout.pattern = "%highlight(%.-1level %d{HH:mm:ss.SSS} [%thread] <%logger{40}> %msg%n)"
        layout.context = loggerContext
        layout.start();

        val encoder = LayoutWrappingEncoder<ILoggingEvent>()
        encoder.context = loggerContext
        encoder.layout = layout

        val consoleAppender = ConsoleAppender<ILoggingEvent>()
        consoleAppender.isWithJansi = true
        consoleAppender.context = loggerContext
        consoleAppender.name = "android-custom-console-appender"
        consoleAppender.encoder = encoder
        consoleAppender.start()

        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)

        // replace the default appenders
        rootLogger.detachAndStopAllAppenders()
        rootLogger.addAppender(consoleAppender)
    }
}
