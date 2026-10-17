import java.sql.*;

public class Database {
    // koneksi ke mysql dan statement untuk menjalankan query
    private Connection connection;
    private Statement statement;

    // constructor
    public Database() {
        try {
            // sambungkan ke database db_people (mysql dari xampp/laragon)
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/db_people",
                    "root",
                    "");

            // statement dibuat sekali, dipakai ulang di semua query
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // digunakan untuk SELECT
    public ResultSet selectQuery(String sql) {
        try {
            // jalankan query lalu kembalikan hasilnya
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // digunakan untuk INSERT, UPDATE, DELETE
    public int insertUpdateDeleteQuery(String sql) {
        try {
            // return-nya jumlah baris yang berubah
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // getter
    public Statement getStatement() {
        return statement;
    }
}