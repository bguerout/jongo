package org.jongo.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class EmbeddedMongoRule implements TestRule {

	private static DB db;

	public static Mongo getMongo() {
		return MongoHolder.getInstance();
	}
	
	public static DB getTestDatabase() {
		return db;
	}

	public EmbeddedMongoRule() {
		setUpTestDatabase();
	}

	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				before(description);
				try {
					base.evaluate();
				} finally {
					after();
				}
			}
		};
	}

	protected void before(Description description) throws Exception {
		setUpTestDatabase();
	}

	protected void after() {
		tearDownTestDatabase();
	}
	
	public void setUpTestDatabase(){
		if(db == null) {
			db = getMongo().getDB("test_" + System.nanoTime());
		}
	}
	
	public void tearDownTestDatabase(){
		if (db != null) {
			db.dropDatabase();
			db = null;
		}
	}
	
}
