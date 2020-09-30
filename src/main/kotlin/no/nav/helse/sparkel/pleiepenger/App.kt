package no.nav.helse.sparkel.pleiepenger

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.pleiepenger.institusjonsopphold.InstitusjonsoppholdClient
import no.nav.helse.sparkel.pleiepenger.institusjonsopphold.ServiceUser
import no.nav.helse.sparkel.pleiepenger.institusjonsopphold.StsRestClient
import java.io.File

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val stsClient = StsRestClient(
        baseUrl = "${env.getValue("STS_BASE_URL")}",
        serviceUser = "/var/run/secrets/nais.io/service_user".let { ServiceUser("$it/username".readFile(), "$it/password".readFile()) }
    )
    val institusjonsoppholdClient = InstitusjonsoppholdClient(
        baseUrl = env.getValue("INSTITUSJONSOPPHOLD_URL"),
        stsClient = stsClient
    )
    val institusjonsoppholdService = InstitusjonsoppholdService(institusjonsoppholdClient)

    return RapidApplication.create(env).apply {
//        Pleiepengerløser(this, pleiepengerService)
    }
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)
