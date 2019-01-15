package com.mycorp.constants;

import org.springframework.beans.factory.annotation.Value;

public final class ZendeskConstants {

	public final static String ESCAPED_LINE_SEPARATOR = "\\n";

	public final static  String ESCAPE_ER = "\\";

	public final static  String HTML_BR = "<br/>";

	@Value("#{envPC['zendesk.ticket']}")
    public final static  String PETICION_ZENDESK= "";

	@Value("#{envPC['zendesk.token']}")
    public final static  String TOKEN_ZENDESK= "";

    @Value("#{envPC['zendesk.url']}")
    public final static  String URL_ZENDESK= "";

    @Value("#{envPC['zendesk.user']}")
    public final static  String ZENDESK_USER= "";

    @Value("#{envPC['tarjetas.getDatos']}")
    public final static  String TARJETAS_GETDATOS = "";

    @Value("#{envPC['cliente.getDatos']}")
    public final static  String CLIENTE_GETDATOS = "";

    @Value("#{envPC['zendesk.error.mail.funcionalidad']}")
    public final static  String ZENDESK_ERROR_MAIL_FUNCIONALIDAD = "";

    @Value("#{envPC['zendesk.error.destinatario']}")
    public final static  String ZENDESK_ERROR_DESTINATARIO = "";
	
}
