package hu.webarticum.miniconnect.generaldocs.examples.holodbembedded;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import jakarta.inject.Inject;
import picocli.CommandLine.Command;

@Command
public class ApplicationCommand implements Runnable {
    
    private final DataSource dataSource;
    

    @Inject
    public ApplicationCommand(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    
    @Override
    public void run() {
        try {
            runThrows();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void runThrows() throws Exception {
        String sql = "SELECT col FROM sch.tbl";
        
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            String colStr = resultSet.getString("col");
            System.out.println("col: " + colStr);
        }
    }

}
