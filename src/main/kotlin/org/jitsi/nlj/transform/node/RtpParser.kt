/*
 * Copyright @ 2019 - present 8x8 Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.nlj.transform.node

import org.jitsi.nlj.PacketInfo
import org.jitsi.nlj.rtp.AudioRtpPacket
import org.jitsi.nlj.rtp.VideoRtpPacket
import org.jitsi.nlj.util.StreamInformationStore
import org.jitsi.nlj.util.cdebug
import org.jitsi.rtp.rtp.RtpHeader
import org.jitsi.utils.MediaType

class RtpParser(
    private val streamInformationStore: StreamInformationStore
) : TransformerNode("RTP Parser") {

    override fun transform(packetInfo: PacketInfo): PacketInfo? {
        val packet = packetInfo.packet
        val payloadTypeNumber = RtpHeader.getPayloadType(packet.buffer, packet.offset).toByte()

        val payloadType = streamInformationStore.rtpPayloadTypes[payloadTypeNumber] ?: run {
            logger.cdebug { "Unknown payload type: $payloadTypeNumber" }
            return null
        }

        packetInfo.packet = when (payloadType.mediaType) {
            MediaType.AUDIO -> packet.toOtherType(::AudioRtpPacket)
            MediaType.VIDEO -> packet.toOtherType(::VideoRtpPacket)
            else -> throw Exception("Unrecognized media type: '${payloadType.mediaType}'")
        }

        packetInfo.resetPayloadVerification()
        return packetInfo
    }
}
