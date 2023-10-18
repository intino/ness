package io.intino.datahub.datamart;

import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeShiftCache {
	private final File file;
	private Connection connection;
	private PreparedStatement query;
	private PreparedStatement insert;
	private PreparedStatement delete;
	private ExecutorService executorService;
	private ScheduledExecutorService commitService;

	public TimeShiftCache(File file) {
		this.file = file;
	}

	public TimeShiftCache open() {
		try {
			if (connection != null && !connection.isClosed()) return this;
			file.getParentFile().mkdirs();
			Class.forName("org.sqlite.JDBC");
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
			this.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS events (id text NOT NULL PRIMARY KEY, ts bigint);");
			this.insert = connection.prepareStatement("INSERT OR REPLACE INTO events (id, ts) VALUES(?,?);");
			this.delete = connection.prepareStatement("DELETE FROM events WHERE id=?;");
			this.query = connection.prepareStatement("SELECT * FROM events WHERE id=?");
			connection.setAutoCommit(false);
			this.executorService = Executors.newSingleThreadExecutor(r -> new Thread(r, "TimeShift-" + file.getName()));
			this.commitService = Executors.newScheduledThreadPool(1, r -> new Thread(r, "TimeShift-commit-" + file.getName()));
			commitService.scheduleAtFixedRate(this::commit, 1, 1, TimeUnit.MINUTES);
		} catch (SQLException | ClassNotFoundException e) {
			Logger.error(e);
		}
		return this;
	}

	public synchronized void put(String id, Instant ts) {
		executorService.execute(() -> {
			try {
				insert.setString(1, id);
				insert.setLong(2, ts.toEpochMilli() / 1000);
				insert.executeUpdate();
			} catch (SQLException e) {
				Logger.error(e);
			}
		});
	}

	public synchronized Instant get(String id) {
		commit();
		try (ResultSet rs = query(id)) {
			boolean next = rs.next();
			if (!next) return null;
			return Instant.ofEpochSecond(rs.getLong(2));
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public synchronized void remove(String id) {
		try {
			delete.setString(1, id);
			delete.executeUpdate();
		} catch (SQLException e) {
			Logger.error(e);
		}
	}

	private void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			Logger.error(e);
		}
	}

	private ResultSet query(String id) throws SQLException {
		query.setString(1, id);
		return query.executeQuery();
	}

	public void close() throws Exception {
		try {
			closeExecutor();
			insert.close();
			delete.close();
			query.close();
			if (connection != null) connection.close();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void closeExecutor() {
		try {
			executorService.shutdown();
			executorService.awaitTermination(1, TimeUnit.MINUTES);
			commitService.shutdown();
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}
}
