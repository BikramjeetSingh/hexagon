package co.there4.hexagon.server.engine.servlet

import co.there4.hexagon.helpers.error
import co.there4.hexagon.server.Server
import co.there4.hexagon.server.engine.ServerEngine
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS
import org.eclipse.jetty.util.component.LifeCycle
import java.net.InetSocketAddress
import java.util.*
import javax.servlet.DispatcherType
import org.eclipse.jetty.server.Server as JettyServer
import java.net.InetAddress.getByName as address

/**
 * TODO .
 */
class JettyServletEngine(private val async: Boolean = false) : ServerEngine {
    private var jettyServer: JettyServer? = null

    override fun runtimePort(): Int =
        ((jettyServer?.connectors?.get(0) ?: error) as ServerConnector).localPort.let {
            if (it == -1) error("Jetty port uninitialized. Use lazy evaluation for HTTP client ;)")
            else it
        }

    override fun started() = jettyServer?.isStarted ?: false

    override fun startup(server: Server, settings: Map<String, *>) {
        val serverInstance = JettyServer(InetSocketAddress(server.bindAddress, server.bindPort))
        jettyServer = serverInstance

        val context = ServletContextHandler(SESSIONS)
        context.addLifeCycleListener(object : LifeCycle.Listener {
            override fun lifeCycleStopped(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleStopping(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleStarted(event: LifeCycle?) { /* Do nothing */ }
            override fun lifeCycleFailure(event: LifeCycle?, cause: Throwable?) { /* Do nothing */ }

            override fun lifeCycleStarting(event: LifeCycle?) {
                val filter = ServletFilter (server.router.requestHandlers)
                val dispatcherTypes = EnumSet.allOf(DispatcherType::class.java)
                val filterBind = context.servletContext.addFilter("filters", filter)
                filterBind.setAsyncSupported(async)
                filterBind.addMappingForUrlPatterns(dispatcherTypes, true, "/*")
            }
        })

        serverInstance.handler = context
        serverInstance.start()
    }

    override fun shutdown() {
        jettyServer?.stopAtShutdown = true
        jettyServer?.stop()
    }
}