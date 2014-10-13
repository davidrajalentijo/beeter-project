package edu.upc.eetac.dsa.smachado.beeter.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

import edu.upc.eetac.dsa.smachado.beeter.api.model.Sting;
import edu.upc.eetac.dsa.smachado.beeter.api.model.StingCollection;
 
@Path("/stings") //URI relativa stings
public class StingResource {
	private DataSource ds = DataSourceSPA.getInstance().getDataSource(); //obtenemos referencia al datasource, para hacer operaciones CRUD, obteniendola con el singelton que hemos generado



	private String GET_STINGS_QUERY = "select s.*, u.name from stings s, users u where u.username=s.username order by creation_timestamp desc";
	private String GET_STING_BY_ID_QUERY = "select s.*, u.name from stings s, users u where u.username=s.username and s.stingid=?";
	private String INSERT_STING_QUERY = "insert into stings (username, subject, content) value (?, ?, ?)";
	private String DELETE_STING_QUERY = "delete from stings where stingid=?";
	private String UPDATE_STING_QUERY = "update stings set subject=ifnull(?, subject), content=ifnull(?, content) where stingid=?";
	
	@GET //Nos devuelve toda la colección de stings
	@Produces(MediaType.BEETER_API_STING_COLLECTION) //indica el mediatype de la respuesta, donde veremos una cabecera content-type igual al valor de este string
	public StingCollection getStings() {
		StingCollection stings = new StingCollection(); //modelo
	 
		 //conexion se obtiene a través del datasource
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
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
		} //catch (SQLException e) {
			//e.printStackTrace();
		//} finally {
			//try {
				//if (stmt != null)
					//stmt.close();
				//conn.close(); //cerramos la conexión, la devolvemos al pool
			//} catch (SQLException e) {
			//}
		//}
		catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
	 
		return stings; //devolvemos Stings
	}
	
	//MOXY pasa JSON a un objeto java y un objeto java a json

	
	
	@GET //Metodo que nos devuelve un sting en concreto pasandole una id
	@Path("/{stingid}")
	@Produces(MediaType.BEETER_API_STING) //mediatype definido
	public Sting getSting(@PathParam("stingid") String stingid) {
		//En el pathparam le pasamos el valor de lo que queremos pasarle es decir el stingid y se lo indicamos
		Sting sting = new Sting();
	 
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
	 
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(GET_STING_BY_ID_QUERY);
			stmt.setInt(1, Integer.valueOf(stingid));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				sting.setStingid(rs.getString("stingid"));
				sting.setUsername(rs.getString("username"));
				sting.setAuthor(rs.getString("name"));
				sting.setSubject(rs.getString("subject"));
				sting.setContent(rs.getString("content"));
				sting.setLastModified(rs.getTimestamp("last_modified")
						.getTime());
				sting.setCreationTimestamp(rs
						.getTimestamp("creation_timestamp").getTime());
			} else {
			throw new NotFoundException("There's no sting with stingid="
			+ stingid);
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
	 
		return sting;
	}
	
	
	@POST //metodo para crear un sting
	@Consumes(MediaType.BEETER_API_STING) //no especifica el tipo que se come, jersey coje el json y crea un sting
	@Produces(MediaType.BEETER_API_STING)
	public Sting createSting(Sting sting) {
		validateSting(sting);
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
	 
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(INSERT_STING_QUERY,
					Statement.RETURN_GENERATED_KEYS); //le pasamos la query y le pedimos que nos devuelva las claves generadas (primary key autogenerada) ya que stingid es autoincremental y queremos saber su valor (retur_generate_keys)
	 //llamamos al metodo get stings y le pasamos el stingid que queremos recuperar
			stmt.setString(1, sting.getUsername());
			stmt.setString(2, sting.getSubject());
			stmt.setString(3, sting.getContent());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys(); //devuelve stingid, si no devuelve nada es que algo ha ido mal
			if (rs.next()) {
				int stingid = rs.getInt(1);
	 
				sting = getSting(Integer.toString(stingid));
			} else {
				// Something has failed...
			}
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
	 
		return sting;
	}
	
	
	private void validateSting(Sting sting) {
		if (sting.getSubject() == null)
			throw new BadRequestException("Subject can't be null.");
		if (sting.getContent() == null)
			throw new BadRequestException("Content can't be null.");
		if (sting.getSubject().length() > 100)
			throw new BadRequestException("Subject can't be greater than 100 characters.");
		if (sting.getContent().length() > 500)
			throw new BadRequestException("Content can't be greater than 500 characters.");
	}
	
	
	
	
	@DELETE //metodo para borrar un sting concreto
	@Path("/{stingid}")
	public void deleteSting(@PathParam("stingid") String stingid) {
		//tenemos un void de manera que no devuelve nada ni consume ni produce, devuelve 204 ya que no hay contenido
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
	 
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(DELETE_STING_QUERY);
			stmt.setInt(1, Integer.valueOf(stingid));
	 
			int rows = stmt.executeUpdate();
			if (rows == 0)
				throw new NotFoundException("There's no sting with stingid="
						+ stingid);
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
	}



	@PUT
	@Path("/{stingid}")
	@Consumes(MediaType.BEETER_API_STING)
	@Produces(MediaType.BEETER_API_STING)
	public Sting updateSting(@PathParam("stingid") String stingid, Sting sting) {
		validateUpdateSting(sting);
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			throw new ServerErrorException("Could not connect to the database",
					Response.Status.SERVICE_UNAVAILABLE);
		}
	 
		PreparedStatement stmt = null;
		try {
			String sql = buildUpdateSting();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, sting.getSubject());
			stmt.setString(2, sting.getContent());
			stmt.setInt(3, Integer.valueOf(stingid));
	 
			int rows = stmt.executeUpdate();
			if (rows == 1)
				sting = getSting(stingid);
			else {
				throw new NotFoundException("There's no sting with stingid="
						+ stingid);
			}
	 
		} catch (SQLException e) {
			throw new ServerErrorException(e.getMessage(),
					Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				conn.close();
			} catch (SQLException e) {
			}
		}
	 
		return sting;
	}
	 
	private void validateUpdateSting(Sting sting) {
		if (sting.getSubject() != null && sting.getSubject().length() > 100)
			throw new BadRequestException(
					"Subject can't be greater than 100 characters.");
		if (sting.getContent() != null && sting.getContent().length() > 500)
			throw new BadRequestException(
					"Content can't be greater than 500 characters.");
	}
	
	private String buildUpdateSting() {
		return "update stings set subject=ifnull(?, subject), content=ifnull(?, content) where stingid=?";
	}
	
	


}