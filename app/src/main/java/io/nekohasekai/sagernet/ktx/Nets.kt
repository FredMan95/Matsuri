/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by nekohasekai <contact-sagernet@sekai.icu>             *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

@file:Suppress("SpellCheckingInspection")

package io.nekohasekai.sagernet.ktx

import cn.hutool.core.lang.Validator
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.bg.VpnService
import io.nekohasekai.sagernet.fmt.AbstractBean
import okhttp3.HttpUrl
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

fun linkBuilder() = HttpUrl.Builder().scheme("https")

fun HttpUrl.Builder.toLink(scheme: String, appendDefaultPort: Boolean = true): String {
    var url = build()
    val defaultPort = HttpUrl.defaultPort(url.scheme)
    var replace = false
    if (appendDefaultPort && url.port == defaultPort) {
        url = url.newBuilder().port(14514).build()
        replace = true
    }
    return url.toString().replace("${url.scheme}://", "$scheme://").let {
        if (replace) it.replace(":14514", ":$defaultPort") else it
    }
}

fun String.isIpAddress(): Boolean {
    return Validator.isIpv4(this) || Validator.isIpv6(this)
}

fun String.isIpAddressV6(): Boolean {
    return Validator.isIpv6(this.unwrapIPV6Host())
}

// [2001:4860:4860::8888] -> 2001:4860:4860::8888
fun String.unwrapIPV6Host(): String {
    if (startsWith("[") && endsWith("]")) {
        return substring(1, length - 1).unwrapIPV6Host()
    }
    return this
}

// [2001:4860:4860::8888] or 2001:4860:4860::8888 -> [2001:4860:4860::8888]
fun String.wrapIPV6Host(): String {
    if (!this.isIpAddressV6()) return this
    return "[${this.unwrapIPV6Host()}]"
}

fun AbstractBean.wrapUri(): String {
    return if (Validator.isIpv6(finalAddress)) {
        "[$finalAddress]:$finalPort"
    } else {
        "$finalAddress:$finalPort"
    }
}

fun parseAddress(addressArray: ByteArray) = InetAddress.getByAddress(addressArray)
val INET_TUN = InetAddress.getByName(VpnService.PRIVATE_VLAN4_CLIENT)
val INET6_TUN = InetAddress.getByName(VpnService.PRIVATE_VLAN6_CLIENT)

fun mkPort(): Int {
    val socket = Socket()
    socket.reuseAddress = true
    socket.bind(InetSocketAddress(0))
    val port = socket.localPort
    socket.close()
    return port
}

const val IPPROTO_ICMP = 1
const val IPPROTO_ICMPv6 = 58

const val IPPROTO_TCP = 6
const val IPPROTO_UDP = 17

const val USER_AGENT = "curl/7.74.0"
const val USER_AGENT_ORIGIN = "SagerNet/${BuildConfig.VERSION_NAME}"