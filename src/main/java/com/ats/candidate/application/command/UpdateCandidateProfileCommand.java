package com.ats.candidate.application.command;

import java.util.Set;

public record UpdateCandidateProfileCommand(Long candidateId, String fullName, String email, String phone,
                                            Set<Long> skillIds) {
}
