package org.json;

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
 * Copyright 2012-2016 - UDS/Centre de Données astronomiques de Strasbourg (CDS),
 *                       Astronomisches Rechen Institut (ARI)
 */

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import uws.ISO8601Format;
import uws.job.ErrorSummary;
import uws.job.JobInfo;
import uws.job.JobList;
import uws.job.Result;
import uws.job.UWSJob;
import uws.job.serializer.XMLSerializer;
import uws.job.serializer.filter.JobListFilter;
import uws.job.user.JobOwner;
import uws.service.UWS;
import uws.service.UWSUrl;

/**
 * Useful conversion functions from UWS to JSON.
 * 
 * @author Gr&eacute;gory Mantelet (CDS;ARI)
 * @version 4.2 (04/2016)
 */
public final class Json4Uws {

	private Json4Uws(){
		;
	}

	/**
	 * Gets the JSON representation of the given UWS.
	 * @param uws				The UWS to represent in JSON.
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	public final static JSONObject getJson(final UWS uws) throws JSONException{
		JSONObject json = new JSONObject();
		if (uws != null){
			json.put("name", uws.getName());
			json.put("version", XMLSerializer.UWS_VERSION);
			json.put("description", uws.getDescription());

			JSONArray jobLists = new JSONArray();
			for(JobList jobList : uws){
				JSONObject jsonJL = new JSONObject();
				UWSUrl jlUrl = jobList.getUrl();
				jsonJL.put("name", jobList.getName());
				if (jlUrl != null)
					jsonJL.put("href", jlUrl.getRequestURI());
				jobLists.put(jsonJL);
			}

			json.put("jobLists", jobLists);
		}
		return json;
	}

	/**
	 * Gets the JSON representation of the given jobs list.
	 * @param jobsList			The jobs list to represent in JSON.
	 * @param owner				The user who asks to serialize the given jobs list. (MAY BE NULL)
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 * @see #getJson(JobList, JobOwner, JobListFilter)
	 */
	public final static JSONObject getJson(final JobList jobsList, final JobOwner owner) throws JSONException{
		return getJson(jobsList, owner, null);
	}

	/**
	 * Gets the JSON representation of the given jobs list by filtering using user-specified filters.
	 * 
	 * @param jobsList			The jobs list to represent in JSON.
	 * @param owner				The user who asks to serialize the given jobs list. (MAY BE NULL)
	 * @param filter			Represent all the specified job filters to apply ; only the job that pass through this filter should be displayed.
	 *              			If null, all jobs are displayed.
	 * 
	 * @return					Its JSON representation.
	 * 
	 * @throws JSONException	If there is an error while building the JSON object.
	 * 
	 * @since 4.2
	 */
	public final static JSONObject getJson(final JobList jobsList, final JobOwner owner, final JobListFilter filter) throws JSONException{
		JSONObject json = new JSONObject();
		if (jobsList != null){
			json.put("name", jobsList.getName());
			json.put("version", XMLSerializer.UWS_VERSION);
			JSONArray jsonJobs = new JSONArray();
			UWSUrl jobsListUrl = jobsList.getUrl();

			// Security filter: retrieve only the jobs of the specified owner:
			Iterator<UWSJob> it = jobsList.getJobs(owner);

			// User filter: filter the jobs in function of filters specified by the user:
			if (filter != null)
				it = filter.filter(it);

			// Append the JSON serialization of all filtered jobs:
			JSONObject jsonObj = null;
			while(it.hasNext()){
				jsonObj = getJson(it.next(), jobsListUrl, true);
				if (jsonObj != null)
					jsonJobs.put(jsonObj);
			}

			json.put("jobs", jsonJobs);
		}
		return json;
	}

