package io.intino.datahub.datamart;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.intino.alexandria.logger.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class TimeShiftCache implements AutoCloseable {
	private final File file;
	private Connection connection;
	private PreparedStatement query;
	private PreparedStatement insert;
	private PreparedStatement delete;

	public TimeShiftCache(File file) {
		this.file = file;
	}

	public void open() {
		try {
			file.getParentFile().mkdirs();
			DataSource dataSource = dataSource();
			this.connection = dataSource.getConnection();
			this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS events (id text NOT NULL PRIMARY KEY, ts bigint);");
			this.insert = connection.prepareStatement("INSERT OR REPLACE INTO events (id, ts) VALUES(?,?);");
			this.delete = connection.prepareStatement("DELETE FROM events WHERE id=?;");
			this.query = connection.prepareStatement("SELECT * FROM event WHERE id=?");
		} catch (SQLException e) {
			Logger.error(e);
		}
	}

	public synchronized void put(String id, Instant ts) {
		try {
			insert.setString(1, id);
			insert.setLong(2, ts.toEpochMilli() / 1000);
		} catch (SQLException e) {
			Logger.error(e);
		}
	}

	public synchronized Instant get(String id) {
		try (ResultSet rs = query(id)) {
			boolean next = rs.next();
			if (!next) return null;
			return Instant.ofEpochSecond(rs.getLong(2));
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	private ResultSet query(String id) throws SQLException {
		query.setString(1, id);
		return query.executeQuery();
	}

	public synchronized void remove(String id) {
		try {
			delete.setString(1, id);
			delete.execute();
		} catch (SQLException e) {
			Logger.error(e);
		}
	}


	@Override
	public void close() throws Exception {
		try {
			if (connection != null) connection.close();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		config.setPoolName("HikariSQLiteConnectionPool");
		config.addDataSourceProperty("cachePrepStmts", "false");
		config.addDataSourceProperty("prepStmtCacheSize", "25");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "48");
		config.addDataSourceProperty("useServerPrepStmts", "true");
		config.addDataSourceProperty("implicitCachingEnabled", "true");
		config.setDriverClassName("org.sqlite.JDBC");
		config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
		return new HikariDataSource(config);
	}
}
