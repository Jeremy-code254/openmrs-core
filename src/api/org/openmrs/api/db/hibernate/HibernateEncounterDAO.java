/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.EncounterDAO;

public class HibernateEncounterDAO implements EncounterDAO {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	public HibernateEncounterDAO() {
	}

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounter(org.openmrs.Encounter)
	 */
	public Encounter saveEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(encounter);
		return encounter;
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 */
	public void deleteEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounter);
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounter(java.lang.Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws DAOException {
		return (Encounter) sessionFactory.getCurrentSession()
		                                 .get(Encounter.class, encounterId);
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByPatientId(java.lang.Integer,
	 *      boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncountersByPatientId(Integer patientId)
	        throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession()
		                              .createCriteria(Encounter.class)
		                              .createAlias("patient", "p")
		                              .add(Expression.eq("p.patientId",
		                                                 patientId))
		                              .add(Expression.eq("voided", false))
		                              .addOrder(Order.desc("encounterDatetime"));

		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location, java.util.Date, java.util.Date,
	 *      java.util.Collection, java.util.Collection, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncounters(Patient patient, Location location,
	        Date fromDate, Date toDate, Collection<Form> enteredViaForms,
	        Collection<EncounterType> encounterTypes, boolean includeVoided) {
		Criteria crit = sessionFactory.getCurrentSession()
		                              .createCriteria(Encounter.class);
		if (patient != null && patient.getPatientId() != null) {
			crit.add(Expression.eq("patient", patient));
		}
		if (location != null && location.getLocationId() != null) {
			crit.add(Expression.eq("location", location));
		}
		if (fromDate != null) {
			crit.add(Expression.ge("encounterDatetime", fromDate));
		}
		if (toDate != null) {
			crit.add(Expression.le("encounterDatetime", toDate));
		}
		if (enteredViaForms != null && enteredViaForms.size() > 0) {
			crit.add(Expression.in("form", enteredViaForms));
		}
		if (encounterTypes != null && encounterTypes.size() > 0) {
			crit.add(Expression.in("encounterType", encounterTypes));
		}
		if (!includeVoided) {
			crit.add(Expression.eq("voided", false));
		}
		crit.addOrder(Order.asc("encounterDatetime"));
		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounterType(org.openmrs.EncounterType)
	 */
	public EncounterType saveEncounterType(EncounterType encounterType) {
		sessionFactory.getCurrentSession().saveOrUpdate(encounterType);
		return encounterType;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#deleteEncounterType(org.openmrs.EncounterType)
	 */
	public void deleteEncounterType(EncounterType encounterType)
	        throws DAOException {
		sessionFactory.getCurrentSession().delete(encounterType);
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId)
	        throws DAOException {
		return (EncounterType) sessionFactory.getCurrentSession()
		                                     .get(EncounterType.class,
		                                          encounterTypeId);
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterType(java.lang.String)
	 */
	public EncounterType getEncounterType(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession()
		                              .createCriteria(EncounterType.class);
		crit.add(Expression.eq("retired", false));
		crit.add(Expression.eq("name", name));
		EncounterType encounterType = (EncounterType) crit.uniqueResult();

		return encounterType;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getAllEncounterTypes(java.lang.Boolean)
	 */
	public List<EncounterType> getAllEncounterTypes(Boolean includeRetired)
	        throws DAOException {
		return sessionFactory.getCurrentSession()
		                     .createCriteria(EncounterType.class)
		                     .add(Expression.eq("retired", includeRetired))
		                     .addOrder(Order.asc("name"))
		                     .list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#findEncounterTypes(java.lang.String)
	 */
	public List<EncounterType> findEncounterTypes(String name)
	        throws DAOException {
		return sessionFactory.getCurrentSession()
		                     .createCriteria(EncounterType.class)
		                     // 'ilike' case insensitive search
		                     .add(Expression.ilike("name",
		                                           name,
		                                           MatchMode.START))
		                     .addOrder(Order.asc("name"))
		                     .list();
	}
	
	/**
     * @see org.openmrs.api.db.EncounterDAO#getEncounterByGuid(java.lang.String)
     */
    public Encounter getEncounterByGuid(String guid) {
		return (Encounter) sessionFactory.getCurrentSession().createQuery("from Encounter e where e.guid = :guid").setString("guid", guid).uniqueResult();
    }

	/**
     * @see org.openmrs.api.db.EncounterDAO#getEncounterTypeByGuid(java.lang.String)
     */
    public EncounterType getEncounterTypeByGuid(String guid) {
		return (EncounterType) sessionFactory.getCurrentSession().createQuery("from EncounterType et where et.guid = :guid").setString("guid", guid).uniqueResult();
    }

	/**
     * @see org.openmrs.api.db.EncounterDAO#getLocationByGuid(java.lang.String)
     */
    public Location getLocationByGuid(String guid) {
		return (Location) sessionFactory.getCurrentSession().createQuery("from Location l where l.guid = :guid").setString("guid", guid).uniqueResult();
    }
    
	/**
     * @see org.openmrs.api.db.EncounterDAO#getSavedEncounterDatetime(org.openmrs.Encounter)
     */
    public Date getSavedEncounterDatetime(Encounter encounter) {
	    SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery("select encounter_datetime from encounter where encounter_id = :encounterId");
	    sql.setInteger("encounterId", encounter.getEncounterId());
	    return (Date) sql.uniqueResult();
    }
	
}
