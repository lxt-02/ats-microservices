/**
 * Job requisitions and postings (jobs, job_skills), the skill master data (skills) and the public Career Site
 * job listing. Skills live here because jobs own them; the Candidate module reads them through
 * {@link com.ats.job.application.contract.SkillCatalog}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Job")
package com.ats.job;
