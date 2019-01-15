package com.mycorp;

import static com.mycorp.constants.ZendeskConstants.ESCAPED_LINE_SEPARATOR;
import static com.mycorp.constants.ZendeskConstants.ESCAPE_ER;
import static com.mycorp.constants.ZendeskConstants.HTML_BR;
import static com.mycorp.constants.ZendeskConstants.PETICION_ZENDESK;
import static com.mycorp.constants.ZendeskConstants.TARJETAS_GETDATOS;
import static com.mycorp.constants.ZendeskConstants.TOKEN_ZENDESK;
import static com.mycorp.constants.ZendeskConstants.URL_ZENDESK;
import static com.mycorp.constants.ZendeskConstants.ZENDESK_ERROR_DESTINATARIO;
import static com.mycorp.constants.ZendeskConstants.ZENDESK_ERROR_MAIL_FUNCIONALIDAD;
import static com.mycorp.constants.ZendeskConstants.ZENDESK_USER;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mycorp.support.CorreoElectronico;
import com.mycorp.support.DatosCliente;
import com.mycorp.support.MensajeriaService;
import com.mycorp.support.Poliza;
import com.mycorp.support.PolizaBasicoFromPolizaBuilder;
import com.mycorp.support.Ticket;
import com.mycorp.support.ValueCode;

import portalclientesweb.ejb.interfaces.PortalClientesWebEJBRemote;
import util.datos.PolizaBasico;
import util.datos.UsuarioAlta;

