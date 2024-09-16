package org.renaissance.http4k

import org.http4k.core.Body
import org.http4k.format.Moshi.auto
import org.renaissance.common.model.Product

internal object Lens {
    val productLens = Body.auto<Product>().toLens()
    val productsLens = Body.auto<Array<Product>>().toLens()
}