package com.malinskiy.marathon.ios

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.layout.TTLLLayout
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase

class IOSLogConfigurator: ContextAwareBase(), Configurator  {
    override fun configure(loggerContext: LoggerContext?) {
        loggerContext?.also {
            addInfo("Setting up default configuration.")

            // same as
            // PatternLayout layout = new PatternLayout();
            // layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
            val layout = TTLLLayout()
            layout.context = loggerContext
            layout.start();

            val encoder = LayoutWrappingEncoder<ILoggingEvent>()
            encoder.context = loggerContext
            encoder.layout = layout

            val consoleAppender = ConsoleAppender<ILoggingEvent>()
            consoleAppender.context = loggerContext
            consoleAppender.name = "console"
            consoleAppender.encoder = encoder
            consoleAppender.start()

            loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(consoleAppender)

            val noisyLogger = loggerContext.getLogger("net.schmizz.sshj.common.ECDSAVariationsAdapter")
            noisyLogger.level = Level.INFO
        }
    }
}
