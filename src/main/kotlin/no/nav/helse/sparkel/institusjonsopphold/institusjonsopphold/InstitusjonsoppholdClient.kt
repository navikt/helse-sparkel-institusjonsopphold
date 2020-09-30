package no.nav.helse.sparkel.institusjonsopphold.institusjonsopphold

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

internal class InstitusjonsoppholdClient(
    private val baseUrl: String,
    private val stsClient: StsRestClient
) {

    companion object {
        private val objectMapper = ObjectMapper()
    }

    internal fun hentInstitusjonsopphold(
        fødselsnummer: String
    ): JsonNode {
        val url = "${baseUrl}/api/v1/person/institusjonsopphold"

        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            //TODO: STS
            setRequestProperty("Authorization", "Bearer ${stsClient.token()}")
            setRequestProperty("Accept", "application/json")
            //TODO: En ID som identifiserer kallkjeden som dette kallet er en del av.
            setRequestProperty("Nav-Call-Id", "")
            //TODO: ID’en på systemet som gjør kallet, som regel service brukeren til applikasjonen, for eksempel "srvsigrun".
            setRequestProperty("Nav-Consumer-Id", "")
            setRequestProperty("Nav-Personident", fødselsnummer)

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        if (responseCode >= 300 || responseBody == null) {
            throw RuntimeException("unknown error (responseCode=$responseCode) from Inst2")
        }

        return objectMapper.readTree(responseBody)
    }
}
