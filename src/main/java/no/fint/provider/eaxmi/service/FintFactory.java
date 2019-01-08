package no.fint.provider.eaxmi.service;

import no.fint.model.metamodell.kompleksedatatyper.Dokumentasjon;
import no.fint.model.metamodell.kompleksedatatyper.Identifikator;
import no.fint.model.metamodell.kompleksedatatyper.Multiplisitet;

import java.util.Arrays;
import java.util.List;

public enum FintFactory {
    ;

    public static Identifikator getIdentifikator(String identifikatorVerdi) {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(identifikatorVerdi);
        return identifikator;
    }

    public static List<Dokumentasjon> getDokumentasjon(String tekst) {
        Dokumentasjon dokumentasjon = new Dokumentasjon();
        dokumentasjon.setTekst(tekst);
        return Arrays.asList(dokumentasjon);
    }

    public static Multiplisitet getMultiplisitet(String nedre, String ovre) {
        Multiplisitet multiplisitet = new Multiplisitet();
        multiplisitet.setNedre(nedre);
        multiplisitet.setOvre(ovre);
        return multiplisitet;
    }

    public static Multiplisitet getMultiplisitetFromString(String multiplicityString) {
        Multiplisitet multiplisitet = new Multiplisitet();
        String[] multiplicity = multiplicityString.split("\\.\\.");
        if (multiplicity.length == 2) {
            multiplisitet.setNedre(multiplicity[0]);
            multiplisitet.setOvre(multiplicity[1]);
        }
        if (multiplicity.length == 1) {
            multiplisitet.setNedre(multiplicity[0]);
            multiplisitet.setOvre(multiplicity[0]);
        }
        return multiplisitet;
    }
}
