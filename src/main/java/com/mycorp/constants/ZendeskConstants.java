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

    public final static  String POTENCIAL = "POTENCIAL";

    public final static  String REAL = "REAL";

    public final static  String PROSPECTO = "PROSPECTO";

    //Customer Data
    public final static  String CUSTOMER_POLICY_NUMBER = "NÂº de poliza/colectivo: ";
    public final static  String SANITAS_CARD_ID =" NÂº tarjeta Sanitas o Identificador: ";
    public final static  String DOCUMENT_TYPE_CD = "Tipo documento: ";
    public final static  String DOCUMENT_NUMBER_CD = "NÂº documento: ";
    public final static  String EMAIL = "Email personal";
    public final static  String CELLPHONE_NUMBER = "NÂº mÃ³vil: ";
    public final static  String USER_AGENT = "User Agent";

    //Customer Bravo Data
    public final static  String GETTED_CUSTOMER_BRAVO_DATA = "Datos recuperados de BRAVO:";
    public final static  String PHONE = "TelÃ©fono: ";
    public final static  String BIRTH_DATE = "Feha de nacimiento: ";
    public final static  String DOCUMENT_NUMBER_BD = "NÃºmero documento: ";
    public final static  String DOCUMENT_TYPE_BD = "Tipo de documento: ";
    public final static  String CUSTOMER_TYPE = "Tipo cliente: ";
    public final static  String CUSTOMER_STATE_ID = "ID estado del cliente: ";
    public final static  String NEW_CUSTOMER_CAUSE_ID = "ID motivo de alta cliente: ";
    public final static  String REGISTERED = "Registrado: ";
    public final static  String YES = "SÃ­";
    public final static  String NO = "No: ";
    public final static  String CARD_SERVICE_RECOVERED_DATA = "Datos recuperados del servicio de tarjeta:";
   
}
