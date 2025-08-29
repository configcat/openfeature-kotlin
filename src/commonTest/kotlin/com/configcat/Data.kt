package com.configcat

import com.configcat.model.Config
import com.configcat.model.Setting
import com.configcat.override.OverrideDataSource
import kotlinx.serialization.json.Json

object TestData {
    val complexJson = """
{
    "p": {
        "u": "https://cdn-global.configcat.com",
        "s": "s449fLWNwiEFQ/AqfRj13pPHVdV9g3h0HAFzWtjpZgE="
    },
    "f": {
        "disabledFeature": {
            "v": {
                "b": false
            },
            "i": "v-disabled-f",
            "t": 0,
            "r": [
                {
                    "c": [
                        {
                            "u": {
                                "a": "Identifier",
                                "c": 2,
                                "l": ["@matching.com"]
                            }
                        }
                    ],
                    "s": {
                        "v": {
                            "b": true
                        },
                        "i": "v-disabled-t"
                    }
                }
            ]
        },
        "enabledFeature": {
            "t": 0,
            "i": "v-enabled",
            "v": {
                "b": true
            }
        },
        "intSetting": {
            "t": 2,
            "i": "v-int",
            "v": {
                "i": 5
            }
        },
        "doubleSetting": {
            "t": 3,
            "i": "v-double",
            "v": {
                "d": 1.2
            }
        },
        "stringSetting": {
            "t": 1,
            "i": "v-string",
            "v": {
                "s": "test"
            }
        },
        "objectSetting": {
            "t": 1,
            "i": "v-object",
            "v": {
                "s": "{ \"bool_field\": true, \"text_field\": \"value\" }"
            }
        }
    }
}
    """
}

class TestOverrideDataSource : OverrideDataSource {
    private val json: Json = Json { ignoreUnknownKeys = true }
    private val settings: Map<String, Setting> =
        json.decodeFromString<Config>(TestData.complexJson).settings!!

    override fun getOverrides(): Map<String, Setting> = settings
}
