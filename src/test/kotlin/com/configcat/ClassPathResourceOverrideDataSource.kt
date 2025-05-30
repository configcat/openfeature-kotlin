package com.configcat

import com.configcat.model.Config
import com.configcat.model.Setting
import com.configcat.override.OverrideDataSource
import kotlinx.serialization.json.Json

class ClassPathResourceOverrideDataSource(name: String) : OverrideDataSource {
    private val json: Json = Json { ignoreUnknownKeys = true }
    private val settings: Map<String, Setting>

    init {
        val content = this::class.java.classLoader!!.getResource(name)!!.readText()
        settings = json.decodeFromString<Config>(content).settings!!
    }

    override fun getOverrides(): Map<String, Setting> = settings
}
