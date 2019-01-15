package com.mycorp;

import static com.mycorp.constants.ZendeskConstants.BIRTH_DATE;
import static com.mycorp.constants.ZendeskConstants.CUSTOMER_TYPE;
import static com.mycorp.constants.ZendeskConstants.DOCUMENT_NUMBER;
import static com.mycorp.constants.ZendeskConstants.ESCAPED_LINE_SEPARATOR;
import static com.mycorp.constants.ZendeskConstants.ESCAPE_ER;
import static com.mycorp.constants.ZendeskConstants.HTML_BR;
import static com.mycorp.constants.ZendeskConstants.PETICION_ZENDESK;
import static com.mycorp.constants.ZendeskConstants.PHONE;
import static com.mycorp.constants.ZendeskConstants.POTENCIAL;
import static com.mycorp.constants.ZendeskConstants.PROSPECTO;
import static com.mycorp.constants.ZendeskConstants.REAL;
import static com.mycorp.constants.ZendeskConstants.REGISTERED;
import static com.mycorp.constants.ZendeskConstants.TARJETAS_GETDATOS;
import static com.mycorp.constants.ZendeskConstants.TOKEN_ZENDESK;
import static com.mycorp.constants.ZendeskConstants.URL_ZENDESK;
import static com.mycorp.constants.ZendeskConstants.ZENDESK_ERROR_DESTINATARIO;
import static com.mycorp.constants.ZendeskConstants.ZENDESK_ERROR_MAIL_FUNCIONALIDAD;
import static com.mycorp.constants.ZendeskConstants.ZENDESK_USER;
import static com.mycorp.constants.ZendeskConstants.NEW_CUSTOMER_CAUSE_ID;
import static com.mycorp.constants.ZendeskConstants.CUSTOMER_STATE_ID;
import static com.mycorp.constants.ZendeskConstants.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
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
    
        StringBuilder datosUsuario = new StringBuilder();
        StringBuilder datosBravo = new StringBuilder();
        StringBuilder datosServicio = new StringBuilder();
        StringBuilder clientName = new StringBuilder();
        
        ZendeskCustomerData zendeskCustomerData = new ZendeskCustomerData();
        
        // AÃ±ade los datos del formulario
        addFormUserData(usuarioAlta, userAgent, zendeskCustomerData);
      
        // Obtiene el idCliente de la tarjeta
        getIdCustomer(usuarioAlta, mapper, zendeskCustomerData);

        getCustomerBravoData(zendeskCustomerData);

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

	private void getCustomerBravoData(ZendeskCustomerData zendeskCustomerData) {
		
		String currentCustomerId = zendeskCustomerData.getIdCliente();
		StringBuilder currentUserBravoData = zendeskCustomerData.getDatosBravo();

        currentUserBravoData.append(ESCAPED_LINE_SEPARATOR).append(GETTED_CUSTOMER_BRAVO_DATA).append(ESCAPED_LINE_SEPARATOR).append(ESCAPED_LINE_SEPARATOR);
		try
		    {
		        // Obtenemos los datos del cliente
		        DatosCliente cliente = restTemplate.getForObject("http://localhost:8080/test-endpoint", DatosCliente.class, currentCustomerId);
		        //Append Customer's Bravo Data 
		        currentUserBravoData.append(PHONE).append(cliente.getGenTGrupoTmk()).append(ESCAPED_LINE_SEPARATOR);
		        currentUserBravoData.append(BIRTH_DATE).append(formatter.format(formatter.parse(cliente.getFechaNacimiento()))).append(ESCAPED_LINE_SEPARATOR);
		        appendCustomerBravoDataDocumentTypes(currentUserBravoData, cliente);
		        currentUserBravoData.append(DOCUMENT_NUMBER_BD).append(cliente.getNumeroDocAcred()).append(ESCAPED_LINE_SEPARATOR);
		        currentUserBravoData.append(CUSTOMER_TYPE).append(CustomerType.getCustomerType(cliente.getGenTTipoCliente()));
		        currentUserBravoData.append(CUSTOMER_STATE_ID).append(cliente.getGenTStatus()).append(ESCAPED_LINE_SEPARATOR);
		        currentUserBravoData.append(NEW_CUSTOMER_CAUSE_ID).append(cliente.getIdMotivoAlta()).append(ESCAPED_LINE_SEPARATOR);
		        currentUserBravoData.append(REGISTERED).append((cliente.getfInactivoWeb() == null ? YES : NO)).append(ESCAPED_LINE_SEPARATOR + ESCAPED_LINE_SEPARATOR);
		    }catch(Exception e)
		    {
		        LOG.error("Error al obtener los datos en BRAVO del cliente", e);
		    }
		
		zendeskCustomerData.setDatosBravo(currentUserBravoData);
	}
	
	private void appendCustomerBravoDataDocumentTypes(StringBuilder currentUserBravoData, DatosCliente cliente) {
		List< ValueCode > tiposDocumentos = getTiposDocumentosRegistro();
		for(int i = 0; i < tiposDocumentos.size();i++)
		{
		    if(tiposDocumentos.get(i).getCode().equals(cliente.getGenCTipoDocumento().toString()))
		    {
		    	currentUserBravoData.append(DOCUMENT_TYPE_BD).append(tiposDocumentos.get(i).getValue()).append(ESCAPED_LINE_SEPARATOR);
		    }
		}
	}

	private void getIdCustomer(UsuarioAlta usuarioAlta, ObjectMapper mapper, ZendeskCustomerData zendeskCustomerData) {

		String currentCustomerId = zendeskCustomerData.getIdCliente();
		StringBuilder currentCustomerName = zendeskCustomerData.getClientName();
		StringBuilder currentUserServiceData = zendeskCustomerData.getDatosServicio();
		
		if(StringUtils.isNotBlank(usuarioAlta.getNumTarjeta())){
            try{
                String urlToRead = TARJETAS_GETDATOS + usuarioAlta.getNumTarjeta();
                ResponseEntity<String> res = restTemplate.getForEntity( urlToRead, String.class);
                if(res.getStatusCode() == HttpStatus.OK){
                    String dusuario = res.getBody();
                    currentCustomerName.append(dusuario);
                    currentCustomerId = dusuario;
                    currentUserServiceData.append(CARD_SERVICE_RECOVERED_DATA).append(ESCAPED_LINE_SEPARATOR).append(mapper.writeValueAsString(dusuario));
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

                currentCustomerName.append(detallePolizaResponse.getTomador().getNombre()).
                            append(" ").
                            append(detallePolizaResponse.getTomador().getApellido1()).
                            append(" ").
                            append(detallePolizaResponse.getTomador().getApellido2());

                currentCustomerId = detallePolizaResponse.getTomador().getIdentificador();
                currentUserServiceData.append(CARD_SERVICE_RECOVERED_DATA).append(ESCAPED_LINE_SEPARATOR).append(mapper.writeValueAsString(detallePolizaResponse));
            }catch(Exception e)
            {
                LOG.error("Error al obtener los datos de la poliza", e);
            }
        }
        zendeskCustomerData.setIdCliente(currentCustomerId);
        zendeskCustomerData.setClientName(currentCustomerName);
        zendeskCustomerData.setDatosServicio(currentUserServiceData);
	}

	private void addFormUserData(UsuarioAlta usuarioAlta, String userAgent, ZendeskCustomerData zendeskCustomerData) {

		StringBuilder currentUserData = zendeskCustomerData.getDatosUsuario();
		
		if(StringUtils.isNotBlank(usuarioAlta.getNumPoliza())){
			currentUserData.append(CUSTOMER_POLICY_NUMBER).append(usuarioAlta.getNumPoliza()).append("/").append(usuarioAlta.getNumDocAcreditativo());
        }else{
        	currentUserData.append(SANITAS_CARD_ID).append(usuarioAlta.getNumTarjeta());
        }
		currentUserData.append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append(DOCUMENT_TYPE_CD).append(usuarioAlta.getTipoDocAcreditativo()).append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append(DOCUMENT_NUMBER_CD).append(usuarioAlta.getNumDocAcreditativo()).append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append(EMAIL).append(usuarioAlta.getEmail()).append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append(CELLPHONE_NUMBER).append(usuarioAlta.getNumeroTelefono()).append(ESCAPED_LINE_SEPARATOR);
		currentUserData.append(USER_AGENT).append(userAgent).append(ESCAPED_LINE_SEPARATOR);

        zendeskCustomerData.setDatosUsuario(currentUserData);
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
    
	private static class CustomerType {
		
		static HashMap<Integer, String > myCustomerTypeMap = new HashMap<>();

		private CustomerType(){
			myCustomerTypeMap.put(1, (new StringBuilder().append(POTENCIAL).append(ESCAPED_LINE_SEPARATOR)).toString());
			myCustomerTypeMap.put(2, (new StringBuilder().append(REAL).append(ESCAPED_LINE_SEPARATOR)).toString());
			myCustomerTypeMap.put(3, (new StringBuilder().append(PROSPECTO).append(ESCAPED_LINE_SEPARATOR)).toString());
		}
		
		public static String getCustomerType(Integer key){
			return myCustomerTypeMap.get(key);
		}
		
	}
    
}