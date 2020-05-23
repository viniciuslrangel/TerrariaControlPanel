package dev.viniciusrangel.terrariacontrolpanel.server

import io.micronaut.websocket.WebSocketBroadcaster
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton

@Singleton
class ServerHandler(
        private val config: ServerConfig,
        private val broadcaster: WebSocketBroadcaster
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val regex = Regex("([^\"]\\S*|\".+?\")\\s*")

    val args by lazy {
        val path = regex.findAll(config.path).map { it.groupValues[1].removeSurrounding("\"") }.toList()
        if (path.isEmpty()) {
            throw IllegalStateException("Invalid config path")
        }
        val args = path.toMutableList()
        args += listOf(
                "-port",
                config.port.toString(),
                "-maxplayers",
                config.maxPlayers.toString(),
                "-password",
                config.password,
                "-world",
                config.world,
                "-autocreate",
                config.autocreate.toString(),
                "-difficulty",
                if (config.expert) "1" else "0"
        )
        if (config.motd.isPresent) {
            args += listOf("-motd", config.motd.get())
        }
        if (config.seed.isPresent) {
            args += listOf("-seed", config.seed.get())
        }
        args
    }

    val isRunning
        get() = process?.isAlive ?: false

    var process: Process? = null

    private val toWrite = PublishSubject.create<String>()

    fun start() {
        if (isRunning) {
            return
        }
        logger.info("Starting server with $args")
        process = ProcessBuilder(args).start().apply {
            val scanner = Scanner(inputStream).useDelimiter("\\n")
            val writerJob = toWrite.subscribe {
                outputStream.write(it.toByteArray())
                outputStream.write('\n'.toInt())
                outputStream.flush()
            }
            GlobalScope.launch(Dispatchers.IO) {
                scanner.forEach { broadcaster.broadcastAsync(it) }
                writerJob.dispose()
                logger.info("Closing server")
            }
        }
    }

    fun forceStop() {
        process?.destroy()
    }

    fun write(msg: String) {
        if (isRunning) {
            toWrite.onNext(msg)
        }
    }


}