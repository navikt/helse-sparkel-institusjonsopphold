package no.nav.helse.sparkel.pleiepenger

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import no.nav.helse.sparkel.pleiepenger.institusjonsopphold.Institusjonsoppholdperiode
import org.slf4j.LoggerFactory

internal class Institusjonsoppholdløser(
        rapidsConnection: RapidsConnection,
        private val institusjonsoppholdService: InstitusjonsoppholdService
) : River.PacketListener {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "Institusjonsopphold"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        institusjonsoppholdService.løsningForBehov(
                packet["@id"].asText(),
                packet["vedtaksperiodeId"].asText(),
                packet["fødselsnummer"].asText()
        ).let { løsning ->
            packet["@løsning"] = mapOf(
                    behov to (løsning?.get("vedtak")?.map { Institusjonsoppholdperiode(it) } ?: emptyList())
            )
            context.send(packet.toJson().also { json ->
                sikkerlogg.info(
                        "sender svar {} for {}:\n\t{}",
                        keyValue("id", packet["@id"].asText()),
                        keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asText()),
                        json
                )
            })
        }
    }
}
