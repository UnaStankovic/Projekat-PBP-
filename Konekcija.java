package projekat;
import java.sql.*;
import javax.swing.JOptionPane;

public class Konekcija {
	
	public static Connection novaKonekcija(String driver, String url, String username, String password) {
    	
		try {
	        Class.forName(driver);
	        if ((username == null) || (password == null) || (username.trim().length() == 0) || (password.trim().length() == 0)) {
	            return DriverManager.getConnection(url);
	        }
	        else {
	            return DriverManager.getConnection(url, username, password);
	        }
    	}
    	catch(SQLException e){
    		error(e);
    	}
    	catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }

    public static void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        }
        catch (SQLException e) {
            error(e);
        }
        catch(Exception e){
        	e.printStackTrace();
        }
    }

    public static void close(PreparedStatement st) {
        try {
            if (st != null) {
                st.close();
            }
        }
        catch (SQLException e) {
            error(e);
        }
        catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        }
        catch (SQLException e) {
            error(e);
        }
        catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void rollback(Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        }
        catch (SQLException e) {
            error(e);
        }
        catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void commit(Connection connection) {
    	try {
    		if(connection != null) {
    			connection.commit();
    		}
    	}
    	catch(SQLException e) {
    		error(e);
    	}
    	catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void error(SQLException e) {
    	if(e.getErrorCode() == -911 || e.getErrorCode() == -913) {
    		JOptionPane.showMessageDialog(null, "Objekat je zakljucan od strane druge transakcije\n. Sacekati neko vreme i pokusati malo kasnije");
    	}
    	else {
    		e.printStackTrace();
    		JOptionPane.showMessageDialog(null, "SQL kod: " + e.getErrorCode() + "\nPoruka: " + e.getMessage());
    		System.exit(0);
    	}
    }
    
}