	/**
	 * Gets the JSON representation of the given job.
	 * @param job				The job to represent in JSON.
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	public final static JSONObject getJson(final UWSJob job) throws JSONException{
		return getJson(job, null, false);
	}

	/**
	 * Gets the JSON representation of the given job.
	 * @param job				The job to represent in JSON.
	 * @param jobsListUrl		The URL of its jobs list. (MAY BE NULL)
	 * @param reference			<i>true</i> if only a reference to the given job must be returned rather than its full description,
	 * 							<i>false</i> otherwise.
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	public final static JSONObject getJson(final UWSJob job, final UWSUrl jobsListUrl, final boolean reference) throws JSONException{
		JSONObject json = new JSONObject();
		if (job != null){
			json.put("version", XMLSerializer.UWS_VERSION);
			json.put(UWSJob.PARAM_JOB_ID, job.getJobId());
			json.put(UWSJob.PARAM_PHASE, job.getPhase());
			if (reference){
				if (jobsListUrl != null){
					jobsListUrl.setJobId(job.getJobId());
					json.put("href", jobsListUrl.getRequestURL());
				}
				if (job.getRunId() != null)
					json.put(UWSJob.PARAM_RUN_ID, job.getRunId());
				if (job.getOwner() != null)
					json.put(UWSJob.PARAM_OWNER, job.getOwner().getPseudo());
				json.put(UWSJob.PARAM_CREATION_TIME, ISO8601Format.format(job.getCreationTime()));
			}else{
				json.put(UWSJob.PARAM_RUN_ID, job.getRunId());
				if (job.getOwner() != null)
					json.put(UWSJob.PARAM_OWNER, job.getOwner().getPseudo());
				json.put(UWSJob.PARAM_QUOTE, job.getQuote());
				json.put(UWSJob.PARAM_CREATION_TIME, ISO8601Format.format(job.getCreationTime()));
				if (job.getStartTime() != null)
					json.put(UWSJob.PARAM_START_TIME, ISO8601Format.format(job.getStartTime()));
				if (job.getEndTime() != null)
					json.put(UWSJob.PARAM_END_TIME, ISO8601Format.format(job.getEndTime()));
				if (job.getDestructionTime() != null)
					json.put(UWSJob.PARAM_DESTRUCTION_TIME, ISO8601Format.format(job.getDestructionTime()));
				json.put(UWSJob.PARAM_EXECUTION_DURATION, job.getExecutionDuration());
				json.put(UWSJob.PARAM_PARAMETERS, getJobParamsJson(job));
				json.put(UWSJob.PARAM_RESULTS, getJobResultsJson(job));
				json.put(UWSJob.PARAM_ERROR_SUMMARY, getJson(job.getErrorSummary()));
				if (job.hasJobInfo())
					json.put(UWSJob.PARAM_JOB_INFO, getJson(job.getJobInfo()));
			}
		}
		return json;
	}

	/**
	 * Gets the JSON representation of the parameters of the given job.
	 * @param job				The job whose the parameters must be represented in JSON.
	 * @return					The JSON representation of its parameters.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	public final static JSONObject getJobParamsJson(final UWSJob job) throws JSONException{
		JSONObject json = new JSONObject();
		if (job != null){
			Object val;
			for(String name : job.getAdditionalParameters()){
				// get the raw value:
				val = job.getAdditionalParameterValue(name);
				// if an array, build a JSON array of strings:
				if (val != null && val.getClass().isArray()){
					JSONArray array = new JSONArray();
					for(Object o : (Object[])val){
						if (o != null)
							array.put(o.toString());
					}
					json.put(name, array);
				}
				// otherwise, just put the value:
				else
					json.put(name, val);
			}
		}
		return json;
	}

	/**
	 * Gets the JSON representation of the results of the given job.
	 * @param job				The job whose the results must be represented in JSON.
	 * @return					The JSON representation of its results.
	 * @throws JSONException	If there is an error while building the JSON array.
	 */
	public final static JSONArray getJobResultsJson(final UWSJob job) throws JSONException{
		JSONArray json = new JSONArray();
		if (job != null){
			Iterator<Result> it = job.getResults();
			if (it == null)
				return null;

			while(it.hasNext())
				json.put(getJobResultJson(it.next()));
		}
		return json;
	}

	/**
	 * Gets the JSON representation of the the given result.
	 * @param r					The result to represent in JSON.
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	public final static JSONObject getJobResultJson(final Result r) throws JSONException{
		JSONObject resultJson = new JSONObject();
		if (r != null){
			resultJson.put("id", r.getId());
			resultJson.put("type", r.getType());
			resultJson.put("href", r.getHref());
			resultJson.put("mime", r.getMimeType());
			if (r.getSize() >= 0)
				resultJson.put("size", r.getSize());
			resultJson.put("redirection", r.isRedirectionRequired());
		}
		return resultJson;
	}

	/**
	 * Gets the JSON representation of the given error summary.
	 * @param error				The error summary to represent in JSON.
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	public final static JSONObject getJson(final ErrorSummary error) throws JSONException{
		JSONObject errorJson = new JSONObject();
		if (error != null){
			errorJson.put("type", error.getType());
			errorJson.put("hasDetail", error.hasDetail());
			errorJson.put("detailsRef", error.getDetails());
			errorJson.put("message", error.getMessage());
		}
		return errorJson;
	}

	/**
	 * Gets the JSON representation of the given {@link JobInfo}.
	 * @param info				The additional job information to represent in JSON.
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 * @since 4.2
	 */
	public final static Object getJson(final JobInfo info) throws JSONException{
		if (info != null && info.getContent() != null && info.getType() != null){
			switch(info.getType()){
				case ARRAY:
				case MAP:
					return info.getContent();
				default:
					return info.toJSON();
			}
		}else
			return null;
	}

	/**
	 * Gets the JSON representation of the given list of job information.
	 * @param	jobInfo			The job information to represent in JSON.
	 * @return					Its JSON representation.
	 * @throws	JSONException	If there is an error while building the JSON object.
	 * @since 4.2
	 */
	public final static JSONObject getJson(final Iterator<Map.Entry<String,Object>> jobInfo) throws JSONException{
		JSONObject jobInfoJson = new JSONObject();
		if (jobInfo != null){
			Map.Entry<String,Object> entry;
			while(jobInfo.hasNext()){
				entry = jobInfo.next();
				if (entry.getValue() == null)
					continue;
				else if (entry.getValue() instanceof Date)
					jobInfoJson.put(entry.getKey(), ISO8601Format.format((Date)entry.getValue()));
				else
					jobInfoJson.put(entry.getKey(), entry.getValue());
			}
		}
		return jobInfoJson;
	}

	/**
	 * Gets the JSON representation of the given pair key/value.
	 * @param key				The value name.
	 * @param value				The value of type long corresponding to the given key/name.
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	public final static JSONObject getJson(final String key, final long value) throws JSONException{
		JSONObject json = new JSONObject();
		if (key != null && !key.trim().isEmpty())
			json.put(key, value);
		return json;
	}

	/**
	 * Gets the JSON representation of the given pair key/value.
	 * @param key				The value name.
	 * @param value				The value of type String corresponding to the given key/name.
	 * @return					Its JSON representation.
	 * @throws JSONException	If there is an error while building the JSON object.
	 */
	public final static JSONObject getJson(final String key, final String value) throws JSONException{
		JSONObject json = new JSONObject();
		if (key != null && !key.trim().isEmpty())
			json.put(key, value);
		return json;
	}
}
