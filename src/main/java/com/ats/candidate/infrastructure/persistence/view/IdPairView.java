package com.ats.candidate.infrastructure.persistence.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** (key, value) pair of ids or (id, count). */
@Getter
@Setter
@NoArgsConstructor
public class IdPairView {

    private Long keyId;
    private Long valueId;
}
