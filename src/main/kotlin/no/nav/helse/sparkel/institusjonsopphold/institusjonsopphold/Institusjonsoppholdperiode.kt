package no.nav.helse.sparkel.institusjonsopphold.institusjonsopphold

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate

class Institusjonsoppholdperiode(jsonNode: JsonNode) {
    val institusjonstype: String? = jsonNode["institusjonstype"]?.textValue()
    val kategori: Int = jsonNode["kategori"].intValue()
    val startdato: LocalDate = jsonNode["startdato"].textValue().let { LocalDate.parse(it) }
    val faktiskSluttdato: LocalDate? = jsonNode["faktiskSluttdato"].takeUnless { it.isNull }?.textValue()?.let { LocalDate.parse(it) }
}
