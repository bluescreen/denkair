package de.denkair.fluginfo;

import java.io.Serializable;
import java.util.Date;

/**
 * Flug-Informations-Bean.
 *
 * ACHTUNG: Dieses Package stammt aus der urspruenglichen DenkAir-Alt-Applikation von 2013/2014.
 * Der SAP-Connector referenziert die Klassennamen ueber RFC-Bindung
 * (vgl. SapConnector.callRFC) — RFC-Mapping nach Paketumbenennung pruefen.
 *
 * NICHT LOESCHEN ohne Absprache mit SAP-Integration (akin, mueller).
 *
 * @author jens
 * @since 2014-03-14
 * @deprecated use {@link de.denkair.booking.domain.Flight} for all new code
 */
@Deprecated
public class FlugInfoBean implements Serializable {

    private static final long serialVersionUID = 20140314L;

    private Long fluginfoId;
    private String flugnummer;
    private String abflugOrtIata;
    private String zielOrtIata;
    private Date abflug;
    private Date ankunft;
    private Double preisBrutto;
    private Integer freiePlaetze;
    private String flugzeugTyp;

    // Legacy-Flag aus DenkAir-Altsystem: 0 = Charter, 1 = Linie, 2 = Ad-hoc
    private Integer flugArt = 1;

    public FlugInfoBean() {
    }

    public Long getFluginfoId() { return fluginfoId; }
    public void setFluginfoId(Long fluginfoId) { this.fluginfoId = fluginfoId; }

    public String getFlugnummer() { return flugnummer; }
    public void setFlugnummer(String flugnummer) { this.flugnummer = flugnummer; }

    public String getAbflugOrtIata() { return abflugOrtIata; }
    public void setAbflugOrtIata(String abflugOrtIata) { this.abflugOrtIata = abflugOrtIata; }

    public String getZielOrtIata() { return zielOrtIata; }
    public void setZielOrtIata(String zielOrtIata) { this.zielOrtIata = zielOrtIata; }

    public Date getAbflug() { return abflug; }
    public void setAbflug(Date abflug) { this.abflug = abflug; }

    public Date getAnkunft() { return ankunft; }
    public void setAnkunft(Date ankunft) { this.ankunft = ankunft; }

    public Double getPreisBrutto() { return preisBrutto; }
    public void setPreisBrutto(Double preisBrutto) { this.preisBrutto = preisBrutto; }

    public Integer getFreiePlaetze() { return freiePlaetze; }
    public void setFreiePlaetze(Integer freiePlaetze) { this.freiePlaetze = freiePlaetze; }

    public String getFlugzeugTyp() { return flugzeugTyp; }
    public void setFlugzeugTyp(String flugzeugTyp) { this.flugzeugTyp = flugzeugTyp; }

    public Integer getFlugArt() { return flugArt; }
    public void setFlugArt(Integer flugArt) { this.flugArt = flugArt; }

    @Override
    public String toString() {
        return "FlugInfoBean[" + flugnummer + " " + abflugOrtIata + "->" + zielOrtIata + " " + abflug + "]";
    }
}
