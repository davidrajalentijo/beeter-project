package edu.upc.eetac.dsa.smachado.beeter.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import edu.upc.eetac.dsa.smachado.beeter.api.model.Sting;
import edu.upc.eetac.dsa.smachado.beeter.api.model.StingCollection;
 
@Path("/stings") //URI relativa stings
public class StingResource {
	private DataSource ds = DataSourceSPA.getInstance().getDataSource(); //obtenemos referencia al datasource, para hacer operaciones CRUD, obteniendola con el singelton que hemos generado



	private String GET_STINGS_QUERY = "select s.*, u.name from stings s, users u where u.username=s.username order by creation_timestamp desc";
	 
	@GET
	@Produces(MediaType.BEETER_API_STING_COLLECTION) //indica el mediatype de la respuesta, donde veremos una cabecera content-type igual al valor de este string
	public StingCollection getStings() {
		StingCollection stings = new StingCollection(); //modelo
	 
		Connection conn = null; //conexion se obtiene a través del datasource
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	 
		PreparedStatement stmt = null; 
		try {
			stmt = conn.prepareStatement(GET_STINGS_QUERY); //preparamos la query
			ResultSet rs = stmt.executeQuery(); //ejecutamos la query
			while (rs.next()) {
				Sting sting = new Sting(); //modelo para un sting
				sting.setStingid(rs.getString("stingid")); //damos valores a lo que recuperamos (atributos) de la base de datos
				sting.setUsername(rs.getString("username"));
				sting.setAuthor(rs.getString("name"));
				sting.setSubject(rs.getString("subject"));
				sting.setLastModified(rs.getTimestamp("last_modified")
						.getTime());
				sting.setCreationTimestamp(rs
						.getTimestamp("creation_timestamp").getTime());
				stings.addSting(sting); //lo añadimos a la conexión
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close(); //cerramos la conexión, la devolvemos al pool
			} catch (SQLException e) {
			}
		}
	 
		return stings; //devolvemos Stings
	}
	
	//MOXY pasa JSON a un objeto java y un objeto java a json







}