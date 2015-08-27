package org.n52.server.oxf.util.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.persistence.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

public class HibernateUtil {
	protected static HibernateUtil utilInstance = null;
	protected static int batchSize = 20;
	private static SessionFactory sessionFactory = null;
	private static final Logger LOG = LoggerFactory.getLogger(HibernateUtil.class);
	private static ArrayList<Session> activeSessions = new ArrayList<Session>();
	private static Session mainSession = null;

	public static HibernateUtil getInstance() {
		if (utilInstance == null) {
			LOG.info("HibernateUtil initializing.................................");
			utilInstance = new HibernateUtil();
		}
		return utilInstance;
	}

	private HibernateUtil() {
		try {
			sessionFactory = new AnnotationConfiguration().configure("/hibernate.tereno22.xml").buildSessionFactory();
			//sessionFactory = new AnnotationConfiguration().configure("/hibernate.wascal.xml").buildSessionFactory();
			mainSession = sessionFactory.openSession();
			// Create the SessionFactory from hibernate.cfg.xml

		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			LOG.error("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public Object loadObject(Object object, Long primaryKey) {
		Session s = sessionFactory.openSession();
		Transaction tx = null;
		Object obj = null;
		try {
			tx = s.beginTransaction();
			obj = s.get(object.getClass(), primaryKey);
			activeSessions.add(s);
			s.getTransaction().commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
		return obj;
	}

	public void closeAllActiveSessions() {
		for (Session s : activeSessions) {
			if (s.isOpen()) {
				s.close();
			}
		}
		this.activeSessions.clear();
	}

	private static Object getPrimaryKeyValue(Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class clazz = object.getClass();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getAnnotation(Id.class) != null) {
				return method.invoke(object);

			}
		}
		throw new IllegalArgumentException("object has no primary key:\n" + object.toString());
	}

	public Session getMainSession() {
		if(mainSession==null && !mainSession.isOpen()) {
			LOG.info("Prev main sessions is closed. Recreating new main session.................");
			mainSession = sessionFactory.openSession();
		}
		return mainSession;
	}
	/*
	public void closeMainSession()
	{
		if (mainSession!=null && mainSession.isOpen()) {
			//LOG.info("Closing previous main session.................");
			mainSession.close();
		}
	}*/
	
	public static void setMainSession(Session mainsession) {
		mainSession = mainsession;
	}


}
