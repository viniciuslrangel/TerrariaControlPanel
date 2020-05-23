package dev.viniciusrangel.terrariacontrolpanel.auth

import io.micronaut.context.annotation.Property
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.*
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Singleton

@Singleton
class LoginDatabase(
        @Property(name = "terraria.auth.path")
        private val filePath: String
) : ApplicationEventListener<StartupEvent> {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val userMap = Collections.synchronizedMap(mutableMapOf<String, String>())

    private val watcherCoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun poolLoop(path: Path, watcher: WatchService) = watcherCoroutineScope.launch {
        var poll = true
        while (poll) {
            val key = watcher.take()
            key.pollEvents().forEach { event ->
                val ePath = event.context() as? Path
                if (path == ePath) {
                    updateData(path)
                }
            }
            poll = key.reset()
        }
    }

    override fun onApplicationEvent(event: StartupEvent) {
        val watcher = FileSystems.getDefault().newWatchService()
        val path = Paths.get(filePath)
        if (Files.notExists(path)) {
            Files.createFile(path)
            logger.info("Creating login file")
        }
        if (Files.isDirectory(path)) {
            throw IOException("$path is a directory")
        }
        path.toAbsolutePath().parent.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY)
        poolLoop(path, watcher)
        updateData(path)
    }

    private fun updateData(path: Path) {
        logger.info("Updating login data from disk")
        userMap.clear()
        Files.readAllLines(path).forEach {
            val (user, pass) = it.split(",")
            userMap[user] = pass
        }
    }

    fun validateUser(user: Any, pass: Any) = userMap[user] == pass

}