@Service
public class ZendeskService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger( ZendeskService.class );

	//MODIFY - change to final
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    private ZendeskCustomerData zendeskCustomerData = new ZendeskCustomerData();

    /** The portalclientes web ejb remote. */
    @Autowired
    // @Qualifier("portalclientesWebEJB")
    private PortalClientesWebEJBRemote portalclientesWebEJBRemote;

    /** The rest template. */
    @Autowired
    @Qualifier("restTemplateUTF8")
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier( "emailService" )
    MensajeriaService emailService;

    /**
     * Crea un ticket en Zendesk. Si se ha informado el nÂº de tarjeta, obtiene los datos asociados a dicha tarjeta de un servicio externo.
     * @param usuarioAlta
     * @param userAgent
     */
    public String altaTicketZendesk(UsuarioAlta usuarioAlta, String userAgent){

        //CREATE - create a specific Zendesk's mapper class and inject dependency
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    
        String idCliente = null;

        StringBuilder datosUsuario = new StringBuilder();
        StringBuilder datosBravo = new StringBuilder();
        StringBuilder datosServicio = new StringBuilder();
        StringBuilder clientName = new StringBuilder();
        
        // AÃ±ade los datos del formulario
        addFormUserData(usuarioAlta, userAgent, zendeskCustomerData);
      
        // Obtiene el idCliente de la tarjeta
        idCliente = getIdCustomer(usuarioAlta, mapper, datosServicio, clientName, idCliente);

        renameThisMethod(datosBravo, idCliente);

        createZendeskTicket(usuarioAlta, mapper, datosUsuario, datosBravo, datosServicio, clientName);

		datosUsuario.append(datosBravo);

		return datosUsuario.toString();
    }

	private void createZendeskTicket(UsuarioAlta usuarioAlta, ObjectMapper mapper, StringBuilder datosUsuario,
			StringBuilder datosBravo, StringBuilder datosServicio, StringBuilder clientName) {
		String ticket = String.format(PETICION_ZENDESK, clientName.toString(), usuarioAlta.getEmail(), datosUsuario.toString()+datosBravo.toString()+
                parseJsonBravo(datosServicio));
        ticket = ticket.replaceAll("["+ESCAPED_LINE_SEPARATOR+"]", " ");

        try(Zendesk zendesk = new Zendesk.Builder(URL_ZENDESK).setUsername(ZENDESK_USER).setToken(TOKEN_ZENDESK).build()){
            //Ticket
            Ticket petiZendesk = mapper.readValue(ticket, Ticket.class);
            zendesk.createTicket(petiZendesk);

        }catch(Exception e){
            LOG.error("Error al crear ticket ZENDESK", e);
            // Send email

            CorreoElectronico correo = new CorreoElectronico( Long.parseLong(ZENDESK_ERROR_MAIL_FUNCIONALIDAD), "es" )
                    .addParam(datosUsuario.toString().replaceAll(ESCAPE_ER+ESCAPED_LINE_SEPARATOR, HTML_BR))
                    .addParam(datosBravo.toString().replaceAll(ESCAPE_ER+ESCAPED_LINE_SEPARATOR, HTML_BR));
            correo.setEmailA( ZENDESK_ERROR_DESTINATARIO );
            try
            {
                emailService.enviar( correo );
			} catch (Exception ex) {
				LOG.error("Error al enviar mail", ex);
			}

		}
	}

	private void renameThisMethod(StringBuilder datosBravo, String idCliente) {
		try
		    {
		        // Obtenemos los datos del cliente
		        DatosCliente cliente = restTemplate.getForObject("http://localhost:8080/test-endpoint", DatosCliente.class, idCliente);

		        datosBravo.append("TelÃ©fono: ").append(cliente.getGenTGrupoTmk()).append(ESCAPED_LINE_SEPARATOR);


		        datosBravo.append("Feha de nacimiento: ").append(formatter.format(formatter.parse(cliente.getFechaNacimiento()))).append(ESCAPED_LINE_SEPARATOR);

		        List< ValueCode > tiposDocumentos = getTiposDocumentosRegistro();
		        for(int i = 0; i < tiposDocumentos.size();i++)
		        {
		            if(tiposDocumentos.get(i).getCode().equals(cliente.getGenCTipoDocumento().toString()))
		            {
		                datosBravo.append("Tipo de documento: ").append(tiposDocumentos.get(i).getValue()).append(ESCAPED_LINE_SEPARATOR);
		            }
		        }
		        datosBravo.append("NÃºmero documento: ").append(cliente.getNumeroDocAcred()).append(ESCAPED_LINE_SEPARATOR);

		        datosBravo.append("Tipo cliente: ");
		        switch (cliente.getGenTTipoCliente()) {
		        case 1:
		            datosBravo.append("POTENCIAL").append(ESCAPED_LINE_SEPARATOR);
		            break;
		        case 2:
		            datosBravo.append("REAL").append(ESCAPED_LINE_SEPARATOR);
		            break;
		        case 3:
		            datosBravo.append("PROSPECTO").append(ESCAPED_LINE_SEPARATOR);
		            break;
		        }

		        datosBravo.append("ID estado del cliente: ").append(cliente.getGenTStatus()).append(ESCAPED_LINE_SEPARATOR);

		        datosBravo.append("ID motivo de alta cliente: ").append(cliente.getIdMotivoAlta()).append(ESCAPED_LINE_SEPARATOR);

		        datosBravo.append("Registrado: ").append((cliente.getfInactivoWeb() == null ? "SÃ­" : "No")).append(ESCAPED_LINE_SEPARATOR + ESCAPED_LINE_SEPARATOR);


		    }catch(Exception e)
		    {
		        LOG.error("Error al obtener los datos en BRAVO del cliente", e);
		    }
	}

	private String getIdCustomer(UsuarioAlta usuarioAlta, ObjectMapper mapper, StringBuilder datosServicio,
			StringBuilder clientName, String idCliente) {
		if(StringUtils.isNotBlank(usuarioAlta.getNumTarjeta())){
            try{
                String urlToRead = TARJETAS_GETDATOS + usuarioAlta.getNumTarjeta();
                ResponseEntity<String> res = restTemplate.getForEntity( urlToRead, String.class);
                if(res.getStatusCode() == HttpStatus.OK){
                    String dusuario = res.getBody();
                    clientName.append(dusuario);
                    idCliente = dusuario;
                    datosServicio.append("Datos recuperados del servicio de tarjeta:").append(ESCAPED_LINE_SEPARATOR).append(mapper.writeValueAsString(dusuario));
                }
            }catch(Exception e)
            {
                LOG.error("Error al obtener los datos de la tarjeta", e);
            }
        }
        else if(StringUtils.isNotBlank(usuarioAlta.getNumPoliza())){
            try
            {
                Poliza poliza = new Poliza();
                poliza.setNumPoliza(Integer.valueOf(usuarioAlta.getNumPoliza()));
                poliza.setNumColectivo(Integer.valueOf(usuarioAlta.getNumDocAcreditativo()));
                poliza.setCompania(1);

                PolizaBasico polizaBasicoConsulta = new PolizaBasicoFromPolizaBuilder().withPoliza( poliza ).build();

                final util.datos.DetallePoliza detallePolizaResponse = portalclientesWebEJBRemote.recuperarDatosPoliza(polizaBasicoConsulta);

                clientName.append(detallePolizaResponse.getTomador().getNombre()).
                            append(" ").
                            append(detallePolizaResponse.getTomador().getApellido1()).
                            append(" ").
                            append(detallePolizaResponse.getTomador().getApellido2());

                idCliente = detallePolizaResponse.getTomador().getIdentificador();
                datosServicio.append("Datos recuperados del servicio de tarjeta:").append(ESCAPED_LINE_SEPARATOR).append(mapper.writeValueAsString(detallePolizaResponse));
            }catch(Exception e)
            {
                LOG.error("Error al obtener los datos de la poliza", e);
            }
        }
		return idCliente;
	}

	private void addFormUserData(UsuarioAlta usuarioAlta, String userAgent, ZendeskCustomerData zendeskCustomerData) {

		StringBuilder currentUserData = zendeskCustomerData.getDatosUsuario();
		StringBuilder currentUserBravoData = zendeskCustomerData.getDatosBravo();
		
		if(StringUtils.isNotBlank(usuarioAlta.getNumPoliza())){
			currentUserData.append("NÂº de poliza/colectivo: ").append(usuarioAlta.getNumPoliza()).append("/").append(usuarioAlta.getNumDocAcreditativo());
        }else{
        	currentUserData.append("NÂº tarjeta Sanitas o Identificador: ").append(usuarioAlta.getNumTarjeta());
        }
		currentUserData.append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append("Tipo documento: ").append(usuarioAlta.getTipoDocAcreditativo()).append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append("NÂº documento: ").append(usuarioAlta.getNumDocAcreditativo()).append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append("Email personal: ").append(usuarioAlta.getEmail()).append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append("NÂº mÃ³vil: ").append(usuarioAlta.getNumeroTelefono()).append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append("User Agent: ").append(userAgent).append(ESCAPED_LINE_SEPARATOR);

        currentUserBravoData.append(ESCAPED_LINE_SEPARATOR + "Datos recuperados de BRAVO:" + ESCAPED_LINE_SEPARATOR + ESCAPED_LINE_SEPARATOR);
        
        zendeskCustomerData.setDatosUsuario(currentUserData);
        zendeskCustomerData.setDatosBravo(currentUserBravoData);
	}

    public List< ValueCode > getTiposDocumentosRegistro() {
        return Arrays.asList( new ValueCode(), new ValueCode() ); // simulacion servicio externo
    }

    
    //EXTRACT - it must be a specific method of parsing JSON class
    /**
     * MÃ©todo para parsear el JSON de respuesta de los servicios de tarjeta/pÃ³liza
     *
     * @param resBravo
     * @return
     */
    private String parseJsonBravo(StringBuilder resBravo)
    {
        return resBravo.toString().replaceAll("[\\[\\]\\{\\}\\\"\\r]", "").replaceAll(ESCAPED_LINE_SEPARATOR, ESCAPE_ER + ESCAPED_LINE_SEPARATOR);
    }
    
    private class ZendeskCustomerData {
    	
        String idCliente = null;
        StringBuilder datosUsuario = new StringBuilder();
        StringBuilder datosBravo = new StringBuilder();
        StringBuilder datosServicio = new StringBuilder();
        StringBuilder clientName = new StringBuilder();
        
		public String getIdCliente() {
			return idCliente;
		}
		public void setIdCliente(String idCliente) {
			this.idCliente = idCliente;
		}
		public StringBuilder getDatosUsuario() {
			return datosUsuario;
		}
		public void setDatosUsuario(StringBuilder datosUsuario) {
			this.datosUsuario = datosUsuario;
		}
		public StringBuilder getDatosBravo() {
			return datosBravo;
		}
		public void setDatosBravo(StringBuilder datosBravo) {
			this.datosBravo = datosBravo;
		}
		public StringBuilder getDatosServicio() {
			return datosServicio;
		}
		public void setDatosServicio(StringBuilder datosServicio) {
			this.datosServicio = datosServicio;
		}
		public StringBuilder getClientName() {
			return clientName;
		}
		public void setClientName(StringBuilder clientName) {
			this.clientName = clientName;
		}
        
    }
    
    
}