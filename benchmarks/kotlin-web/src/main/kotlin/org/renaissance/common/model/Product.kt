package org.renaissance.common.model

import kotlinx.serialization.Serializable

// sbt build system cannot handle the kotlinx.serialization plugin well
@Suppress("plugin_is_not_enabled")
@Serializable
internal data class Product(val id: String, val name: String)