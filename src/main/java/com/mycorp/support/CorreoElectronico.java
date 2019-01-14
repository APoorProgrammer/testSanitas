package com.mycorp.support;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CorreoElectronico extends DatosEnvioMail {
    /** Contruye un objeto {@link CorreoElectronico} a partir de los parÃ¡metros recibidos. */
    public CorreoElectronico( long idFuncionalidad, String idioma, List< String > params ) {
        this.funcionalidad = ( int )idFuncionalidad;
        this.idioma = idioma;
        this.params = params;
    }

    /** Contruye un objeto {@link CorreoElectronico} a partir de los parÃ¡metros recibidos. */
    public CorreoElectronico( long idFuncionalidad, String idioma ) {
        this.funcionalidad = ( int )idFuncionalidad;
        this.idioma = idioma;
    }

    /**
     * AÃ±ade un adjunto al {@link CorreoElectronico}.
     *
     * @param adjunto el adjunto
     */
    public CorreoElectronico addAdjunto( FicheroAdjunto adjunto ) {
        if ( adjunto != null && adjunto.getContenido() != null && StringUtils.isNotEmpty( adjunto.getNombre() ) ) {
            getAdjuntos().add( adjunto );
        }
        return this;
    }

    /**
     * AÃ±ade un parÃ¡metro al {@link CorreoElectronico}.
     *
     * @param param el parÃ¡metro
     */
    public CorreoElectronico addParam( String param ) {
        if ( StringUtils.isNotBlank( param ) ) {
            getParams().add( param );
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new StringBuilder().append( funcionalidad )
           .append( ":{ [emailA: " ).append( StringUtils.defaultString( emailA ) ).append( "]" )
           .append( " [emailDe: " ).append( StringUtils.defaultString( emailDe ) ).append( "]" )
           .append( " [emailCC: " ).append( StringUtils.defaultString( emailCC ) ).append( "]" )
           .append( " [emailCCO: " ).append( StringUtils.defaultString( emailCCO ) ).append( "]" )
           .append( " [asunto: " ).append( StringUtils.defaultString( asunto ) ).append( "]" )
           .append( " [dias: " ).append( StringUtils.defaultString( dias ) ).append( "]" )
           .append( " [estado: " ).append( estado ).append( "]" )
           .append( " [idioma: " ).append( StringUtils.defaultString( idioma ) ).append( "]" )
           .append( " [idMensaje: " ).append( idMensaje ).append( "]}" ).toString();
    }

}
