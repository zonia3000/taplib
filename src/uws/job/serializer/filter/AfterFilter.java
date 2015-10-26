package uws.job.serializer.filter;

/*
 * This file is part of UWSLibrary.
 * 
 * UWSLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UWSLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with UWSLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2015 - Astronomisches Rechen Institut (ARI) 
 */

import java.util.Date;

import uws.job.UWSJob;

/**
 * <p>Job filter based on the start time.
 * Only jobs that have started after a given date (not-included) are kept.</p>
 * 
 * <p><i><b>Important note:</b>
 * 	This filter will also reject all jobs that have not yet started.
 * </i></p>
 * 
 * @author Gr&eacute;gory Mantelet (ARI)
 * @version 4.2 (10/2015)
 * @since 4.2
 */
public final class AfterFilter implements JobFilter {

	/** The date after which jobs must be kept. */
	private final Date limit;

	/**
	 * Build the {@link AfterFilter} with the given limit date.
	 * 
	 * @param date	The date (non-included) after which jobs must be kept.
	 * 
	 * @throws NullPointerException	If the given date is NULL.
	 */
	public AfterFilter(final Date date) throws NullPointerException{
		if (date == null)
			throw new NullPointerException("Missing limit date! Can not create an AfterFilter.");

		limit = date;
	}

	/**
	 * Get the date which filters jobs on their startTime.
	 * Only jobs started after this date will be retained. 
	 * 
	 * @return	The limit date.
	 */
	public final Date getDate(){
		return limit;
	}

	@Override
	public boolean match(final UWSJob job){
		return (job != null) && (job.getStartTime() != null) && (job.getStartTime().after(limit));
	}

}
