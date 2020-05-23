package dev.viniciusrangel.terrariacontrolpanel

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("dev.viniciusrangel.terrariacontrolpanel")
                .mainClass(Application.javaClass)
                .start()
    }
}
